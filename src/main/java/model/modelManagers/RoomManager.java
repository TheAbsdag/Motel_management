package model.modelManagers;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import model.Room;
import model.RoomStatus;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Manages the motel's room grid (3D: tower → floor → room) and room-level operations.
 * Extracted from {@link MotelManagement} for single-responsibility.
 *
 * @author Santiago
 */
public class RoomManager {

    private final ArrayList<ArrayList<ArrayList<Room>>> rooms;
    private final ZoneId zoneID;
    private final Room reception;

    private int currentFloorViewed;
    private int currentRoomViewed;
    private int currentTowerViewed;
    private int currentServiceDesired;

    // Room change state
    private int selectedRoomChangeRoom;
    private int selectedRoomChangeFloor;
    private int selectedRoomChangeTower;

    public RoomManager(ZoneId zoneID) {
        this.zoneID = zoneID;
        this.rooms = new ArrayList<>();
        this.reception = new Room("Recepcion", -1, -1, -1);
    }

    // ========== Room Grid Initialization ==========

    /**
     * Builds the room grid from the application properties JSON.
     */
    public void buildRoomGrid(JSONObject programData) {
        JSONArray roomsPerTower = programData.getJSONArray("roomsPerTower");

        for (int towerIndex = 0; towerIndex < roomsPerTower.length(); towerIndex++) {
            JSONObject tower = roomsPerTower.getJSONObject(towerIndex);
            int towerNumber = tower.getInt("towerNumber");
            int towerFloors = tower.getInt("towerFloors");

            rooms.add(new ArrayList<>());

            for (int floorIndex = 0; floorIndex < towerFloors; floorIndex++) {
                rooms.get(towerIndex).add(new ArrayList<>());
            }

            JSONArray towerRooms = tower.getJSONArray("towerRooms");

            for (int floorDataIndex = 0; floorDataIndex < towerRooms.length(); floorDataIndex++) {
                JSONObject floorData = towerRooms.getJSONObject(floorDataIndex);
                int floorNumber = floorData.getInt("floor");
                JSONArray roomsArray = floorData.getJSONArray("rooms");

                for (int roomIndex = 0; roomIndex < roomsArray.length(); roomIndex++) {
                    JSONObject roomJson = roomsArray.getJSONObject(roomIndex);
                    String roomString = roomJson.getString("roomString");
                    int roomFloor = roomJson.getInt("roomFloor");
                    int roomNumber = roomJson.getInt("roomNumber");

                    Room currentRoom = new Room(roomString, roomFloor, roomNumber, towerNumber);
                    rooms.get(towerIndex).get(floorNumber).add(currentRoom);
                }
            }
        }
    }

    /**
     * Restores room states from saved room data JSON.
     */
    public void restoreRoomStates(JSONObject roomData) {
        if (roomData == null || roomData.isEmpty()) {
            return;
        }
        JSONArray roomsArray = roomData.getJSONArray("rooms");
        for (int i = 0; i < roomsArray.length(); i++) {
            JSONObject room = roomsArray.getJSONObject(i);
            String roomString = room.getString("roomString");
            int towerNum = room.getInt("towerNumber");
            int floor = room.getInt("floorNumber");
            int roomNum = room.getInt("roomNumber");
            int status = room.getInt("status");
            RoomStatus roomStatus = RoomStatus.fromCode(status);

            if (towerNum >= 0 && towerNum < rooms.size()
                    && floor >= 0 && floor < rooms.get(towerNum).size()
                    && roomNum >= 0 && roomNum < rooms.get(towerNum).get(floor).size()) {

                Room targetRoom = rooms.get(towerNum).get(floor).get(roomNum);

                switch (roomStatus) {
                    case FREE:
                        targetRoom.setRoomStatus(RoomStatus.FREE);
                        break;
                    case CLEANING:
                        targetRoom.setRoomStatus(RoomStatus.CLEANING,
                                ZonedDateTime.parse(room.getString("startStatus")).toInstant());
                        break;
                    case OCCUPIED:
                        targetRoom.setRoomStatus(RoomStatus.OCCUPIED,
                                ZonedDateTime.parse(room.getString("startStatus")).toInstant(),
                                room.getInt("service"));
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid status: " + status);
                }
                int extension = room.getInt("extension");
                if (extension != 0) {
                    targetRoom.extendRoomTime(extension);
                }
            } else {
                System.out.println("Room not found: " + roomString + " (Tower: " + towerNum
                        + ", Floor: " + floor + ", Room: " + roomNum + ")");
            }
        }
    }

