package model.email.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EmailSecureData(
        @JsonProperty("username") String username,
        @JsonProperty("credential") String credential,
        @JsonProperty("receivers") List<String> receivers,
        @JsonProperty("cc") List<String> cc,
        @JsonProperty("bcc") List<String> bcc,
        @JsonProperty("caseSpecificReceivers") Map<Integer, List<String>> caseSpecificReceivers) {

    public EmailSecureData {
        Objects.requireNonNull(username, "username cannot be null");
        if (username.isBlank()) {
            throw new IllegalArgumentException("username cannot be blank");
        }
        if (receivers == null) {
            receivers = List.of();
        } else {
            receivers = Collections.unmodifiableList(new ArrayList<>(receivers));
        }
        if (cc == null) {
            cc = List.of();
        } else {
            cc = Collections.unmodifiableList(new ArrayList<>(cc));
        }
        if (bcc == null) {
            bcc = List.of();
        } else {
            bcc = Collections.unmodifiableList(new ArrayList<>(bcc));
        }
        if (caseSpecificReceivers == null) {
            caseSpecificReceivers = Map.of();
        } else {
            caseSpecificReceivers = Collections.unmodifiableMap(new HashMap<>(caseSpecificReceivers));
        }
    }
}
