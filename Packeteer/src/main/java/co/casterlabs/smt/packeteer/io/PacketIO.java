/*
 * Copyright 2023 Casterlabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package co.casterlabs.smt.packeteer.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.smt.packeteer.Packet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
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

    private static final int headerLength = 0
        + 4   // Magic
        + 2   // Flags
        + 4   // ID
        + 255 // Extended ID (Null Terminated, UTF-8)
        + 8   // Timestamp
        + 2   // Payload Length
        + 4   // CRC32 (Flags + ID + Extended ID + Payload Length + Timestamp)
        + 4   // CRC32 (Payload)
    ;

    public static final int bodyMaxLength = Short.MAX_VALUE - headerLength; // Just shy of 32kb

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

    public void serialize(Packet packet, OutputStream out) throws IOException {
        this.serialize(packet.getId(), packet.getExtendedId(), packet.serialize(), System.currentTimeMillis(), out);
    }

    public void serialize(int id, @Nullable String extendedId, byte[] payload, long timestamp, OutputStream out) throws IOException {
        if (payload.length > bodyMaxLength) throw new IOException("Payload cannot be larger than " + bodyMaxLength);
        if ((extendedId != null) && extendedId.length() > 255) throw new IOException("Extended ID cannot be longer than 255 characters");

        // Magic
        out.write(headerMagic);

        // Flags
        byte[] flagsBytes = BigEndianIOUtil.shortToBytes((short) this.flags.getRawValue());
        out.write(flagsBytes);

        // ID
        byte[] idBytes = BigEndianIOUtil.intToBytes(id);
        out.write(idBytes);

        // Extended ID
        byte[] extendedIdBytes = new byte[255];
        if (extendedId != null) {
            byte[] utf8 = extendedId.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(utf8, 0, extendedIdBytes, 0, utf8.length); // Leaves the rest of the bytes `null`.
        }
        out.write(extendedIdBytes);

        // Timestamp
        byte[] timestampBytes = BigEndianIOUtil.longToBytes(timestamp);
        out.write(timestampBytes);

        // Payload Length
        byte[] payloadLengthBytes = BigEndianIOUtil.shortToBytes((short) payload.length);
        out.write(payloadLengthBytes);

        // CRC32 (Flags + ID + Payload Length + Timestamp)
        CRC32 headerCrc = new CRC32();
        headerCrc.update(flagsBytes);
        headerCrc.update(idBytes);
        headerCrc.update(extendedIdBytes);
        headerCrc.update(timestampBytes);
        headerCrc.update(payloadLengthBytes);
        out.write(BigEndianIOUtil.intToBytes((int) headerCrc.getValue()));

        // CRC32 (Body)
        CRC32 bodyCrc = new CRC32();
        bodyCrc.update(payload);
        out.write(BigEndianIOUtil.intToBytes((int) bodyCrc.getValue()));

        // Payload
        out.write(payload);
    }

    /**
     * @return <IO Flags, Packet ID, Packet Body>
     */
    public DeserializationResult deserialize(InputStream in) throws IOException {
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
                        DeserializationResult result = deserializePacket(in);

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

    private DeserializationResult deserializePacket(InputStream in) throws IOException {
        // Header Magic has already been consumed irreversibly, but we still want to be
        // able to pick up where we left off.
        in.mark(bodyMaxLength + headerLength - headerMagic.length);

        // Flags
        byte[] flagsBytes = guaranteedRead(2, in);
        Flags flags = new Flags(BigEndianIOUtil.bytesToShort(flagsBytes));
        this.logger.trace("flags=%s", flags.toString(16));

        // ID
        byte[] idBytes = guaranteedRead(4, in);
        int packetId = BigEndianIOUtil.bytesToInt(idBytes);
        this.logger.trace("packetId=%d", packetId);

        // Extended ID
        byte[] extendedIdBytes = guaranteedRead(255, in);
        String extendedId = null;
        {
            int actualLength = 0;
            for (byte b : extendedIdBytes) {
                if (b == 0) {
                    break;
                }
                actualLength++;
            }

            if (actualLength > 0) {
                byte[] actualBytes = new byte[actualLength];
                System.arraycopy(extendedIdBytes, 0, actualBytes, 0, actualLength);
                extendedId = new String(actualBytes, StandardCharsets.UTF_8);
            }
        }

        // Timestamp
        byte[] timestampBytes = guaranteedRead(8, in);
        long timestamp = BigEndianIOUtil.bytesToLong(timestampBytes);
        this.logger.trace("timestamp=%d", timestamp);

        // Payload Length
        byte[] payloadLengthBytes = guaranteedRead(2, in);
        short payloadLength = BigEndianIOUtil.bytesToShort(payloadLengthBytes);
        this.logger.trace("payloadLength=%d", payloadLength);

        // Check the header CRC.
        byte[] headerCrcBytes = guaranteedRead(4, in);
        long headerCrc = Integer.toUnsignedLong(BigEndianIOUtil.bytesToInt(headerCrcBytes));

        CRC32 computedHeaderCrc = new CRC32();
        computedHeaderCrc.update(flagsBytes);
        computedHeaderCrc.update(idBytes);
        computedHeaderCrc.update(extendedIdBytes);
        computedHeaderCrc.update(timestampBytes);
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
        long bodyCrc = Integer.toUnsignedLong(BigEndianIOUtil.bytesToInt(bodyCrcBytes));

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
        return new DeserializationResult(
            flags,
            packetId,
            extendedId,
            timestamp,
            payload
        );
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

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeserializationResult {
        public final Flags flags;
        public final int packetId;
        public final String extendedId;
        public final long timestamp;

        public final @ToString.Exclude byte[] payload;

        @Override
        public String toString() {
            return String.format(
                "PacketIO.DeserializationResult(flags=%s, packetId=%d, extendedId=%s, timestamp=%d, payloadLength=%d)",
                this.flags.toString(16),
                this.packetId,
                this.extendedId,
                this.timestamp,
                this.payload.length
            );
        }

    }

}
