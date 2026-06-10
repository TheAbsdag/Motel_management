package model.email.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record EmailCaseConfig(
        @JsonProperty("caseIndex") int caseIndex,
        @JsonProperty("enabled") boolean enabled,
        @JsonProperty("useGlobalReceivers") boolean useGlobalReceivers,
        @JsonProperty("specificReceivers") List<String> specificReceivers,
        @JsonProperty("subject") String subject,
        @JsonProperty("body") String body,
        @JsonProperty("attachments") List<String> attachments,
        @JsonProperty("variableMappings") Map<String, String> variableMappings) {

    public EmailCaseConfig {
        if (specificReceivers == null) specificReceivers = List.of();
        if (attachments == null) attachments = List.of();
        if (subject == null) subject = "";
        if (body == null) body = "";
        if (variableMappings == null) variableMappings = Map.of();
    }
}
