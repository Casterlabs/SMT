package co.casterlabs.smt.packeteer;

import java.io.IOException;

import co.casterlabs.smt.packeteer.io.PacketeerInput;
import co.casterlabs.smt.packeteer.io.PacketeerOutput;

public class TestPacket extends Packet {
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
