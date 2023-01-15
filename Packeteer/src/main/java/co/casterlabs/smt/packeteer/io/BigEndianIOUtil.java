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

    /* -------- */
    /* flag     */
    /* -------- */

    public synchronized byte flagToBytes(boolean b0, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7) {
        byte value = 0;
        if (b0) value |= 1 << 0;
        if (b1) value |= 1 << 1;
        if (b2) value |= 1 << 2;
        if (b3) value |= 1 << 3;
        if (b4) value |= 1 << 4;
        if (b5) value |= 1 << 5;
        if (b6) value |= 1 << 6;
        if (b7) value |= 1 << 7;
        return value;
    }

    public synchronized boolean[] bytesToFlag(byte b) {
        return new boolean[] {
                (b & (0 << 0)) != 0,
                (b & (1 << 0)) != 0,
                (b & (2 << 0)) != 0,
                (b & (3 << 0)) != 0,
                (b & (4 << 0)) != 0,
                (b & (5 << 0)) != 0,
                (b & (6 << 0)) != 0,
                (b & (7 << 0)) != 0
        };
    }

}
