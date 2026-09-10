package model.email.dto;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EmailMessageTest {

    @Test
    void multipleRecipients_shouldBeAccepted() {
        EmailMessage msg = new EmailMessage("receiver1@test.com,receiver2@test.com",
                null, null, "Sujeto", "Cuerpo", true, List.of());

        assertThat(msg.to()).isEqualTo("receiver1@test.com,receiver2@test.com");
    }

    @Test
    void invalidSingleRecipient_shouldThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new EmailMessage("not-an-email", null, null,
                        "Sujeto", "Cuerpo", false, List.of()));
    }

    @Test
    void emptyRecipientInList_shouldThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new EmailMessage("receiver1@test.com,,receiver2@test.com",
                        null, null, "Sujeto", "Cuerpo", false, List.of()));
    }

    @Test
    void blindCarbonCopy_shouldBeStored() {
        EmailMessage msg = new EmailMessage("to@test.com", "cc@test.com", "bcc@test.com",
                "Sujeto", "Cuerpo", false, List.of());

        assertThat(msg.cc()).isEqualTo("cc@test.com");
        assertThat(msg.bcc()).isEqualTo("bcc@test.com");
    }

    @Test
    void nullBlindCarbonCopy_shouldBeAllowed() {
        EmailMessage msg = new EmailMessage("to@test.com", null, null,
                "Sujeto", "Cuerpo", false, List.of());

        assertThat(msg.bcc()).isNull();
    }
}
