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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PacketeerOutput {
    private final BigEndianIOUtil util = new BigEndianIOUtil();
    private final OutputStream stream;

    public PacketeerOutput writeByte(byte b) throws IOException {
        this.stream.write(b);
        return this;
    }

    public PacketeerOutput writeBytes(byte[] b) throws IOException {
        // SIZE + BYTES...
        this.writeInt(b.length);
        this.stream.write(b);
        return this;
    }

    public PacketeerOutput writeString(@NonNull String str) throws IOException {
        this.writeBytes(str.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public PacketeerOutput writeNull() throws IOException {
        this.writeByte((byte) 0);
        return this;
    }

    public PacketeerOutput writeLong(long l) throws IOException {
        this.stream.write(this.util.longToBytes(l));
        return this;
    }

    public PacketeerOutput writeInt(int i) throws IOException {
        this.stream.write(this.util.intToBytes(i));
        return this;
    }

    public PacketeerOutput writeShort(short s) throws IOException {
        this.stream.write(this.util.shortToBytes(s));
        return this;
    }

    public PacketeerOutput writeBoolean(boolean bl) throws IOException {
        byte value = (byte) (bl ? 1 : 0);

        this.writeByte(value);

        return this;
    }

    public PacketeerOutput writeDouble(double d) throws IOException {
        this.writeLong(Double.doubleToRawLongBits(d));
        return this;
    }

    public PacketeerOutput writeFloat(float f) throws IOException {
        this.writeInt(Float.floatToRawIntBits(f));
        return this;
    }

}
