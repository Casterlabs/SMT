package co.casterlabs.smt.packeteer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.smt.packeteer.io.PacketIO;
import co.casterlabs.smt.packeteer.io.PacketeerInput;
import co.casterlabs.smt.packeteer.io.PacketeerOutput;

public abstract class Packet {

    protected abstract void readIn(PacketeerInput in) throws IOException;

    protected abstract void writeOut(PacketeerOutput out) throws IOException;

    public abstract int getId();

    public final void serialize(OutputStream out) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.writeOut(new PacketeerOutput(stream));

        PacketIO.serialize(this.getId(), stream.toByteArray(), out);
    }

    public final byte[] serializeToBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.serialize(baos);

        return baos.toByteArray();
    }

    public final void deserialize(byte[] payload) throws IOException {
        this.readIn(new PacketeerInput(new ByteArrayInputStream(payload)));
    }

}
