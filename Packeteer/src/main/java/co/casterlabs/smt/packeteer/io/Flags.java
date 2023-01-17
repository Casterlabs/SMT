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
package co.casterlabs.smt.packeteer.io;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Flags {
    private @Getter int rawValue;

    /**
     * @return this instance, for chaining.
     */
    public Flags set(int flag, boolean value) {
        if (value) {
            this.rawValue |= 1 << flag;
        } else {
            this.rawValue &= ~(1 << flag);
        }
        return this;
    }

    public boolean get(int flag) {
        return (this.rawValue & (1 << 0)) != 0;
    }

    @Override
    public Flags clone() {
        return new Flags(this.rawValue);
    }

    @Override
    public String toString() {
        return this.toString(32);
    }

    public String toString(int digits) {
        if (digits < 0 || digits > 32) throw new IllegalArgumentException("toString(digits) must be within 0-32");
        return String
            .format("%" + digits + "s", Integer.toBinaryString(this.rawValue))
            .replace(' ', '0');
    }

}
