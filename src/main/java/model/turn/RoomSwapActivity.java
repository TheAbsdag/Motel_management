package model.turn;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomSwapActivity(
        @JsonProperty("changeDate") ZonedDateTime changeDate,
        @JsonProperty("originalRoom") String originalRoom,
        @JsonProperty("originalRoomNumber") int originalRoomNumber,
        @JsonProperty("originalFloorNumber") int originalFloorNumber,
        @JsonProperty("originalTowerNumber") int originalTowerNumber,
        @JsonProperty("swappedRoom") @JsonAlias("swapedRoom") String swappedRoom,
        @JsonProperty("swappedRoomNumber") @JsonAlias("swapedRoomNumber") int swappedRoomNumber,
        @JsonProperty("swappedFloorNumber") @JsonAlias("swapedFloorNumber") int swappedFloorNumber,
        @JsonProperty("swappedTowerNumber") @JsonAlias("swapedTowerNumber") int swappedTowerNumber,
        @JsonProperty("originalRoomData") RoomData originalRoomData,
        @JsonProperty("swapRoomData") RoomData swapRoomData
) implements TurnActivity {

    public RoomSwapActivity(
            ZonedDateTime changeDate, String originalRoom, int originalRoomNumber,
            int originalFloorNumber, int originalTowerNumber,
            String swappedRoom, int swappedRoomNumber,
            int swappedFloorNumber, int swappedTowerNumber) {
        this(changeDate, originalRoom, originalRoomNumber, originalFloorNumber, originalTowerNumber,
                swappedRoom, swappedRoomNumber, swappedFloorNumber, swappedTowerNumber,
                new RoomData(originalTowerNumber, originalFloorNumber, originalRoomNumber, originalRoom),
                new RoomData(swappedTowerNumber, swappedFloorNumber, swappedRoomNumber, swappedRoom));
    }

    @Override
    public int consecutiveTrans() { return 0; }

    @JsonCreator
    public static RoomSwapActivity createFromJson(
            @JsonProperty("changeDate") ZonedDateTime changeDate,
            @JsonProperty("originalRoom") String originalRoom,
            @JsonProperty("originalRoomNumber") int originalRoomNumber,
            @JsonProperty("originalFloorNumber") int originalFloorNumber,
            @JsonProperty("originalTowerNumber") int originalTowerNumber,
            @JsonProperty("swappedRoom") @JsonAlias("swapedRoom") String swappedRoom,
            @JsonProperty("swappedRoomNumber") @JsonAlias("swapedRoomNumber") int swappedRoomNumber,
            @JsonProperty("swappedFloorNumber") @JsonAlias("swapedFloorNumber") int swappedFloorNumber,
            @JsonProperty("swappedTowerNumber") @JsonAlias("swapedTowerNumber") int swappedTowerNumber,
            @JsonProperty("originalRoomData") RoomData originalRoomData,
            @JsonProperty("swapRoomData") RoomData swapRoomData) {
        if (originalRoomData == null) {
            originalRoomData = new RoomData(originalTowerNumber, originalFloorNumber, originalRoomNumber, originalRoom);
        }
        if (swapRoomData == null) {
            swapRoomData = new RoomData(swappedTowerNumber, swappedFloorNumber, swappedRoomNumber, swappedRoom);
        }
        return new RoomSwapActivity(changeDate, originalRoom, originalRoomNumber, originalFloorNumber, originalTowerNumber,
                swappedRoom, swappedRoomNumber, swappedFloorNumber, swappedTowerNumber,
                originalRoomData, swapRoomData);
    }
}
