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

public class Test_PacketDeserializationTiming {
    private static PacketIO io = new PacketIO();

    public static void main(String[] args) throws IOException {
        io.getLogger().setCurrentLevel(LogLevel.ALL);

        byte[] bytes = getBytes();
        ByteArrayInputStream bains = new ByteArrayInputStream(bytes);

        long start = System.currentTimeMillis();
        try {
            while (true) {
                Triple<Flags, Integer, byte[]> result = io.deserialize(bains);
                int packetId = result.b();
                byte[] payload = result.c();

                if (packetId == 42) {
                    TestPacket2 test = new TestPacket2();
                    test.deserialize(payload);
                    FastLogger.logStatic(test.testNumber);
                } else {
                    FastLogger.logStatic("UNKNOWN PACKET ID: " + packetId);
                }
            }
        } catch (IOException e) {
            if (!e.getMessage().contains("End of stream")) {
                FastLogger.logException(e);
            }
        }
        long finish = System.currentTimeMillis();
        FastLogger.logStatic("Deserializing packet took %d ms.", finish - start);
    }

    private static byte[] getBytes() throws IOException {
        TestPacket2 test = new TestPacket2();
        test.testNumber = 123456;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        long start = System.currentTimeMillis();
        io.serialize(test, baos);
        long finish = System.currentTimeMillis();
        FastLogger.logStatic("Serializing packet took %d ms.", finish - start);

        return baos.toByteArray();
    }

}

class TestPacket2 extends Packet {
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
