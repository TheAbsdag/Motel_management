package model.email.config;

import java.util.Arrays;

public enum ProviderPreset {

    GMAIL("Gmail", "smtp.gmail.com", 587, true, false, true),
    OUTLOOK("Outlook", "smtp-mail.outlook.com", 587, true, false, true),
    YAHOO("Yahoo", "smtp.mail.yahoo.com", 587, true, false, true),
    CUSTOM("Personalizado", "", 0, true, false, false);

    public static final String GMAIL_APP_PASSWORD_URL = "https://support.google.com/accounts/answer/185833";
    public static final String OUTLOOK_APP_PASSWORD_URL = "https://support.microsoft.com/en-us/account-billing/manage-app-passwords";
    public static final String YAHOO_APP_PASSWORD_URL = "https://help.yahoo.com/kb/account/SLN15241";

    private final String displayName;
    private final String smtpHost;
    private final int smtpPort;
    private final boolean useTls;
    private final boolean useSsl;
    private final boolean usesAppPassword;

    ProviderPreset(String displayName, String smtpHost, int smtpPort, boolean useTls, boolean useSsl, boolean usesAppPassword) {
        this.displayName = displayName;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.useTls = useTls;
        this.useSsl = useSsl;
        this.usesAppPassword = usesAppPassword;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public boolean useTls() {
        return useTls;
    }

    public boolean useSsl() {
        return useSsl;
    }

    public boolean usesAppPassword() {
        return usesAppPassword;
    }

    public String getAppPasswordUrl() {
        return switch (this) {
            case GMAIL -> GMAIL_APP_PASSWORD_URL;
            case OUTLOOK -> OUTLOOK_APP_PASSWORD_URL;
            case YAHOO -> YAHOO_APP_PASSWORD_URL;
            case CUSTOM -> "";
        };
    }

    public static ProviderPreset fromSmtpConfig(EmailSmtpConfig config) {
        if (config == null) return CUSTOM;
        for (ProviderPreset p : values()) {
            if (p != CUSTOM
                    && p.smtpHost.equalsIgnoreCase(config.smtpHost())
                    && p.smtpPort == config.smtpPort()) {
                return p;
            }
        }
        return CUSTOM;
    }

    public static String[] displayNames() {
        return Arrays.stream(values())
                .filter(p -> p != CUSTOM)
                .map(ProviderPreset::getDisplayName)
                .toArray(String[]::new);
    }

    public static int ordinalOf(ProviderPreset preset) {
        return preset.ordinal();
    }
}
