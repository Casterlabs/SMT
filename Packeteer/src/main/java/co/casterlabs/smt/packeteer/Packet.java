package co.casterlabs.smt.packeteer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import co.casterlabs.smt.packeteer.io.PacketeerInput;
import co.casterlabs.smt.packeteer.io.PacketeerOutput;

public abstract class Packet {

    protected abstract void readIn(PacketeerInput in) throws IOException;

    protected abstract void writeOut(PacketeerOutput out) throws IOException;

    public abstract int getId();

    public final byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.writeOut(new PacketeerOutput(baos));
        return baos.toByteArray();
    }

    public final void deserialize(byte[] payload) throws IOException {
        this.readIn(new PacketeerInput(new ByteArrayInputStream(payload)));
    }

}
