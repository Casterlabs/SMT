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

public class Test_PacketDeserialization {
    private static PacketIO io = new PacketIO();

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        ByteArrayInputStream bains = new ByteArrayInputStream(getBytes());

        while (true) {
            try {
                Triple<Flags, Integer, byte[]> result = io.deserialize(bains);
                int packetId = result.b();
                byte[] payload = result.c();

                if (packetId == 42) {
                    TestPacket test = new TestPacket();
                    test.deserialize(payload);
                    System.out.println(test.testNumber);
                    long finish = System.currentTimeMillis();
                    System.out.printf("Took %d ms.\n", finish - start);
                    System.exit(0); // Will otherwise hang forever.
                } else {
                    System.out.println("UNKNOWN PACKET ID: " + packetId);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static byte[] getBytes() throws IOException {
        TestPacket test = new TestPacket();
        test.testNumber = 123456;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
