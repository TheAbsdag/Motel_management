package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

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
        json.put("swapedRoom", swappedRoom);
        json.put("originalRoomNumber", originalRoomNumber);
        json.put("originalFloorNumber", originalFloorNumber);
        json.put("originalTowerNumber", originalTowerNumber);
        json.put("swapedRoomNumber", swappedRoomNumber);
        json.put("swapedFloorNumber", swappedFloorNumber);
        json.put("swapedTowerNumber", swappedTowerNumber);
        return json;
    }

    public static RoomSwapActivity fromJson(JSONObject json) {
        return new RoomSwapActivity(
                ZonedDateTime.parse(json.getString("changeDate")),
                json.getString("originalRoom"),
                json.getInt("originalRoomNumber"),
                json.getInt("originalFloorNumber"),
                json.getInt("originalTowerNumber"),
                json.getString("swapedRoom"),
                json.getInt("swapedRoomNumber"),
                json.getInt("swapedFloorNumber"),
                json.getInt("swapedTowerNumber")
        );
    }
}
