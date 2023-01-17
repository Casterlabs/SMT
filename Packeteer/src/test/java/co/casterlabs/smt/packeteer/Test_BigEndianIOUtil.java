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
