package model.email.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import model.json.ObjectMapperFactory;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EmailConfigTest {

    @Test
    void toProperties_shouldContainSmtpDefaults() {
        EmailConfig config = EmailConfig.genericSmtp("smtp.test.com", 587, "user@test.com", "pass123", true);

        assertThat(config.toProperties())
                .containsEntry("mail.smtp.host", "smtp.test.com")
                .containsEntry("mail.smtp.port", "587")
                .containsEntry("mail.smtp.auth", "true")
                .containsEntry("mail.smtp.starttls.enable", "true")
                .containsEntry("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
                .containsEntry("mail.mime.charset", "UTF-8");
    }

    @Test
    void jsonRoundTrip_withoutCredential_shouldRestoreFields() throws JsonProcessingException {
        EmailConfig original = EmailConfig.builder()
                .smtpHost("smtp.gmail.com")
                .smtpPort(587)
                .useStartTls(true)
                .authMode(AuthMode.PASSWORD)
                .username("test@gmail.com")
                .extraProperty("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
                .build();

        String json = ObjectMapperFactory.get().writeValueAsString(original);
        EmailConfig restored = ObjectMapperFactory.get().readValue(json, EmailConfig.class);

        assertThat(restored.smtpHost()).isEqualTo("smtp.gmail.com");
        assertThat(restored.smtpPort()).isEqualTo(587);
        assertThat(restored.useStartTls()).isTrue();
        assertThat(restored.authMode()).isEqualTo(AuthMode.PASSWORD);
        assertThat(restored.username()).isEmpty();
        assertThat(restored.credential()).isNull();
        assertThat(json).doesNotContain("username");
        assertThat(json).doesNotContain("credential");
    }

    @Test
    void gmailAppPassword_preset_shouldHaveCorrectDefaults() {
        EmailConfig config = EmailConfig.gmailAppPassword("test@gmail.com", "abcd-efgh-ijkl-mnop");

        assertThat(config.smtpHost()).isEqualTo("smtp.gmail.com");
        assertThat(config.smtpPort()).isEqualTo(587);
        assertThat(config.useStartTls()).isTrue();
        assertThat(config.useImplicitSsl()).isFalse();
        assertThat(config.authMode()).isEqualTo(AuthMode.PASSWORD);
        assertThat(config.username()).isEqualTo("test@gmail.com");
        assertThat(config.credential()).isEqualTo("abcd-efgh-ijkl-mnop");
    }

    @Test
    void gmailAppPassword_nullEmail_shouldThrow() {
        assertThatNullPointerException()
                .isThrownBy(() -> EmailConfig.gmailAppPassword(null, "pass"));
    }

    @Test
    void gmailAppPassword_nullPassword_shouldThrow() {
        assertThatNullPointerException()
                .isThrownBy(() -> EmailConfig.gmailAppPassword("test@gmail.com", null));
    }

    @Test
    void outlookPassword_preset_shouldHaveCorrectDefaults() {
        EmailConfig config = EmailConfig.outlookPassword("test@outlook.com", "app-pass-123");

        assertThat(config.smtpHost()).isEqualTo("smtp.office365.com");
        assertThat(config.smtpPort()).isEqualTo(587);
        assertThat(config.authMode()).isEqualTo(AuthMode.PASSWORD);
        assertThat(config.credential()).isEqualTo("app-pass-123");
    }

    @Test
    void builder_missingHost_shouldThrow() {
        assertThatNullPointerException()
                .isThrownBy(() -> EmailConfig.builder().smtpPort(587).authMode(AuthMode.NONE).build());
    }

    @Test
    void builder_invalidPort_shouldThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> EmailConfig.builder().smtpHost("test.com").smtpPort(0).build());
        assertThatIllegalArgumentException()
                .isThrownBy(() -> EmailConfig.builder().smtpHost("test.com").smtpPort(65536).build());
    }

    @Test
    void emailMessage_invalidEmail_shouldThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new model.email.dto.EmailMessage("not-an-email", null, "subj", "body", false, null));
    }
}
