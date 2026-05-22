package model.turn;

public enum ActivityType {
    ROOM("room"),
    SALE("sale"),
    ROOM_SWAP("roomSwap"),
    REFUND("refund"),
    SPENDING("spending"),
    EXTRA_CHANGE("extraChange");

    private final String value;

    ActivityType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ActivityType fromString(String s) {
        for (ActivityType t : values()) {
            if (t.value.equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown ActivityType: " + s);
    }
}
