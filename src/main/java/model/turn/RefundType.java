package model.turn;

/**
 * Categorizes the type of refund processed during a turn.
 *
 * <p>{@link #ROOM_REFUND} applies to room booking cancellations or adjustments.
 * {@link #SALE_REFUND} applies to previously sold items that were returned.
 */
public enum RefundType {
    ROOM_REFUND("roomRefund"),
    SALE_REFUND("saleRefund");

    private final String value;

    RefundType(String value) {
        this.value = value;
    }

    /**
     * Returns the JSON string representation of this type.
     *
     * @return {@code "roomRefund"} or {@code "saleRefund"}
     */
    public String getValue() {
        return value;
    }

    /**
     * Parses a {@code RefundType} from its string representation.
     *
     * @param s the string to parse (case-sensitive)
     * @return the matching enum constant
     * @throws IllegalArgumentException if no match is found
     */
    public static RefundType fromString(String s) {
        for (RefundType t : values()) {
            if (t.value.equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown RefundType: " + s);
    }
}
