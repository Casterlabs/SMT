package co.casterlabs.smt.packeteer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.smt.packeteer.io.PacketeerInput;
import co.casterlabs.smt.packeteer.io.PacketeerOutput;

public abstract class Packet {

    protected abstract void readIn(@Nullable String extendedId, PacketeerInput in) throws IOException;

    protected abstract void writeOut(PacketeerOutput out) throws IOException;

    public abstract int getId();

    /**
     * @implNote The extendedId can only be a max of 255 characters long.
     */
    public @Nullable String getExtendedId() {
        return null;
    }

    public final byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.writeOut(new PacketeerOutput(baos));
        return baos.toByteArray();
    }

    public final void deserialize(@Nullable String extendedId, byte[] payload) throws IOException {
        this.readIn(extendedId, new PacketeerInput(new ByteArrayInputStream(payload)));
    }

}
