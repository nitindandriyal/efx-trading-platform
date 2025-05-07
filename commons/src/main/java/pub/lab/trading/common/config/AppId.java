package pub.lab.trading.common.config;

import org.agrona.collections.Int2ObjectHashMap;

public enum AppId {
    AERON_MEDIA_DRIVER(0),
    CONFIG_SERVICE(1),
    QUOTING_ENGINE(2),
    PRICING_ENGINE(3),
    MARKET_DATA(4),
    STANDARD_ADAPTER(5);

    private final int code;

    AppId(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    private static final Int2ObjectHashMap<AppId> MAP = new Int2ObjectHashMap<>();

    static {
        for (AppId t : values()) {
            MAP.put(t.code, t);
        }
    }

    public static AppId fromCode(int code) {
        return MAP.get(code);
    }
}
