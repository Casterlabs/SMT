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
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PacketeerInput {
    private final InputStream stream;

    public byte readByte() throws IOException {
        return (byte) this.stream.read();
    }

    public byte[] readBytes() throws IOException {
        int len = this.readInt();

        return PacketIO.guaranteedRead(len, this.stream);
    }

    public int available() throws IOException {
        return this.stream.available();
    }

    public String readString() throws IOException {
        return new String(this.readBytes(), StandardCharsets.UTF_8);
    }

    public void readNull() throws IOException {
        this.readByte(); // Discard.
    }

    public long readLong() throws IOException {
        byte[] bytes = PacketIO.guaranteedRead(8, this.stream);

        return BigEndianIOUtil.bytesToLong(bytes);
    }

    public int readInt() throws IOException {
        byte[] bytes = PacketIO.guaranteedRead(4, this.stream);

        return BigEndianIOUtil.bytesToInt(bytes);
    }

    public short readShort() throws IOException {
        byte[] bytes = PacketIO.guaranteedRead(2, this.stream);

        return BigEndianIOUtil.bytesToShort(bytes);
    }

    public boolean readBoolean() throws IOException {
        byte value = this.readByte();

        // 0 = false
        // anything else = true
        return value != 0;
    }

    public double readDouble() throws IOException {
        long longBits = this.readLong();
        return Double.longBitsToDouble(longBits);
    }

    public float readFloat() throws IOException {
        int intBits = this.readInt();
        return Float.intBitsToFloat(intBits);
    }

}
