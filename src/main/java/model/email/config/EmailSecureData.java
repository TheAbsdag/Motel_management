package model.email.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record EmailSecureData(
        @JsonProperty("username") String username,
        @JsonProperty("credential") String credential,
        @JsonProperty("receivers") List<String> receivers) {

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
    }
}
