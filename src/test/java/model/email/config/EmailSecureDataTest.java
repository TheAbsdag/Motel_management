package model.email.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import model.json.ObjectMapperFactory;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EmailSecureDataTest {

    @Test
    void jsonRoundTrip_shouldRestoreAllFields() throws JsonProcessingException {
        EmailSecureData original = new EmailSecureData(
                "test@gmail.com", "app-pass-123",
                List.of("receiver1@test.com", "receiver2@test.com"));

        String json = ObjectMapperFactory.get().writeValueAsString(original);
        EmailSecureData restored = ObjectMapperFactory.get().readValue(json, EmailSecureData.class);

        assertThat(restored.username()).isEqualTo("test@gmail.com");
        assertThat(restored.credential()).isEqualTo("app-pass-123");
        assertThat(restored.receivers())
                .containsExactly("receiver1@test.com", "receiver2@test.com");
    }

    @Test
    void jsonRoundTrip_withEmptyReceivers_shouldRestore() throws JsonProcessingException {
        EmailSecureData original = new EmailSecureData("user@test.com", "secret", List.of());

        String json = ObjectMapperFactory.get().writeValueAsString(original);
        EmailSecureData restored = ObjectMapperFactory.get().readValue(json, EmailSecureData.class);

        assertThat(restored.username()).isEqualTo("user@test.com");
        assertThat(restored.credential()).isEqualTo("secret");
        assertThat(restored.receivers()).isEmpty();
    }

    @Test
    void nullReceivers_shouldDefaultToEmptyList() {
        EmailSecureData data = new EmailSecureData("test@test.com", "pass", null);

        assertThat(data.receivers()).isEmpty();
    }

    @Test
    void blankUsername_shouldThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new EmailSecureData("   ", "pass", List.of()));
    }

    @Test
    void nullUsername_shouldThrow() {
        assertThatNullPointerException()
                .isThrownBy(() -> new EmailSecureData(null, "pass", List.of()));
    }

    @Test
    void nullCredential_isAllowed() {
        EmailSecureData data = new EmailSecureData("test@test.com", null, List.of());

        assertThat(data.credential()).isNull();
    }

    @Test
    void receiversList_isImmutable() {
        EmailSecureData data = new EmailSecureData("test@test.com", "pass",
                List.of("a@b.com"));

        assertThatThrownBy(() -> data.receivers().add("c@d.com"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void jsonContainsFieldNames() throws JsonProcessingException {
        EmailSecureData data = new EmailSecureData("test@test.com", "pass",
                List.of("r@test.com"));

        String json = ObjectMapperFactory.get().writeValueAsString(data);

        assertThat(json).contains("\"username\"");
        assertThat(json).contains("\"credential\"");
        assertThat(json).contains("\"receivers\"");
    }

    @Test
    void jsonDeserialization_withNullReceivers_defaultsToEmpty() throws JsonProcessingException {
        String json = "{ \"username\": \"test@test.com\", \"credential\": \"pass\", \"receivers\": null }";
        EmailSecureData restored = ObjectMapperFactory.get().readValue(json, EmailSecureData.class);

        assertThat(restored.receivers()).isEmpty();
    }
}
