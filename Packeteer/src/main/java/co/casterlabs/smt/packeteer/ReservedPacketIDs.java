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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Want to add something? Make a PR.
public class ReservedPacketIDs {
    public static final Map<String, Integer> VALUES;

    static {
        Map<String, Integer> map = new HashMap<>();
        VALUES = Collections.unmodifiableMap(map);

        try {
            final String IRB = "IRB_";

            for (Field f : ReservedPacketIDs.class.getFields()) {
                if (!f.getName().startsWith(IRB)) continue;
                if (f.getName().equals("IRB_MARKER")) continue;

                map.put(
                    f.getName().substring(IRB.length()),
                    f.getInt(null)
                );
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // Signifies that the current packet should be treated differently.
    public static final int IRB_MARKER = 1 << 31;

    public static final int IRB_AUDIO_TYPE = 1 | IRB_MARKER;
    public static final int IRB_VIDEO_TYPE = 2 | IRB_MARKER;
    public static final int IRB_CONTAINER_TYPE = 3 | IRB_MARKER;

}
