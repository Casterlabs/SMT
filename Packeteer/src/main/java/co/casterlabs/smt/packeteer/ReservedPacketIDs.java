package co.casterlabs.smt.packeteer;

// Want to add something? Make a PR.
public class ReservedPacketIDs {
    // @formatter:off
    
    // Signifies that the current packet should be treated differently. The rest of
    // the upper 8 bits form the "Reserved Type" which a hint of what a packet
    // contains (e.g Audio or Video).
    public static final int ID_RESERVED_BIT = 1 << 31;

    // Audio Codecs
    public static final int IRB_AUDIO_TYPE = (1 << 24) | ID_RESERVED_BIT;
    public static final int IRB_AUDIO__OPUS =  1 | IRB_AUDIO_TYPE;
    public static final int IRB_AUDIO__AAC  =  2 | IRB_AUDIO_TYPE;

    // Video Codecs
    public static final int IRB_VIDEO_TYPE = (2 << 24) | ID_RESERVED_BIT;
    public static final int IRB_VIDEO__RGB8  =  1 | IRB_VIDEO_TYPE;
    public static final int IRB_VIDEO__RGB10 =  2 | IRB_VIDEO_TYPE;
    public static final int IRB_VIDEO__AVC   = 20 | IRB_VIDEO_TYPE; // aka: h264, MPEG-4 Part 10
    public static final int IRB_VIDEO__VP8   = 21 | IRB_VIDEO_TYPE; // aka: VCB
    public static final int IRB_VIDEO__HEVC  = 22 | IRB_VIDEO_TYPE; // aka: h265, MPEG-H Part 2
    public static final int IRB_VIDEO__VP9   = 23 | IRB_VIDEO_TYPE;
    public static final int IRB_VIDEO__AV1   = 24 | IRB_VIDEO_TYPE;

    // Containers
    public static final int IRB_CONTAINER_TYPE = (3 << 24) | ID_RESERVED_BIT;
    public static final int IRB_CONTAINER__MKV    =  1 | IRB_CONTAINER_TYPE;
    public static final int IRB_CONTAINER__WEBM   =  2 | IRB_CONTAINER_TYPE;
    public static final int IRB_CONTAINER__MP4    =  3 | IRB_CONTAINER_TYPE;
    public static final int IRB_CONTAINER__MPEGTS =  4 | IRB_CONTAINER_TYPE;
    public static final int IRB_CONTAINER__OGG    =  5 | IRB_CONTAINER_TYPE;

    // @formatter:on
}
