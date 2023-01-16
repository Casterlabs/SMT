/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.smt.packeteer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import co.casterlabs.commons.functional.tuples.Triple;
import co.casterlabs.smt.packeteer.io.Flags;
import co.casterlabs.smt.packeteer.io.PacketIO;
import co.casterlabs.smt.packeteer.io.PacketeerInput;
import co.casterlabs.smt.packeteer.io.PacketeerOutput;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Test_PacketDeserialization {
    private static PacketIO io = new PacketIO();

    public static void main(String[] args) throws IOException {
        io.getLogger().setCurrentLevel(LogLevel.ALL);

        long start = System.currentTimeMillis();
        byte[] bytes = getBytes();

        bytes[18]++; // Corrupt the first packet.

        ByteArrayInputStream bains = new ByteArrayInputStream(bytes);

        try {
            while (true) {
                Triple<Flags, Integer, byte[]> result = io.deserialize(bains);
                int packetId = result.b();
                byte[] payload = result.c();

                if (packetId == 42) {
                    TestPacket test = new TestPacket();
                    test.deserialize(payload);
                    FastLogger.logStatic(test.testNumber);
                    long finish = System.currentTimeMillis();
                    FastLogger.logStatic("Took %d ms.", finish - start);
                } else {
                    FastLogger.logStatic("UNKNOWN PACKET ID: " + packetId);
                }
            }
        } catch (IOException e) {
            FastLogger.logException(e);
        }
    }

    private static byte[] getBytes() throws IOException {
        TestPacket test = new TestPacket();
        test.testNumber = 123456;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        io.serialize(test, baos);
        io.serialize(test, baos);

        return baos.toByteArray();
    }

}

class TestPacket extends Packet {
    public int testNumber;

    @Override
    protected void readIn(PacketeerInput in) throws IOException {
        this.testNumber = in.readInt();
    }

    @Override
    protected void writeOut(PacketeerOutput out) throws IOException {
        out.writeInt(this.testNumber);
    }

    @Override
    public int getId() {
        return 42;
    }

}
