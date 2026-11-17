package model.turn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Direction of an inventory movement recorded during a turn.
 */
public enum MovementType {
    INCREASE("increase"),
    CONSUMPTION("consumption");

    private final String value;

    MovementType(String value) {
        this.value = value;
    }

    /**
     * @return the JSON string representation of this type
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * @param s the string to parse
     * @return the matching constant
     * @throws IllegalArgumentException when the string is unknown
     */
    @JsonCreator
    public static MovementType fromString(String s) {
        for (MovementType type : values()) {
            if (type.value.equals(s)) return type;
        }
        throw new IllegalArgumentException("Unknown MovementType: " + s);
    }
}
