package model.email.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class EmailSmtpConfigTest {

    @Test
    void constructWithValidFields() {
        var config = new EmailSmtpConfig("smtp.gmail.com", 587, true, false, AuthMode.PASSWORD, 5000);
        assertThat(config.smtpHost()).isEqualTo("smtp.gmail.com");
        assertThat(config.smtpPort()).isEqualTo(587);
        assertThat(config.useStartTls()).isTrue();
        assertThat(config.useImplicitSsl()).isFalse();
        assertThat(config.authMode()).isEqualTo(AuthMode.PASSWORD);
        assertThat(config.connectionTimeoutMs()).isEqualTo(5000);
    }

    @Test
    void rejectBlankHost() {
        assertThatThrownBy(() -> new EmailSmtpConfig("", 587, true, false, AuthMode.PASSWORD, 5000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectInvalidPort() {
        assertThatThrownBy(() -> new EmailSmtpConfig("host", 0, true, false, AuthMode.PASSWORD, 5000))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EmailSmtpConfig("host", 65536, true, false, AuthMode.PASSWORD, 5000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void defaultNullAuthModeToNone() {
        var config = new EmailSmtpConfig("host", 587, true, false, null, 5000);
        assertThat(config.authMode()).isEqualTo(AuthMode.NONE);
    }

    @Test
    void defaultNegativeTimeoutTo5000() {
        var config = new EmailSmtpConfig("host", 587, true, false, AuthMode.NONE, -1);
        assertThat(config.connectionTimeoutMs()).isEqualTo(5000);
    }
}
