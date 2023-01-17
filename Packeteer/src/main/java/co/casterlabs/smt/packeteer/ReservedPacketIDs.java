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

        try {
            final String IRB = "IRB_";

            for (Field f : ReservedPacketIDs.class.getFields()) {
                if (!f.getName().startsWith(IRB)) continue;

                map.put(
                    f.getName().substring(IRB.length()),
                    f.getInt(null)
                );
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        VALUES = Collections.unmodifiableMap(map);
    }

    // Signifies that the current packet should be treated differently.
    public static final int IRB_MARKER = 1 << 31;

    public static final int IRB_AUDIO_TYPE = 1 | IRB_MARKER;
    public static final int IRB_VIDEO_TYPE = 2 | IRB_MARKER;
    public static final int IRB_CONTAINER_TYPE = 3 | IRB_MARKER;

}
