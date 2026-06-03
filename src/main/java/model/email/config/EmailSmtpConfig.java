package model.email.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmailSmtpConfig(
        @JsonProperty("smtpHost") String smtpHost,
        @JsonProperty("smtpPort") int smtpPort,
        @JsonProperty("useStartTls") boolean useStartTls,
        @JsonProperty("useImplicitSsl") boolean useImplicitSsl,
        @JsonProperty("authMode") AuthMode authMode,
        @JsonProperty("connectionTimeoutMs") int connectionTimeoutMs) {

    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    public EmailSmtpConfig {
        if (smtpHost == null || smtpHost.isBlank()) {
            throw new IllegalArgumentException("smtpHost must not be blank");
        }
        if (smtpPort < MIN_PORT || smtpPort > MAX_PORT) {
            throw new IllegalArgumentException("smtpPort must be between " + MIN_PORT + " and " + MAX_PORT);
        }
        if (authMode == null) {
            authMode = AuthMode.NONE;
        }
        if (connectionTimeoutMs < 0) {
            connectionTimeoutMs = 5000;
        }
    }
}