    // ========== Room Operations ==========

    /**
     * Books a room or extends time. Returns the extension amount (0 for new bookings).
     */
    public int registerRoomTimeAdded(int tower, int floor, int room, int service, Instant currentTime) {
        Room targetRoom = rooms.get(tower).get(floor).get(room);
        RoomStatus currentStatus = targetRoom.getStatus();
        int currentExtension = 0;
        if (currentStatus == RoomStatus.OCCUPIED) {
            targetRoom.extendRoomTime(service);
            currentExtension = service;
        } else {
            targetRoom.setRoomStatus(RoomStatus.OCCUPIED, currentTime, service);
        }
        return currentExtension;
    }

    /**
     * Ends room service (check-out or cleaning complete).
     */
    public void registerRoomTimeEnd(int tower, int floor, int room, Instant currentTime) {
        Room targetRoom = rooms.get(tower).get(floor).get(room);
        if (targetRoom.getStatus() == RoomStatus.CLEANING) {
            targetRoom.setRoomStatus(RoomStatus.FREE);
        } else {
            targetRoom.setRoomStatus(RoomStatus.CLEANING, currentTime);
        }
    }

    /**
     * Moves a guest from the current room to the desired change room.
     * @return true if the change was valid, false if the target room is occupied
     */
    public boolean changeRoomTimeToAnother(Instant currentTime) {
        Room currentRoom = rooms.get(currentTowerViewed).get(currentFloorViewed).get(currentRoomViewed);
        Room desiredChangeRoom = rooms.get(selectedRoomChangeTower)
                .get(selectedRoomChangeFloor).get(selectedRoomChangeRoom);
        if (desiredChangeRoom.getStatus() == RoomStatus.OCCUPIED) {
            return false;
        }
        int currentService = currentRoom.getService();
        int currentTotalExtension = currentRoom.getExtension();
        Instant currentStartTime = currentRoom.getStartStatus();
        desiredChangeRoom.setRoomStatus(RoomStatus.OCCUPIED, currentStartTime, currentService);
        desiredChangeRoom.setExtension(currentTotalExtension);
        currentRoom.setRoomStatus(RoomStatus.CLEANING, currentTime);
        return true;
    }

    // ========== Queries ==========

    public int[][] getRoomsArray() {
        int[][] arr = new int[rooms.size()][];
        for (int tower = 0; tower < rooms.size(); tower++) {
            arr[tower] = new int[rooms.get(tower).size()];
            for (int floor = 0; floor < rooms.get(tower).size(); floor++) {
                arr[tower][floor] = rooms.get(tower).get(floor).size();
            }
        }
        return arr;
    }

    public Room getRoom(int tower, int floor, int room) {
        if (tower < 0 || floor < 0 || room < 0
                || tower >= rooms.size()
                || floor >= rooms.get(tower).size()
                || room >= rooms.get(tower).get(floor).size()) {
            return reception;
        }
        return rooms.get(tower).get(floor).get(room);
    }

    public String getRemainingTimeRoom(int tower, int floor, int room, Instant currentTime) {
        Room target = getRoom(tower, floor, room);
        Instant endStatus = target.getEndStatus();
        if (endStatus == null) {
            return "0:0";
        }
        Duration duration = Duration.between(currentTime, endStatus);
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return hours + ":" + minutes;
    }

