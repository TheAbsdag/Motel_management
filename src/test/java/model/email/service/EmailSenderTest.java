package model.email.service;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import model.email.config.AuthMode;
import model.email.config.EmailConfig;
import model.email.dto.EmailMessage;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EmailSenderTest {

    private static EmailSender sender() {
        EmailConfig config = new EmailConfig("smtp.test.com", 587, true, false,
                AuthMode.NONE, "sender@test.com", null, 5000, Map.of());
        return new EmailSender(config);
    }

    private static String[] addresses(MimeMessage mime, Message.RecipientType type) throws Exception {
        return Arrays.stream(mime.getRecipients(type))
                .map(a -> ((InternetAddress) a).getAddress())
                .toArray(String[]::new);
    }

    @Test
    void buildMimeMessage_shouldIncludeAllRecipients() throws Exception {
        EmailMessage msg = new EmailMessage("to1@test.com,to2@test.com",
                "cc@test.com", "bcc@test.com", "Sujeto", "<p>Cuerpo</p>", true, List.of());

        MimeMessage mime = sender().buildMimeMessage(
                Session.getInstance(new Properties()), msg);

        assertThat(addresses(mime, Message.RecipientType.TO))
                .containsExactly("to1@test.com", "to2@test.com");
        assertThat(addresses(mime, Message.RecipientType.CC))
                .containsExactly("cc@test.com");
        assertThat(addresses(mime, Message.RecipientType.BCC))
                .containsExactly("bcc@test.com");
        assertThat(mime.getSubject()).isEqualTo("Sujeto");
        assertThat(mime.getContentType()).startsWith("text/html");
    }

    @Test
    void buildMimeMessage_withoutCcOrBcc_shouldNotSetThoseRecipients() throws Exception {
        EmailMessage msg = new EmailMessage("to@test.com", null, null,
                "Sujeto", "Cuerpo", false, List.of());

        MimeMessage mime = sender().buildMimeMessage(
                Session.getInstance(new Properties()), msg);

        assertThat(mime.getRecipients(Message.RecipientType.CC)).isNull();
        assertThat(mime.getRecipients(Message.RecipientType.BCC)).isNull();
    }
}
