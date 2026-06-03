package model.email.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmailSmtpConfig(
        @JsonProperty("smtpHost") String smtpHost,
        @JsonProperty("smtpPort") int smtpPort,
        @JsonProperty("useStartTls") boolean useStartTls,
        @JsonProperty("useImplicitSsl") boolean useImplicitSsl,
        @JsonProperty("authMode") AuthMode authMode,
        @JsonProperty("connectionTimeoutMs") int connectionTimeoutMs) {

    public EmailSmtpConfig {
        if (smtpHost == null || smtpHost.isBlank()) {
            throw new IllegalArgumentException("smtpHost must not be blank");
        }
        if (smtpPort < 1 || smtpPort > 65535) {
            throw new IllegalArgumentException("smtpPort must be between 1 and 65535");
        }
        if (authMode == null) {
            authMode = AuthMode.NONE;
        }
        if (connectionTimeoutMs < 0) {
            connectionTimeoutMs = 5000;
        }
    }
}
