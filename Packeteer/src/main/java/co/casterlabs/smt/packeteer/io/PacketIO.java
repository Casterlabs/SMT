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

    public static final int FLAG_UNRELIABLE = 0;
    public static final int FLAG_IGNORE_BODY_CRC = 1;

    @Getter
    @Setter
    @NonNull
    private Flags flags = new Flags();

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
            byte expectedMagic = headerMagic[succeedingPosition];

            if (expectedMagic == read) {
                succeedingPosition++;

                if (succeedingPosition == headerMagic.length) {
                    // We've found the start of a packet!
                    return deserializePacket(in);
                }
            } else {
                succeedingPosition = 0; // Restart the search.
            }
        }
    }

    private Triple<Flags, Integer, byte[]> deserializePacket(InputStream in) throws IOException {
        try {
            // Header Magic has already been consumed irreversibly, but we still want to be
            // able to pick up where we left off.
            in.mark(bodyMaxLength + headerLength - headerMagic.length);

            byte[] flagsBytes = guaranteedRead(2, in);
            byte[] idBytes = guaranteedRead(4, in);
            byte[] payloadLengthBytes = guaranteedRead(2, in);

            Flags flags = new Flags(this.util.bytesToShort(flagsBytes));

            // Check the header CRC.
            byte[] headerCrcBytes = guaranteedRead(4, in);
            long headerCrc = Integer.toUnsignedLong(this.util.bytesToInt(headerCrcBytes));

            CRC32 computedHeaderCrc = new CRC32();
            computedHeaderCrc.update(flagsBytes);
            computedHeaderCrc.update(idBytes);
            computedHeaderCrc.update(payloadLengthBytes);

            if (headerCrc != computedHeaderCrc.getValue()) {
                throw new IOException("Corrupt packet (Header CRC failed).");
            }

            // Body reading
            byte[] bodyCrcBytes = guaranteedRead(4, in);
            long bodyCrc = Integer.toUnsignedLong(this.util.bytesToInt(bodyCrcBytes));

            int payloadLength = this.util.bytesToShort(payloadLengthBytes);

            byte[] payload = guaranteedRead(payloadLength, in);

            // Checksum

            CRC32 computedBodyCrc = new CRC32();
            computedBodyCrc.update(payload);

            if (bodyCrc != computedBodyCrc.getValue()) {
                throw new IOException("Corrupt packet (Body CRC failed).");
            }

            // Success
            int packetId = this.util.bytesToInt(idBytes);
            return new Triple<>(flags, packetId, payload);
        } finally {
            in.reset();
        }
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
