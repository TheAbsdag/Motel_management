package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

/**
 * Records a room-to-room swap that moves a guest from one room to another.
 *
 * <p>Room swaps have no direct financial impact on the turn totals; they are
 * purely logistical changes tracked for audit purposes.
 *
 * @param changeDate           when the swap occurred
 * @param originalRoom         display name of the original (source) room
 * @param originalRoomNumber   source room index
 * @param originalFloorNumber  source floor index
 * @param originalTowerNumber  source tower index
 * @param swappedRoom          display name of the destination room
 * @param swappedRoomNumber    destination room index
 * @param swappedFloorNumber   destination floor index
 * @param swappedTowerNumber   destination tower index
 */
public record RoomSwapActivity(
        ZonedDateTime changeDate,
        String originalRoom, int originalRoomNumber, int originalFloorNumber, int originalTowerNumber,
        String swappedRoom, int swappedRoomNumber, int swappedFloorNumber, int swappedTowerNumber
) implements TurnActivity {

    @Override
    public int consecutiveTrans() { return 0; }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("changeDate", changeDate.toString());
        json.put("changeType", "roomSwap");
        json.put("originalRoom", originalRoom);
        json.put("swappedRoom", swappedRoom);
        json.put("originalRoomNumber", originalRoomNumber);
        json.put("originalFloorNumber", originalFloorNumber);
        json.put("originalTowerNumber", originalTowerNumber);
        json.put("swappedRoomNumber", swappedRoomNumber);
        json.put("swappedFloorNumber", swappedFloorNumber);
        json.put("swappedTowerNumber", swappedTowerNumber);
        return json;
    }

    /**
     * Deserializes a {@code RoomSwapActivity} from its JSON representation.
     * Supports both {@code "swappedRoom"} (current) and {@code "swapedRoom"} (legacy)
     * keys for backward compatibility.
     *
     * @param json JSON object previously produced by {@link #toJson()}
     * @return the reconstructed activity
     */
    public static RoomSwapActivity fromJson(JSONObject json) {
        String swappedRoom = json.has("swappedRoom")
                ? json.getString("swappedRoom")
                : json.getString("swapedRoom");
        int swappedRoomNumber = json.has("swappedRoomNumber")
                ? json.getInt("swappedRoomNumber")
                : json.getInt("swapedRoomNumber");
        int swappedFloorNumber = json.has("swappedFloorNumber")
                ? json.getInt("swappedFloorNumber")
                : json.getInt("swapedFloorNumber");
        int swappedTowerNumber = json.has("swappedTowerNumber")
                ? json.getInt("swappedTowerNumber")
                : json.getInt("swapedTowerNumber");
        return new RoomSwapActivity(
                ZonedDateTime.parse(json.getString("changeDate")),
                json.getString("originalRoom"),
                json.getInt("originalRoomNumber"),
                json.getInt("originalFloorNumber"),
                json.getInt("originalTowerNumber"),
                swappedRoom,
                swappedRoomNumber,
                swappedFloorNumber,
                swappedTowerNumber
        );
    }
}
