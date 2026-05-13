package model.turn;

public enum ExtraChangeType {
    BANK_TRANSFER("bankTransfer"),
    SAFE_DEPOSIT("safeDeposit");

    private final String value;

    ExtraChangeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExtraChangeType fromString(String s) {
        for (ExtraChangeType t : values()) {
            if (t.value.equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown ExtraChangeType: " + s);
    }
}
