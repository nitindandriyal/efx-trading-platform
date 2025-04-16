package pub.lab.trading.model;

import org.agrona.collections.Int2ObjectHashMap;

public enum Tenor {
    SPOT(0),
    TOM(1),
    TODAY(2),
    ONE_WEEK(7),
    TWO_WEEKS(14),
    ONE_MONTH(30),
    THREE_MONTHS(90),
    SIX_MONTHS(180),
    ONE_YEAR(365);

    private final int code;

    Tenor(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    private static final Int2ObjectHashMap<Tenor> BY_DAYS = new Int2ObjectHashMap<>();

    static {
        for (Tenor t : values()) {
            BY_DAYS.put(t.code, t);
        }
    }

    public static Tenor fromCode(int code) {
        return BY_DAYS.get(code);
    }
}
