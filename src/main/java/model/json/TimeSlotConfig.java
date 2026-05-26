package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TimeSlotConfig(
        @JsonProperty("price") long price,
        @JsonProperty("timeSeconds") long timeSeconds
) {}