    public String getStartTimeRoom(int tower, int floor, int room) {
        Instant startStatus = getRoom(tower, floor, room).getStartStatus();
        if (startStatus == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("hh:mm:ss").appendLiteral(' ')
                .appendText(ChronoField.AMPM_OF_DAY, new HashMap<Long, String>() {{
                    put(0L, "\tAM"); put(1L, "\tPM");
                }})
                .toFormatter();
        ZonedDateTime start = startStatus.atZone(zoneID);
        return start.format(formatter);
    }

    public String getStartDateRoom(int tower, int floor, int room) {
        Instant startStatus = getRoom(tower, floor, room).getStartStatus();
        if (startStatus == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", new Locale("es", "ES"));
        ZonedDateTime start = startStatus.atZone(zoneID);
        return start.format(formatter);
    }

    /**
     * Returns the room that a sale should be attributed to.
     * Returns reception room when floor/room is -1 (reception sale).
     */
    public Room getRoomForSale() {
        if (currentFloorViewed == -1) {
            return reception;
        }
        return rooms.get(currentTowerViewed).get(currentFloorViewed).get(currentRoomViewed);
    }

    /**
     * Returns the currently-viewed room for room-swap operations.
     */
    public Room getCurrentRoom() {
        return rooms.get(currentTowerViewed).get(currentFloorViewed).get(currentRoomViewed);
    }

    /**
     * Returns the target room for room-swap operations.
     */
    public Room getDesiredChangeRoom() {
        return rooms.get(selectedRoomChangeTower).get(selectedRoomChangeFloor).get(selectedRoomChangeRoom);
    }

    // ========== Room Data for Persistence ==========

    /**
     * Serializes all room data to a JSONArray for saving.
     */
    public JSONArray getRoomDataForSaving() {
        JSONArray roomDataArray = new JSONArray();
        for (int tower = 0; tower < rooms.size(); tower++) {
            for (int floor = 0; floor < rooms.get(tower).size(); floor++) {
                for (int roomIndex = 0; roomIndex < rooms.get(tower).get(floor).size(); roomIndex++) {
                    Room room = rooms.get(tower).get(floor).get(roomIndex);
                    JSONObject currentRoom = new JSONObject();
                    currentRoom.put("roomString", room.getRoomString());
                    currentRoom.put("towerNumber", tower);
                    currentRoom.put("floorNumber", room.getFloorNumber());
                    currentRoom.put("roomNumber", room.getRoomNumber());
                    currentRoom.put("status", room.getStatus().getCode());
                    currentRoom.put("service", room.getService());

                    Instant startStatus = room.getStartStatus();
                    currentRoom.put("startStatus", startStatus == null ? "" : startStatus.atZone(zoneID).toString());

                    Instant endStatus = room.getEndStatus();
                    currentRoom.put("endStatus", endStatus == null ? "" : endStatus.atZone(zoneID).toString());

                    currentRoom.put("extension", room.getExtension());
                    roomDataArray.put(currentRoom);
                }
            }
        }
        return roomDataArray;
    }

    // ========== Getters / Setters ==========

    public int getCurrentFloorViewed() { return currentFloorViewed; }
    public int getCurrentRoomViewed() { return currentRoomViewed; }
    public int getCurrentTowerViewed() { return currentTowerViewed; }
    public int getCurrentServiceDesired() { return currentServiceDesired; }

    public void setCurrentServiceDesired(int service) { this.currentServiceDesired = service; }

    public void setCurrentFloorRoom(int tower, int floor, int room) {
        this.currentTowerViewed = tower;
        this.currentFloorViewed = floor;
        this.currentRoomViewed = room;
    }

    public void setDesiredRoomChange(int tower, int floor, int room) {
        this.selectedRoomChangeTower = tower;
        this.selectedRoomChangeFloor = floor;
        this.selectedRoomChangeRoom = room;
    }

    public int getSelectedRoomChangeRoom() { return selectedRoomChangeRoom; }
    public int getSelectedRoomChangeFloor() { return selectedRoomChangeFloor; }
    public int getSelectedRoomChangeTower() { return selectedRoomChangeTower; }

    public ArrayList<ArrayList<ArrayList<Room>>> getRooms() { return rooms; }
}
