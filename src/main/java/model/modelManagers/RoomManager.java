package model.modelManagers;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Room;
import model.RoomStatus;
import model.RoomTime;
import org.json.JSONArray;
import org.json.JSONObject;
import view.helpers.TimeFormatter;

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
    private long currentServiceDesired;

    // Room change state
    private int selectedRoomChangeRoom;
    private int selectedRoomChangeFloor;
    private int selectedRoomChangeTower;

    private static final Logger logger = Logger.getLogger(RoomManager.class.getName());

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

                    JSONArray customTimeArr = roomJson.optJSONArray("customTimeData");
                    if (customTimeArr != null && customTimeArr.length() > 0) {
                        RoomTime[] timeData = new RoomTime[customTimeArr.length()];
                        for (int t = 0; t < customTimeArr.length(); t++) {
                            JSONObject td = customTimeArr.getJSONObject(t);
                            timeData[t] = new RoomTime(td.getLong("price"), td.getLong("timeSeconds"));
                        }
                        currentRoom.setCustomRoomTimeData(timeData);
                    }

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
                        long serviceDuration = room.has("serviceDuration")
                                ? room.getLong("serviceDuration")
                                : (long) room.getInt("service") * 3600L;
                        targetRoom.setRoomStatus(RoomStatus.OCCUPIED,
                                ZonedDateTime.parse(room.getString("startStatus")).toInstant(),
                                serviceDuration);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid status: " + status);
                }
                long extensionDuration = room.has("extensionDuration")
                        ? room.getLong("extensionDuration")
                        : (long) room.getInt("extension") * 3600L;
                if (extensionDuration != 0) {
                    targetRoom.extendRoomTime(extensionDuration);
                }
            } else {
                logger.log(Level.WARNING, "Room not found during restore: " + roomString + " (Tower: " + towerNum
                        + ", Floor: " + floor + ", Room: " + roomNum + ")");
            }
        }
    }

    // ========== Room Operations ==========

    /**
     * Books a room or extends time. Returns the extension amount (0 for new bookings).
     */
    public long registerRoomTimeAdded(int tower, int floor, int room, long serviceDuration, Instant currentTime) {
        Room targetRoom = rooms.get(tower).get(floor).get(room);
        RoomStatus currentStatus = targetRoom.getStatus();
        long currentExtension = 0;
        if (currentStatus == RoomStatus.OCCUPIED) {
            targetRoom.extendRoomTime(serviceDuration);
            currentExtension = serviceDuration;
        } else {
            targetRoom.setRoomStatus(RoomStatus.OCCUPIED, currentTime, serviceDuration);
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
     *
     * @param currentTime the current time used to set the source room to CLEANING
     * @return true if the change was valid, false if the target room is occupied
     */
    public boolean changeRoomTimeToAnother(Instant currentTime) {
        Room currentRoom = rooms.get(currentTowerViewed).get(currentFloorViewed).get(currentRoomViewed);
        Room desiredChangeRoom = rooms.get(selectedRoomChangeTower)
                .get(selectedRoomChangeFloor).get(selectedRoomChangeRoom);
        if (desiredChangeRoom.getStatus() == RoomStatus.OCCUPIED) {
            return false;
        }
        long currentService = currentRoom.getServiceDuration();
        long currentTotalExtension = currentRoom.getExtensionDuration();
        Instant currentStartTime = currentRoom.getStartStatus();
        desiredChangeRoom.setRoomStatus(RoomStatus.OCCUPIED, currentStartTime, currentService);
        desiredChangeRoom.setExtensionDuration(currentTotalExtension);
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
        ZonedDateTime start = startStatus.atZone(zoneID);
        return TimeFormatter.formatTime(start);
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
                    currentRoom.put("serviceDuration", room.getServiceDuration());

                    Instant startStatus = room.getStartStatus();
                    currentRoom.put("startStatus", startStatus == null ? "" : startStatus.atZone(zoneID).toString());

                    Instant endStatus = room.getEndStatus();
                    currentRoom.put("endStatus", endStatus == null ? "" : endStatus.atZone(zoneID).toString());

                    currentRoom.put("extensionDuration", room.getExtensionDuration());

                    roomDataArray.put(currentRoom);
                }
            }
        }
        return roomDataArray;
    }

    // ========== Room Configuration CRUD ==========

    /**
     * Sets custom time pricing for a specific room.
     * @param tower    tower index
     * @param floor    floor index
     * @param room     room index
     * @param timeData array of 3 RoomTime instances
     */
    public void setRoomCustomTimeData(int tower, int floor, int room, RoomTime[] timeData) {
        if (tower >= 0 && tower < rooms.size()
                && floor >= 0 && floor < rooms.get(tower).size()
                && room >= 0 && room < rooms.get(tower).get(floor).size()) {
            rooms.get(tower).get(floor).get(room).setCustomRoomTimeData(timeData);
        }
    }

    /**
     * Renames a room.
     * @param tower      tower index
     * @param floor      floor index
     * @param room       room index
     * @param roomString new display identifier
     */
    public void setRoomString(int tower, int floor, int room, String roomString) {
        if (tower >= 0 && tower < rooms.size()
                && floor >= 0 && floor < rooms.get(tower).size()
                && room >= 0 && room < rooms.get(tower).get(floor).size()) {
            Room target = rooms.get(tower).get(floor).get(room);
            target.setRoomString(roomString);
        }
    }

    /**
     * @return total number of towers in the room grid
     */
    public int getTotalTowers() {
        return rooms.size();
    }

    /**
     * @param tower tower index
     * @return number of floors in the given tower, or 0 if out of bounds
     */
    public int getTotalFloors(int tower) {
        if (tower >= 0 && tower < rooms.size()) {
            return rooms.get(tower).size();
        }
        return 0;
    }

    // ========== Room Grid Rebuilding ==========

    /**
     * Clears and rebuilds the entire room grid from a program configuration JSON,
     * preserving existing room states (status, times, custom data). Rooms are
     * matched by their {@code (towerNumber, floorNumber, roomNumber)} triplet
     * from the configuration data. Rooms that no longer exist in the new config
     * are dropped; new rooms start as FREE.
     *
     * @param programData the application properties JSON containing roomsPerTower
     */
    public void rebuildRoomGrid(JSONObject programData) {
        JSONArray savedStates = getRoomDataForSaving();
        rooms.clear();
        buildRoomGrid(programData);
        JSONObject wrapper = new JSONObject();
        wrapper.put("rooms", savedStates);
        restoreRoomStates(wrapper);
    }

    // ========== Incremental Grid Operations ==========

    public void addTowerToGrid(int towerNum, int floors) {
        rooms.add(new ArrayList<>());
        int idx = rooms.size() - 1;
        for (int f = 0; f < floors; f++) {
            rooms.get(idx).add(new ArrayList<>());
        }
    }

    public void removeTowerFromGrid(int towerIndex) {
        if (towerIndex >= 0 && towerIndex < rooms.size()) {
            rooms.remove(towerIndex);
        }
    }

    public void addRoomToGrid(int towerIndex, int floorIndex, int floorNumber,
                              int roomNumber, String roomString, int towerNumber) {
        if (towerIndex < 0 || towerIndex >= rooms.size()) return;
        if (floorIndex < 0 || floorIndex >= rooms.get(towerIndex).size()) return;
        Room room = new Room(roomString, floorNumber, roomNumber, towerNumber);
        rooms.get(towerIndex).get(floorIndex).add(room);
    }

    public void removeRoomFromGrid(int towerIndex, int floorIndex, int roomIndex) {
        if (towerIndex < 0 || towerIndex >= rooms.size()) return;
        if (floorIndex < 0 || floorIndex >= rooms.get(towerIndex).size()) return;
        if (roomIndex < 0 || roomIndex >= rooms.get(towerIndex).get(floorIndex).size()) return;
        rooms.get(towerIndex).get(floorIndex).remove(roomIndex);
    }

    public void addFloorToGrid(int towerIndex, int floorIndex) {
        if (towerIndex < 0 || towerIndex >= rooms.size()) return;
        rooms.get(towerIndex).add(floorIndex, new ArrayList<>());
    }

    public void removeFloorFromGrid(int towerIndex, int floorIndex) {
        if (towerIndex < 0 || towerIndex >= rooms.size()) return;
        if (floorIndex < 0 || floorIndex >= rooms.get(towerIndex).size()) return;
        rooms.get(towerIndex).remove(floorIndex);
    }

    // ========== Getters / Setters ==========

    public int getCurrentFloorViewed() { return currentFloorViewed; }
    public int getCurrentRoomViewed() { return currentRoomViewed; }
    public int getCurrentTowerViewed() { return currentTowerViewed; }
    public long getCurrentServiceDesired() { return currentServiceDesired; }

    public void setCurrentServiceDesired(long service) { this.currentServiceDesired = service; }

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
