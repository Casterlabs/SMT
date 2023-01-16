package co.casterlabs.smt.packeteer.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

import co.casterlabs.commons.functional.tuples.Triple;
import co.casterlabs.smt.packeteer.Packet;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@Accessors(chain = true)
public class PacketIO {
    private static final byte[] headerMagic = {
            0,
            'S',
            'M',
            'T'
    };

    private static final byte headerLength = 0
        + 4 // Magic
        + 2 // Flags
        + 4 // ID
        + 2 // Payload Length
        + 4 // CRC32 (Flags + ID + Payload Length)
        + 4 // CRC32 (Payload)
    ;

    public static final int bodyMaxLength = 32768 - headerLength; // Just shy of 32kb

    public static final int FLAG_UNRELIABLE = 0; // Not used directly by packeteer, just defined here for other frameworks.
    public static final int FLAG_IGNORE_BODY_CRC = 1;

    @Getter
    @Setter
    @NonNull
    private Flags flags = new Flags(); // Note that we only use the lower 16 bits. The upper 16 are not serialized.

    @Getter
    @Setter
    @NonNull
    private FastLogger logger = new FastLogger(LogLevel.NONE);

    private BigEndianIOUtil util = new BigEndianIOUtil();

    public void serialize(Packet packet, OutputStream out) throws IOException {
        this.serialize(packet.getId(), packet.serialize(), out);
    }

    public void serialize(int id, byte[] payload, OutputStream out) throws IOException {
        if (payload.length > bodyMaxLength) throw new IOException("Payload cannot be larger than " + bodyMaxLength);

        // Magic
        out.write(headerMagic);

        // Flags
        byte[] flagsBytes = this.util.shortToBytes((short) this.flags.getRawValue());
        out.write(flagsBytes);

        // ID
        byte[] idBytes = this.util.intToBytes(id);
        out.write(idBytes);

        // Payload Length
        byte[] payloadLengthBytes = this.util.shortToBytes((short) payload.length);
        out.write(payloadLengthBytes);

        // CRC32 (Flags + ID + Payload Length)
        CRC32 headerCrc = new CRC32();
        headerCrc.update(flagsBytes);
        headerCrc.update(idBytes);
        headerCrc.update(payloadLengthBytes);
        out.write(this.util.intToBytes((int) headerCrc.getValue()));

        // CRC32 (Body)
        CRC32 bodyCrc = new CRC32();
        bodyCrc.update(payload);
        out.write(this.util.intToBytes((int) bodyCrc.getValue()));

        // Payload
        out.write(payload);
    }

    /**
     * @return <IO Flags, Packet ID, Packet Body>
     */
    public Triple<Flags, Integer, byte[]> deserialize(InputStream in) throws IOException {
        if (!in.markSupported()) throw new IOException("InputStream#mark is unsupported, please pass in a buffered input stream to fix this.");

        int succeedingPosition = 0;
        while (true) {
            int read = in.read();

            if (read == -1) {
                throw new IOException("End of stream reached whilst searching for packet.");
            }

            byte expectedMagic = headerMagic[succeedingPosition];

            if (expectedMagic == read) {
                succeedingPosition++;

                if (succeedingPosition == headerMagic.length) {
                    try {
                        this.logger.debug("Found start of packet!");
                        Triple<Flags, Integer, byte[]> result = deserializePacket(in);

                        if (result == null) {
                            succeedingPosition = 0; // Corrupt packet, restart the search.
                        } else {
                            return result;
                        }
                    } catch (IOException e) {
                        in.reset(); // Important.
                        throw e;
                    }
                }
            } else {
                this.logger.debug("Search failed, whatever.");
                succeedingPosition = 0; // Restart the search.
            }
        }
    }

    private Triple<Flags, Integer, byte[]> deserializePacket(InputStream in) throws IOException {
        // Header Magic has already been consumed irreversibly, but we still want to be
        // able to pick up where we left off.
        in.mark(bodyMaxLength + headerLength - headerMagic.length);

        byte[] flagsBytes = guaranteedRead(2, in);
        Flags flags = new Flags(this.util.bytesToShort(flagsBytes));
        this.logger.trace("flags=%s", String.format("%16s", Integer.toBinaryString(flags.getRawValue())).replace(' ', '0'));

        byte[] idBytes = guaranteedRead(4, in);
        int packetId = this.util.bytesToInt(idBytes);
        this.logger.trace("packetId=%d", packetId);

        byte[] payloadLengthBytes = guaranteedRead(2, in);
        int payloadLength = this.util.bytesToShort(payloadLengthBytes);
        this.logger.trace("payloadLength=%d", payloadLength);

        // Check the header CRC.
        byte[] headerCrcBytes = guaranteedRead(4, in);
        long headerCrc = Integer.toUnsignedLong(this.util.bytesToInt(headerCrcBytes));

        CRC32 computedHeaderCrc = new CRC32();
        computedHeaderCrc.update(flagsBytes);
        computedHeaderCrc.update(idBytes);
        computedHeaderCrc.update(payloadLengthBytes);

        long computedHeaderCrcValue = computedHeaderCrc.getValue();
        this.logger.debug("(Header) Read CRC: %d, Computed CRC: %d", headerCrc, computedHeaderCrcValue);

        if (headerCrc != computedHeaderCrcValue) {
            this.logger.severe("Corrupt packet received! (Header CRC failed)");
            in.reset(); // Important.
            return null;
        }

        // Body reading
        byte[] bodyCrcBytes = guaranteedRead(4, in);
        long bodyCrc = Integer.toUnsignedLong(this.util.bytesToInt(bodyCrcBytes));

        byte[] payload = guaranteedRead(payloadLength, in);

        // Check the body CRC.
        CRC32 computedBodyCrc = new CRC32();
        computedBodyCrc.update(payload);

        long computedBodyCrcValue = computedBodyCrc.getValue();
        this.logger.debug("(Body) Read CRC: %d, Computed CRC: %d", bodyCrc, computedBodyCrcValue);

        if (bodyCrc != computedBodyCrcValue) {
            if (flags.get(FLAG_IGNORE_BODY_CRC)) {
                this.logger.warn("Body CRC failed, continuing anyway (FLAG_IGNORE_BODY_CRC).");
            } else {
                this.logger.severe("Corrupt packet received! (Body CRC failed)");
                in.reset(); // Important.
                return null;
            }
        }

        // Skip over this packet, makes subsequent searches faster.
        in.reset();
        in.skipNBytes(headerLength - headerMagic.length + payloadLength);

        // Success
        this.logger.debug("Successfully decoded packet.");
        return new Triple<>(flags, packetId, payload);
    }

    public static byte[] guaranteedRead(int len, InputStream in) throws IOException {
        byte[] buf = new byte[len];
        int offset = 0;
        while (offset < len) {
            int read = in.read(buf, offset, len - offset);
            if (read == -1) {
                throw new IOException("End of stream reached whilst reading into buffer.");
            } else {
                offset += read;
            }
        }

        return buf;
    }

}
