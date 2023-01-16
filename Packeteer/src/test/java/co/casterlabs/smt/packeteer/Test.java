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
import java.util.concurrent.ThreadLocalRandom;

import co.casterlabs.commons.functional.tuples.Triple;
import co.casterlabs.smt.packeteer.io.PacketIO;
import co.casterlabs.smt.packeteer.io.Flags;

public class Test {

    // Should
    // - Unknown Packet ID
    // - throw Corrupt Packet (Header)
    // - throw Corrupt Packet (Body)
    // - Deserialize: 123456
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        ByteArrayInputStream bains = new ByteArrayInputStream(getBytes());

        while (true) {
            try {
                Triple<Flags, Integer, byte[]> result = PacketIO.deserialize(bains);
                int packetId = result.b();
                byte[] payload = result.c();

                if (packetId == 42) {
                    TestPacket test = new TestPacket();
                    test.deserialize(payload);
                    System.out.println(test.testNumber);
                    long finish = System.currentTimeMillis();
                    System.out.printf("Took %d ms.\n", finish - start);
                    System.exit(0);
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
        byte[] testBytes = test.serializeToBytes();

        byte[] test2Bytes = test.serializeToBytes();
        test2Bytes[13]++;

        byte[] test3Bytes = test.serializeToBytes();
        test3Bytes[22]++;

        byte[] randomBytes = new byte[2048];
        ThreadLocalRandom.current().nextBytes(randomBytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(randomBytes);
        baos.write(randomBytes);
        PacketIO.serialize(new Flags(), 0, new byte[PacketIO.bodyMaxLength], baos);
        baos.write(randomBytes);
        baos.write(test2Bytes);
        baos.write(randomBytes);
        baos.write(randomBytes);
        baos.write(test3Bytes);
        baos.write(randomBytes);
        baos.write(testBytes);

        return baos.toByteArray();
    }

}
