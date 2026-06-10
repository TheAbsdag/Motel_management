package model.email.config;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmailCaseConfigTest {

    @Test
    void constructWithAllFields() {
        var config = new EmailCaseConfig(0, true, false,
                List.of("admin@motel.com"), "Subj", "Body", List.of("Recibo PDF"),
                Map.of("{1}", "{motelName}"));
        assertThat(config.caseIndex()).isZero();
        assertThat(config.enabled()).isTrue();
        assertThat(config.useGlobalReceivers()).isFalse();
        assertThat(config.specificReceivers()).containsExactly("admin@motel.com");
        assertThat(config.subject()).isEqualTo("Subj");
        assertThat(config.body()).isEqualTo("Body");
        assertThat(config.attachments()).containsExactly("Recibo PDF");
    }

    @Test
    void defaultNullFieldsToEmpty() {
        var config = new EmailCaseConfig(1, false, true, null, null, null, null, null);
        assertThat(config.specificReceivers()).isEmpty();
        assertThat(config.attachments()).isEmpty();
        assertThat(config.subject()).isEmpty();
        assertThat(config.body()).isEmpty();
        assertThat(config.variableMappings()).isEmpty();
    }

    @Test
    void jacksonRoundTrip() throws Exception {
        var original = new EmailCaseConfig(2, true, false,
                List.of("a@b.com"), "Subject {x}", "Body {y}", List.of("R1", "R2"), null);
        String json = model.json.ObjectMapperFactory.get().writeValueAsString(original);
        var restored = model.json.ObjectMapperFactory.get().readValue(json, EmailCaseConfig.class);
        assertThat(restored).isEqualTo(original);
    }

    @Test
    void variableMappingsField() {
        var mappings = Map.of("{1}", "{motelName}", "{custom}", "{roomString}");
        var config = new EmailCaseConfig(0, true, false, List.of(), "Subj", "Body",
                List.of(), mappings);
        assertThat(config.variableMappings()).containsEntry("{1}", "{motelName}");
    }

    @Test
    void variableMappingsDefaultsToEmpty() {
        var config = new EmailCaseConfig(0, true, false, List.of(), "Subj", "Body",
                List.of(), null);
        assertThat(config.variableMappings()).isEmpty();
    }

    @Test
    void jacksonRoundTripWithMappings() throws Exception {
        var mappings = Map.of("{1}", "{motelName}");
        var original = new EmailCaseConfig(0, true, false, List.of(), "Subj {1}",
                "Body", List.of(), mappings);
        String json = model.json.ObjectMapperFactory.get().writeValueAsString(original);
        var restored = model.json.ObjectMapperFactory.get().readValue(json, EmailCaseConfig.class);
        assertThat(restored.variableMappings()).containsEntry("{1}", "{motelName}");
    }
}
