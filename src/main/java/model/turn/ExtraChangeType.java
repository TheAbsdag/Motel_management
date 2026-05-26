package model.turn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Categorizes financial extra changes that are not part of regular operations.
 *
 * <p>{@link #BANK_TRANSFER} represents money transferred to a bank account.
 * {@link #SAFE_DEPOSIT} represents money deposited into the establishment's safe.
 */
public enum ExtraChangeType {
    BANK_TRANSFER("bankTransfer"),
    SAFE_DEPOSIT("safeDeposit");

    private final String value;

    ExtraChangeType(String value) {
        this.value = value;
    }

    /**
     * Returns the JSON string representation of this type.
     *
     * @return {@code "bankTransfer"} or {@code "safeDeposit"}
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Parses an {@code ExtraChangeType} from its string representation.
     *
     * @param s the string to parse (case-sensitive)
     * @return the matching enum constant
     * @throws IllegalArgumentException if no match is found
     */
    @JsonCreator
    public static ExtraChangeType fromString(String s) {
        for (ExtraChangeType t : values()) {
            if (t.value.equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown ExtraChangeType: " + s);
    }
}
