package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a single time-slot pricing entry within a room's custom time data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TimeSlotConfig(
        @JsonProperty("price") long price,
        @JsonProperty("timeSeconds") long timeSeconds
) {}
