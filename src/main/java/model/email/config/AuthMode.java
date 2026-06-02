package model.email.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Email authentication modes supported by the email module.
 */
public enum AuthMode {
    NONE("none"),
    PASSWORD("password");

    private final String value;

    AuthMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AuthMode fromString(String value) {
        for (AuthMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown auth mode: " + value);
    }
}
