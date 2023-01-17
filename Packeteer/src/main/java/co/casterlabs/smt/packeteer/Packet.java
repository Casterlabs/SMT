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
