package co.casterlabs.smt.packeteer;

import java.lang.reflect.Field;

import co.casterlabs.smt.packeteer.io.BigEndianIOUtil;

public class Test_ListReservedPacketIDs {

    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
        for (Field f : ReservedPacketIDs.class.getFields()) {
            print(f.getName(), f.getInt(null));
        }
    }

    private static void print(String name, int value) {
        byte[] bytes = new BigEndianIOUtil().intToBytes(value);

        System.out.printf("%-24s: ", name);
        for (byte b : bytes) {
            System.out.print(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(b))).replace(' ', '0') + ' ');
        }
        System.out.println();
    }

}
