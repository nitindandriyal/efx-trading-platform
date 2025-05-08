package pub.lab.trading.common.config;

import org.agrona.collections.Int2ObjectHashMap;

public enum StreamId {
    RAW_QUOTE(100),
    MARKET_QUOTE(200),
    CLIENT_QUOTE(300),
    HEARTBEAT(800),
    CONFIG_STREAM(900);

    private static final Int2ObjectHashMap<StreamId> MAP = new Int2ObjectHashMap<>();

    static {
        for (StreamId t : values()) {
            MAP.put(t.code, t);
        }
    }

    private final int code;

    StreamId(int code) {
        this.code = code;
    }

    public static StreamId fromCode(int code) {
        return MAP.get(code);
    }

    public int getCode() {
        return code;
    }
}
