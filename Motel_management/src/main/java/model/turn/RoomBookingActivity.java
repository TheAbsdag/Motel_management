package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;
import model.RoomStatus;

public record RoomBookingActivity(
        ZonedDateTime changeDate,
        String roomString, int roomNumber, int floorNumber, int towerNumber,
        RoomStatus roomStatus,
        ZonedDateTime startStatus,
        ZonedDateTime endStatus,
        long price,
        int service,
        int extension,
        int servicedExtension,
        int consecutiveTrans,
        boolean refunded
) implements TurnActivity {

    public int getEffectiveService() {
        return servicedExtension != 0 ? servicedExtension : service;
    }

    public boolean isOccupied() {
        return roomStatus == RoomStatus.OCCUPIED;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("changeDate", changeDate.toString());
        json.put("changeType", "room");
        json.put("roomString", roomString);
        json.put("roomNumber", roomNumber);
        json.put("floorNumber", floorNumber);
        json.put("towerNumber", towerNumber);
        json.put("roomStatus", roomStatus.getCode());
        json.put("startStatus", startStatus.toString());
        if (endStatus != null) {
            json.put("endStatus", endStatus.toString());
            json.put("price", price);
            json.put("service", service);
            json.put("extension", extension);
            json.put("servicedExtension", servicedExtension);
            json.put("refunded", refunded);
        }
        if (consecutiveTrans > 0) {
            json.put("consecutiveTrans", consecutiveTrans);
        }
        return json;
    }

    public static RoomBookingActivity fromJson(JSONObject json) {
        boolean occupied = json.getInt("roomStatus") == RoomStatus.OCCUPIED.getCode();
        return new RoomBookingActivity(
                ZonedDateTime.parse(json.getString("changeDate")),
                json.getString("roomString"),
                json.getInt("roomNumber"),
                json.getInt("floorNumber"),
                json.getInt("towerNumber"),
                RoomStatus.fromCode(json.getInt("roomStatus")),
                ZonedDateTime.parse(json.getString("startStatus")),
                occupied ? ZonedDateTime.parse(json.getString("endStatus")) : null,
                occupied ? json.getLong("price") : 0,
                occupied ? json.getInt("service") : 0,
                occupied ? json.getInt("extension") : 0,
                occupied ? json.getInt("servicedExtension") : 0,
                json.optInt("consecutiveTrans", 0),
                json.optBoolean("refunded", false)
        );
    }
}
