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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BigEndianIOUtil {
    private ByteBuffer buf = ByteBuffer.allocate(8);

    public BigEndianIOUtil() {
        this.buf.order(ByteOrder.BIG_ENDIAN);
    }

    /* -------- */
    /* Long     */
    /* -------- */

    public synchronized byte[] longToBytes(long v) {
        byte[] result = new byte[8];

        this.buf.putLong(0, v);
        this.buf.get(0, result);

        return result;
    }

    public synchronized long bytesToLong(byte[] b) {
        return this.buf
            .put(0, b)
            .getLong(0);
    }

    /* -------- */
    /* Int      */
    /* -------- */

    public synchronized byte[] intToBytes(int v) {
        byte[] result = new byte[4];

        this.buf.putInt(0, v);
        this.buf.get(0, result);

        return result;
    }

    public synchronized int bytesToInt(byte[] b) {
        return this.buf
            .put(0, b)
            .getInt(0);
    }

    /* -------- */
    /* Short    */
    /* -------- */

    public synchronized byte[] shortToBytes(short v) {
        byte[] result = new byte[2];

        this.buf.putShort(0, v);
        this.buf.get(0, result);

        return result;
    }

    public synchronized short bytesToShort(byte[] b) {
        return this.buf
            .put(0, b)
            .getShort(0);
    }

}
