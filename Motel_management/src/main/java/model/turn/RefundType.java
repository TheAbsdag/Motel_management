package model.turn;

public enum RefundType {
    ROOM_REFUND("roomRefund"),
    SALE_REFUND("saleRefund");

    private final String value;

    RefundType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RefundType fromString(String s) {
        for (RefundType t : values()) {
            if (t.value.equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown RefundType: " + s);
    }
}
