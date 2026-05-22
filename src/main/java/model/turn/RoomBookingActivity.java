package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;
import model.RoomStatus;

/**
 * Records a room booking activity — check-in, check-out, or status change.
 *
 * <p>When {@code roomStatus} is {@link model.RoomStatus#OCCUPIED} the fields
 * {@code endStatus}, {@code price}, {@code service}, {@code extension},
 * {@code servicedExtension} and {@code refunded} carry booking details;
 * for {@code FREE} or {@code CLEANING} statuses those fields are zero/null.
 *
 * @param changeDate       when this activity was recorded
 * @param roomString       display name of the room (e.g. "1-105")
 * @param roomNumber       room index within its floor
 * @param floorNumber      floor index
 * @param towerNumber      tower index
 * @param roomStatus       new status of the room after this activity
 * @param startStatus      when the OCCUPIED period started
 * @param endStatus        when the OCCUPIED period ended (null if not occupied)
 * @param price            amount charged for the booking
 * @param service          base service duration in minutes
 * @param extension        extra time added in minutes
 * @param servicedExtension effective service duration (may override {@code service})
 * @param consecutiveTrans consecutive transaction counter
 * @param refunded         whether this booking was later refunded
 */
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

    /**
     * Returns the effective service duration, preferring {@code servicedExtension}
     * when non-zero, otherwise falling back to {@code service}.
     *
     * @return the effective service minutes
     */
    public int getEffectiveService() {
        return servicedExtension != 0 ? servicedExtension : service;
    }

    /**
     * Returns whether this activity represents an occupied (active booking) state.
     *
     * @return {@code true} if {@code roomStatus == OCCUPIED}
     */
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

    /**
     * Deserializes a {@code RoomBookingActivity} from its JSON representation.
     *
     * @param json JSON object previously produced by {@link #toJson()}
     * @return the reconstructed activity
     */
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
