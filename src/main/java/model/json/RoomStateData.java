package model.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import model.RoomStatus;

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
) {

    /**
     * Reads a room state accepting the field names written by legacy versions,
     * which stored {@code service} and {@code extension} in hours instead of the
     * current {@code serviceDuration} / {@code extensionDuration} in seconds.
     *
     * @param legacyService    legacy service duration in hours
     * @param legacyExtension  legacy extension duration in hours
     * @return the room state in the current units
     */
    @JsonCreator
    public static RoomStateData createFromJson(
            @JsonProperty("roomString") String roomString,
            @JsonProperty("towerNumber") Integer towerNumber,
            @JsonProperty("floorNumber") Integer floorNumber,
            @JsonProperty("roomNumber") Integer roomNumber,
            @JsonProperty("status") Integer status,
            @JsonProperty("serviceDuration") Long serviceDuration,
            @JsonProperty("service") Integer legacyService,
            @JsonProperty("startStatus") String startStatus,
            @JsonProperty("endStatus") String endStatus,
            @JsonProperty("extensionDuration") Long extensionDuration,
            @JsonProperty("extension") Integer legacyExtension) {
        return new RoomStateData(
                roomString == null ? "" : roomString,
                towerNumber == null ? -1 : towerNumber,
                floorNumber == null ? -1 : floorNumber,
                roomNumber == null ? -1 : roomNumber,
                status == null ? RoomStatus.FREE.getCode() : status,
                serviceDuration != null ? serviceDuration : hoursToSeconds(legacyService),
                startStatus == null ? "" : startStatus,
                endStatus == null ? "" : endStatus,
                extensionDuration != null ? extensionDuration : hoursToSeconds(legacyExtension));
    }

    private static long hoursToSeconds(Integer hours) {
        return hours == null ? 0L : hours * 3600L;
    }
}
