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
        @JsonProperty("swappedTowerNumber") @JsonAlias("swapedTowerNumber") int swappedTowerNumber
) implements TurnActivity {

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
            @JsonProperty("swappedTowerNumber") @JsonAlias("swapedTowerNumber") int swappedTowerNumber) {
        return new RoomSwapActivity(changeDate, originalRoom, originalRoomNumber, originalFloorNumber, originalTowerNumber,
                swappedRoom, swappedRoomNumber, swappedFloorNumber, swappedTowerNumber);
    }
}
