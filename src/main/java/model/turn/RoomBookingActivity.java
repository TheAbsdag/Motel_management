package model.turn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.ZonedDateTime;
import model.RoomStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomBookingActivity(
        @JsonProperty("changeDate") ZonedDateTime changeDate,
        @JsonProperty("roomString") String roomString,
        @JsonProperty("roomNumber") int roomNumber,
        @JsonProperty("floorNumber") int floorNumber,
        @JsonProperty("towerNumber") int towerNumber,
        @JsonProperty("roomStatus") RoomStatus roomStatus,
        @JsonProperty("startStatus") ZonedDateTime startStatus,
        @JsonProperty("endStatus") ZonedDateTime endStatus,
        @JsonProperty("price") long price,
        @JsonProperty("serviceDuration") long serviceDuration,
        @JsonProperty("extensionDuration") long extensionDuration,
        @JsonProperty("servicedExtensionDuration") long servicedExtensionDuration,
        @JsonProperty("consecutiveTrans") int consecutiveTrans,
        @JsonProperty("refunded") boolean refunded
) implements TurnActivity {

    public long getEffectiveServiceDuration() {
        return servicedExtensionDuration != 0 ? servicedExtensionDuration : serviceDuration;
    }

    public boolean isOccupied() {
        return roomStatus == RoomStatus.OCCUPIED;
    }

    @JsonCreator
    public static RoomBookingActivity createFromJson(
            @JsonProperty("changeDate") ZonedDateTime changeDate,
            @JsonProperty("roomString") String roomString,
            @JsonProperty("roomNumber") int roomNumber,
            @JsonProperty("floorNumber") int floorNumber,
            @JsonProperty("towerNumber") int towerNumber,
            @JsonProperty("roomStatus") int roomStatusCode,
            @JsonProperty("startStatus") ZonedDateTime startStatus,
            @JsonProperty("endStatus") ZonedDateTime endStatus,
            @JsonProperty("price") Long price,
            @JsonProperty("serviceDuration") Long serviceDuration,
            @JsonProperty("service") Integer legacyService,
            @JsonProperty("extensionDuration") Long extensionDuration,
            @JsonProperty("extension") Integer legacyExtension,
            @JsonProperty("servicedExtensionDuration") Long servicedExtensionDuration,
            @JsonProperty("servicedExtension") Integer legacyServicedExtension,
            @JsonProperty("consecutiveTrans") Integer consecutiveTrans,
            @JsonProperty("refunded") Boolean refunded) {
        boolean occupied = roomStatusCode == RoomStatus.OCCUPIED.getCode();
        long serviceDur = serviceDuration != null ? serviceDuration
                : (legacyService != null ? (long) legacyService * 3600L : 0L);
        long extensionDur = extensionDuration != null ? extensionDuration
                : (legacyExtension != null ? (long) legacyExtension * 3600L : 0L);
        long servicedExtDur = servicedExtensionDuration != null ? servicedExtensionDuration
                : (legacyServicedExtension != null ? (long) legacyServicedExtension * 3600L : 0L);
        return new RoomBookingActivity(
                changeDate, roomString, roomNumber, floorNumber, towerNumber,
                RoomStatus.fromCode(roomStatusCode),
                startStatus,
                occupied ? endStatus : null,
                occupied ? (price != null ? price : 0L) : 0L,
                serviceDur, extensionDur, servicedExtDur,
                consecutiveTrans != null ? consecutiveTrans : 0,
                refunded != null && refunded);
    }
}
