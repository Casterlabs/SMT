package co.casterlabs.smt.packeteer;

import java.util.Map;

import co.casterlabs.smt.packeteer.io.BigEndianIOUtil;

public class Test_ListReservedPacketIDs {

    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
        for (Map.Entry<String, Integer> e : ReservedPacketIDs.VALUES.entrySet()) {
            print(e.getKey(), e.getValue());
        }
    }

    private static void print(String name, int value) {
        byte[] bytes = BigEndianIOUtil.intToBytes(value);

        System.out.printf("%-24s: ", name);
        for (byte b : bytes) {
            System.out.print(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(b))).replace(' ', '0') + ' ');
        }
        System.out.println();
    }

}
