/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.smt.packeteer;

import java.io.IOException;

import co.casterlabs.smt.packeteer.io.BigEndianIOUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Test_BigEndianIOUtil {

    public static void main(String[] args) throws IOException {
        FastLogger.logStatic("%d -> %d", Long.MAX_VALUE, BigEndianIOUtil.bytesToLong(BigEndianIOUtil.longToBytes(Long.MAX_VALUE)));
        FastLogger.logStatic("%d -> %d", Long.MIN_VALUE, BigEndianIOUtil.bytesToLong(BigEndianIOUtil.longToBytes(Long.MIN_VALUE)));

        FastLogger.logStatic("%d -> %d", Integer.MAX_VALUE, BigEndianIOUtil.bytesToInt(BigEndianIOUtil.intToBytes(Integer.MAX_VALUE)));
        FastLogger.logStatic("%d -> %d", Integer.MIN_VALUE, BigEndianIOUtil.bytesToInt(BigEndianIOUtil.intToBytes(Integer.MIN_VALUE)));

        FastLogger.logStatic("%d -> %d", Short.MAX_VALUE, BigEndianIOUtil.bytesToShort(BigEndianIOUtil.shortToBytes(Short.MAX_VALUE)));
        FastLogger.logStatic("%d -> %d", Short.MIN_VALUE, BigEndianIOUtil.bytesToShort(BigEndianIOUtil.shortToBytes(Short.MIN_VALUE)));
    }

}
