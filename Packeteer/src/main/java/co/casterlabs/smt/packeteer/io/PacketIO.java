package co.casterlabs.smt.packeteer.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

import co.casterlabs.commons.functional.tuples.Pair;

public class PacketIO {

    private static final byte headerLength = 0
        + 5 // Magic
        + 1 // Flags
        + 4 // ID
        + 2 // Payload Length
        + 4 // CRC32 (Flags + ID + Payload Length)
        + 4 // CRC32 (Payload)
    ;

    private static final byte[] headerMagic = {
            0,
            0,
            'S',
            'M',
            'T'
    };

    public static final int bodyMaxLength = 32768 - headerLength; // Just shy of 32kb

    public static void serialize(int id, byte[] payload, OutputStream out) throws IOException {
        if (payload.length > bodyMaxLength) throw new IOException("Payload cannot be larger than " + bodyMaxLength);

        BigEndianIOUtil util = new BigEndianIOUtil();

        // Magic
        out.write(headerMagic);

        // Flags
        byte flags = util.flagToBytes(false, false, false, false, false, false, false, false);
        out.write(flags);

        // ID
        byte[] idBytes = util.intToBytes(id);
        out.write(idBytes);

        // Payload Length
        byte[] payloadLengthBytes = util.shortToBytes((short) payload.length);
        out.write(payloadLengthBytes);

        // CRC32 (Flags + ID + Payload Length)
        CRC32 headerCrc = new CRC32();
        headerCrc.update(flags);
        headerCrc.update(idBytes);
        headerCrc.update(payloadLengthBytes);
        out.write(util.intToBytes((int) headerCrc.getValue()));

        // CRC32 (Body)
        CRC32 bodyCrc = new CRC32();
        bodyCrc.update(payload);
        out.write(util.intToBytes((int) bodyCrc.getValue()));

        // Payload
        out.write(payload);
    }

    /**
     * @return <Packet ID, Packet Body>
     */
    public static Pair<Integer, byte[]> deserialize(InputStream in) throws IOException {
        if (!in.markSupported()) throw new IOException("InputStream#mark is unsupported, please pass in a buffered input stream to fix this.");

        int succeedingPosition = 0;
        while (true) {
            int read = in.read();
            byte expectedMagic = headerMagic[succeedingPosition];

            if (expectedMagic == read) {
                succeedingPosition++;

                if (succeedingPosition == headerMagic.length) {
                    // We've found the start of a packet!
                    return continueDeserialization(in);
                }
            } else {
                succeedingPosition = 0; // Restart the search.
            }
        }
    }

    private static Pair<Integer, byte[]> continueDeserialization(InputStream in) throws IOException {
        try {
            BigEndianIOUtil util = new BigEndianIOUtil();

            // Header Magic has already been consumed irreversibly, but we still want to be
            // able to pick up where we left off.
            in.mark(bodyMaxLength + headerLength - headerMagic.length);

            byte flags = (byte) in.read();
            byte[] idBytes = guaranteedRead(4, in);
            byte[] payloadLengthBytes = guaranteedRead(2, in);

            // Check the header CRC.
            byte[] headerCrcBytes = guaranteedRead(4, in);
            long headerCrc = Integer.toUnsignedLong(util.bytesToInt(headerCrcBytes));

            CRC32 computedHeaderCrc = new CRC32();
            computedHeaderCrc.update(flags);
            computedHeaderCrc.update(idBytes);
            computedHeaderCrc.update(payloadLengthBytes);

            if (headerCrc != computedHeaderCrc.getValue()) {
                throw new IOException("Corrupt packet (Header CRC failed).");
            }

            // Body reading
            byte[] bodyCrcBytes = guaranteedRead(4, in);
            long bodyCrc = Integer.toUnsignedLong(util.bytesToInt(bodyCrcBytes));

            int payloadLength = util.bytesToShort(payloadLengthBytes);

            byte[] payload = guaranteedRead(payloadLength, in);

            // Checksum

            CRC32 computedBodyCrc = new CRC32();
            computedBodyCrc.update(payload);

            if (bodyCrc != computedBodyCrc.getValue()) {
                throw new IOException("Corrupt packet (Body CRC failed).");
            }

            // Success
            int packetId = util.bytesToInt(idBytes);
            return new Pair<>(packetId, payload);
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
