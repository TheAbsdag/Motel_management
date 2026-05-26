package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a single room's runtime state during persistence in {@code roomsInformation.json}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomStateData(
        @JsonProperty("roomString") String roomString,
        @JsonProperty("towerNumber") int towerNumber,
        @JsonProperty("floorNumber") int floorNumber,
        @JsonProperty("roomNumber") int roomNumber,
        @JsonProperty("status") int status,
        @JsonProperty("serviceDuration") long serviceDuration,
        @JsonProperty("startStatus") String startStatus,
        @JsonProperty("endStatus") String endStatus,
        @JsonProperty("extensionDuration") long extensionDuration
) {}
