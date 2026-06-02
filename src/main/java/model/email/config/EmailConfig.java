package model.email.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Immutable email server configuration, Jackson-serializable.
 * The {@code username} and {@code credential} fields are marked {@link JsonIgnore}
 * and are never persisted to JSON — they are loaded at runtime from a
 * machine-bound encrypted file ({@code email-secure.dat}).
 *
 * <p>Provider presets (static factory methods):
 * <ul>
 *   <li>{@link #gmailAppPassword(String, String)} — Gmail with app password</li>
 *   <li>{@link #outlookPassword(String, String)} — Outlook/Office365 app password</li>
 *   <li>{@link #genericSmtp(String, int, String, String, boolean)} — custom SMTP</li>
 * </ul>
 */
public record EmailConfig(
        String smtpHost,
        int smtpPort,
        boolean useStartTls,
        boolean useImplicitSsl,
        AuthMode authMode,
        @JsonIgnore String username,
        @JsonIgnore String credential,
        int connectionTimeoutMs,
        Map<String, String> extraProperties) {

    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    @JsonCreator
    public EmailConfig(
            @JsonProperty("smtpHost") String smtpHost,
            @JsonProperty("smtpPort") int smtpPort,
            @JsonProperty("useStartTls") boolean useStartTls,
            @JsonProperty("useImplicitSsl") boolean useImplicitSsl,
            @JsonProperty("authMode") AuthMode authMode,
            @JsonProperty("connectionTimeoutMs") int connectionTimeoutMs,
            @JsonProperty("extraProperties") Map<String, String> extraProperties) {
        this(smtpHost, smtpPort, useStartTls, useImplicitSsl, authMode, "", null,
                connectionTimeoutMs, extraProperties);
    }

    // Compact constructor with validation
    public EmailConfig {
        Objects.requireNonNull(smtpHost, "smtpHost cannot be null");
        if (smtpHost.isBlank()) {
            throw new IllegalArgumentException("smtpHost cannot be blank");
        }
        if (smtpPort < MIN_PORT || smtpPort > MAX_PORT) {
            throw new IllegalArgumentException("smtpPort must be between " + MIN_PORT + " and " + MAX_PORT);
        }
        Objects.requireNonNull(authMode, "authMode cannot be null");
        Objects.requireNonNull(username, "username cannot be null");
        if (connectionTimeoutMs < 0) {
            connectionTimeoutMs = 5000;
        }
        if (extraProperties == null) {
            extraProperties = Map.of();
        } else {
            extraProperties = Collections.unmodifiableMap(new HashMap<>(extraProperties));
        }
    }

    /**
     * @return a new {@link Builder} with no preset values
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gmail app password preset: smtp.gmail.com:587, STARTTLS, PASSWORD mode.
     * Requires a <a href="https://myaccount.google.com/apppasswords">Google app password</a>
     * (16-character password generated when 2-Step Verification is enabled).
     */
    public static EmailConfig gmailAppPassword(String email, String appPassword) {
        Objects.requireNonNull(email, "email cannot be null");
        Objects.requireNonNull(appPassword, "appPassword cannot be null");
        return new EmailConfig("smtp.gmail.com", 587, true, false, AuthMode.PASSWORD,
                email, appPassword, 5000, Map.of("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3"));
    }

    /**
     * Outlook / Office365 app password preset: smtp.office365.com:587, STARTTLS, PASSWORD mode.
     */
    public static EmailConfig outlookPassword(String email, String appPassword) {
        Objects.requireNonNull(email, "email cannot be null");
        Objects.requireNonNull(appPassword, "appPassword cannot be null");
        return new EmailConfig("smtp.office365.com", 587, true, false, AuthMode.PASSWORD,
                email, appPassword, 5000, Map.of("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3"));
    }

    /**
     * Generic SMTP preset with fully customizable host, port, and STARTTLS setting.
     */
    public static EmailConfig genericSmtp(String host, int port, String user, String pass, boolean startTls) {
        Objects.requireNonNull(host, "host cannot be null");
        Objects.requireNonNull(user, "user cannot be null");
        Objects.requireNonNull(pass, "pass cannot be null");
        return new EmailConfig(host, port, startTls, false, AuthMode.PASSWORD,
                user, pass, 5000, Map.of("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3"));
    }

    /**
     * Converts this config to Jakarta Mail {@link Properties}.
     * Includes SMTP settings, TLS enforcement, and timeout values.
     */
    public Properties toProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", authMode != AuthMode.NONE ? "true" : "false");
        props.put("mail.smtp.starttls.enable", String.valueOf(useStartTls));
        if (useImplicitSsl) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", String.valueOf(connectionTimeoutMs));
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.mime.charset", "UTF-8");

        for (Map.Entry<String, String> entry : extraProperties.entrySet()) {
            props.put(entry.getKey(), entry.getValue());
        }
        return props;
    }

    /**
     * Fluent builder for {@link EmailConfig}.
     */
    public static final class Builder {
        private String smtpHost;
        private int smtpPort = 587;
        private boolean useStartTls = true;
        private boolean useImplicitSsl;
        private AuthMode authMode = AuthMode.NONE;
        private String username = "";
        private String credential;
        private int connectionTimeoutMs = 5000;
        private Map<String, String> extraProperties = new HashMap<>();

        public Builder smtpHost(String val)             { this.smtpHost = val; return this; }
        public Builder smtpPort(int val)                { this.smtpPort = val; return this; }
        public Builder useStartTls(boolean val)         { this.useStartTls = val; return this; }
        public Builder useImplicitSsl(boolean val)      { this.useImplicitSsl = val; return this; }
        public Builder authMode(AuthMode val)           { this.authMode = val; return this; }
        public Builder username(String val)             { this.username = val; return this; }
        public Builder credential(String val)           { this.credential = val; return this; }
        public Builder connectionTimeoutMs(int val)     { this.connectionTimeoutMs = val; return this; }
        public Builder extraProperty(String key, String val) {
            this.extraProperties.put(key, val); return this;
        }

        public EmailConfig build() {
            return new EmailConfig(smtpHost, smtpPort, useStartTls, useImplicitSsl, authMode,
                    username, credential, connectionTimeoutMs,
                    Collections.unmodifiableMap(new HashMap<>(extraProperties)));
        }
    }
}
