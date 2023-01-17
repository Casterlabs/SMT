package co.casterlabs.smt.packeteer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.smt.packeteer.io.PacketIO;
import co.casterlabs.smt.packeteer.io.PacketIO.DeserializationResult;
import co.casterlabs.smt.packeteer.io.PacketeerInput;
import co.casterlabs.smt.packeteer.io.PacketeerOutput;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Test_PacketDeserialization {
    private static PacketIO io = new PacketIO();

    public static void main(String[] args) throws IOException {
        io.getLogger().setCurrentLevel(LogLevel.ALL);

        byte[] bytes = getBytes();
        bytes[18]++; // Corrupt the first packet.

        ByteArrayInputStream bains = new ByteArrayInputStream(bytes);

        try {
            while (true) {
                DeserializationResult result = io.deserialize(bains);

                if (result.packetId == 42) {
                    TestPacket test = new TestPacket();
                    test.deserialize(result.extendedId, result.payload);
                    FastLogger.logStatic(test.testNumber);
                } else {
                    FastLogger.logStatic("UNKNOWN PACKET ID: " + result.packetId);
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
    protected void readIn(@Nullable String extendedId, PacketeerInput in) throws IOException {
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
