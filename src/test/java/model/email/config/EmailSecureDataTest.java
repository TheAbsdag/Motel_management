package model.email.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.json.ObjectMapperFactory;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EmailSecureDataTest {

    @Test
    void jsonRoundTrip_shouldRestoreAllFields() throws JsonProcessingException {
        EmailSecureData original = new EmailSecureData(
                "test@gmail.com", "app-pass-123",
                List.of("receiver1@test.com", "receiver2@test.com"),
                null, null, null);

        String json = ObjectMapperFactory.get().writeValueAsString(original);
        EmailSecureData restored = ObjectMapperFactory.get().readValue(json, EmailSecureData.class);

        assertThat(restored.username()).isEqualTo("test@gmail.com");
        assertThat(restored.credential()).isEqualTo("app-pass-123");
        assertThat(restored.receivers())
                .containsExactly("receiver1@test.com", "receiver2@test.com");
    }

    @Test
    void jsonRoundTrip_withEmptyReceivers_shouldRestore() throws JsonProcessingException {
        EmailSecureData original = new EmailSecureData("user@test.com", "secret", List.of(),
                null, null, null);

        String json = ObjectMapperFactory.get().writeValueAsString(original);
        EmailSecureData restored = ObjectMapperFactory.get().readValue(json, EmailSecureData.class);

        assertThat(restored.username()).isEqualTo("user@test.com");
        assertThat(restored.credential()).isEqualTo("secret");
        assertThat(restored.receivers()).isEmpty();
    }

    @Test
    void nullReceivers_shouldDefaultToEmptyList() {
        EmailSecureData data = new EmailSecureData("test@test.com", "pass", null,
                null, null, null);

        assertThat(data.receivers()).isEmpty();
    }

    @Test
    void blankUsername_shouldThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new EmailSecureData("   ", "pass", List.of(),
                        null, null, null));
    }

    @Test
    void nullUsername_shouldThrow() {
        assertThatNullPointerException()
                .isThrownBy(() -> new EmailSecureData(null, "pass", List.of(),
                        null, null, null));
    }

    @Test
    void nullCredential_isAllowed() {
        EmailSecureData data = new EmailSecureData("test@test.com", null, List.of(),
                null, null, null);

        assertThat(data.credential()).isNull();
    }

    @Test
    void receiversList_isImmutable() {
        EmailSecureData data = new EmailSecureData("test@test.com", "pass",
                List.of("a@b.com"), null, null, null);

        assertThatThrownBy(() -> data.receivers().add("c@d.com"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void jsonContainsFieldNames() throws JsonProcessingException {
        EmailSecureData data = new EmailSecureData("test@test.com", "pass",
                List.of("r@test.com"), null, null, null);

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

    @Test
    void constructWithAllNewFields() {
        var config = new EmailSecureData("user", "pass",
                List.of("to@c.com"), List.of("cc@c.com"), List.of("bcc@c.com"),
                Map.of(0, List.of("case0@c.com")));
        assertThat(config.cc()).containsExactly("cc@c.com");
        assertThat(config.bcc()).containsExactly("bcc@c.com");
        assertThat(config.caseSpecificReceivers()).containsKey(0);
        assertThat(config.caseSpecificReceivers().get(0)).containsExactly("case0@c.com");
    }

    @Test
    void caseSpecificReceiversValuesAreUnmodifiable() {
        var config = new EmailSecureData("u", "p", List.of(), null, null,
                Map.of(0, new ArrayList<>(List.of("a@b.com"))));
        assertThatThrownBy(() -> config.caseSpecificReceivers().get(0).add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void defaultNullNewFieldsToEmpty() {
        var config = new EmailSecureData("user", "pass",
                List.of("to@c.com"), null, null, null);
        assertThat(config.cc()).isEmpty();
        assertThat(config.bcc()).isEmpty();
        assertThat(config.caseSpecificReceivers()).isEmpty();
    }

    @Test
    void jacksonRoundTripWithNewFields() throws Exception {
        var original = new EmailSecureData("user", "pass",
                List.of("to@c.com"), List.of("cc@c.com"), List.of("bcc@c.com"),
                Map.of(0, List.of("case0@c.com"), 1, List.of("case1@c.com")));
        String json = model.json.ObjectMapperFactory.get().writeValueAsString(original);
        var restored = model.json.ObjectMapperFactory.get().readValue(json, EmailSecureData.class);
        assertThat(restored).isEqualTo(original);
    }
}
