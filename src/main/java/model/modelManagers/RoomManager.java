package model.modelManagers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Room;
import model.RoomStatus;
import model.RoomTime;
import model.json.FloorConfig;
import model.json.ObjectMapperFactory;
import model.json.RoomConfigData;
import model.json.RoomStateData;
import model.json.TimeSlotConfig;
import model.json.TowerConfig;
import view.helpers.TimeFormatter;

public class RoomManager {

    private final ArrayList<ArrayList<ArrayList<Room>>> rooms;
    private final ZoneId zoneID;
    private final Room reception;

    private int currentFloorViewed;
    private int currentRoomViewed;
    private int currentTowerViewed;
    private long currentServiceDesired;

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

    public void buildRoomGrid(List<TowerConfig> roomsPerTower) {
        for (int towerIndex = 0; towerIndex < roomsPerTower.size(); towerIndex++) {
            TowerConfig tower = roomsPerTower.get(towerIndex);
            int towerNumber = tower.towerNumber();
            int towerFloors = tower.towerFloors();

            rooms.add(new ArrayList<>());

            for (int floorIndex = 0; floorIndex < towerFloors; floorIndex++) {
                rooms.get(towerIndex).add(new ArrayList<>());
            }

            List<FloorConfig> towerRooms = tower.towerRooms();

            for (int floorDataIndex = 0; floorDataIndex < towerRooms.size(); floorDataIndex++) {
                FloorConfig floorData = towerRooms.get(floorDataIndex);
                int floorNumber = floorData.floor();
                List<RoomConfigData> roomsArray = floorData.rooms();

                for (int roomIndex = 0; roomIndex < roomsArray.size(); roomIndex++) {
                    RoomConfigData roomJson = roomsArray.get(roomIndex);
                    String roomString = roomJson.roomString();
                    int roomFloor = roomJson.roomFloor();
                    int roomNumber = roomJson.roomNumber();

                    Room currentRoom = new Room(roomString, roomFloor, roomNumber, towerNumber);

                    List<TimeSlotConfig> customTimeArr = roomJson.customTimeData();
                    if (customTimeArr != null && !customTimeArr.isEmpty()) {
                        RoomTime[] timeData = new RoomTime[customTimeArr.size()];
                        for (int t = 0; t < customTimeArr.size(); t++) {
                            TimeSlotConfig td = customTimeArr.get(t);
                            timeData[t] = new RoomTime(td.price(), td.timeSeconds());
                        }
                        currentRoom.setCustomRoomTimeData(timeData);
                    }

                    rooms.get(towerIndex).get(floorNumber).add(currentRoom);
                }
            }
        }
    }

    public void restoreRoomStates(String json) {
        if (json == null || json.isEmpty()) {
            return;
        }
        List<RoomStateData> roomsStateList;
        try {
            RoomStatesWrapper wrapper = ObjectMapperFactory.get().readValue(json, RoomStatesWrapper.class);
            roomsStateList = wrapper.rooms();
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to parse room states", e);
            return;
        }
        if (roomsStateList == null) return;

        for (RoomStateData roomState : roomsStateList) {
            String roomString = roomState.roomString();
            int towerNum = roomState.towerNumber();
            int floor = roomState.floorNumber();
            int roomNum = roomState.roomNumber();
            int status = roomState.status();
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
                        if (!roomState.startStatus().isEmpty()) {
                            targetRoom.setRoomStatus(RoomStatus.CLEANING,
                                    ZonedDateTime.parse(roomState.startStatus()).toInstant());
                        } else {
                            targetRoom.setRoomStatus(RoomStatus.CLEANING, Instant.now());
                        }
                        break;
                    case OCCUPIED:
                        long serviceDuration = roomState.serviceDuration();
                        targetRoom.setRoomStatus(RoomStatus.OCCUPIED,
                                ZonedDateTime.parse(roomState.startStatus()).toInstant(),
                                serviceDuration);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid status: " + status);
                }
                long extensionDuration = roomState.extensionDuration();
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

    public void registerRoomTimeEnd(int tower, int floor, int room, Instant currentTime) {
        Room targetRoom = rooms.get(tower).get(floor).get(room);
        if (targetRoom.getStatus() == RoomStatus.CLEANING) {
            targetRoom.setRoomStatus(RoomStatus.FREE);
        } else {
            targetRoom.setRoomStatus(RoomStatus.CLEANING, currentTime);
        }
    }

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

    public Room getRoomForSale() {
        if (currentFloorViewed == -1) {
            return reception;
        }
        return rooms.get(currentTowerViewed).get(currentFloorViewed).get(currentRoomViewed);
    }

    public Room getCurrentRoom() {
        return rooms.get(currentTowerViewed).get(currentFloorViewed).get(currentRoomViewed);
    }

    public Room getDesiredChangeRoom() {
        return rooms.get(selectedRoomChangeTower).get(selectedRoomChangeFloor).get(selectedRoomChangeRoom);
    }

    // ========== Room Data for Persistence ==========

    public String getRoomDataForSaving() {
        List<RoomStateData> roomList = new ArrayList<>();
        for (int tower = 0; tower < rooms.size(); tower++) {
            for (int floor = 0; floor < rooms.get(tower).size(); floor++) {
                for (int roomIndex = 0; roomIndex < rooms.get(tower).get(floor).size(); roomIndex++) {
                    Room room = rooms.get(tower).get(floor).get(roomIndex);
                    Instant startStatus = room.getStartStatus();
                    Instant endStatus = room.getEndStatus();
                    roomList.add(new RoomStateData(
                            room.getRoomString(),
                            tower,
                            room.getFloorNumber(),
                            room.getRoomNumber(),
                            room.getStatus().getCode(),
                            room.getServiceDuration(),
                            startStatus == null ? "" : startStatus.atZone(zoneID).toString(),
                            endStatus == null ? "" : endStatus.atZone(zoneID).toString(),
                            room.getExtensionDuration()));
                }
            }
        }
        try {
            RoomStatesWrapper wrapper = new RoomStatesWrapper(roomList, 2);
            return ObjectMapperFactory.get().writeValueAsString(wrapper);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to serialize room data", e);
            return "{}";
        }
    }

    // ========== Room Configuration CRUD ==========

    public void setRoomCustomTimeData(int tower, int floor, int room, RoomTime[] timeData) {
        if (tower >= 0 && tower < rooms.size()
                && floor >= 0 && floor < rooms.get(tower).size()
                && room >= 0 && room < rooms.get(tower).get(floor).size()) {
            rooms.get(tower).get(floor).get(room).setCustomRoomTimeData(timeData);
        }
    }

    public void setRoomString(int tower, int floor, int room, String roomString) {
        if (tower >= 0 && tower < rooms.size()
                && floor >= 0 && floor < rooms.get(tower).size()
                && room >= 0 && room < rooms.get(tower).get(floor).size()) {
            Room target = rooms.get(tower).get(floor).get(room);
            target.setRoomString(roomString);
        }
    }

    public int getTotalTowers() {
        return rooms.size();
    }

    public int getTotalFloors(int tower) {
        if (tower >= 0 && tower < rooms.size()) {
            return rooms.get(tower).size();
        }
        return 0;
    }

    // ========== Room Grid Rebuilding ==========

    public void rebuildRoomGrid(List<TowerConfig> roomsPerTower) {
        String savedStates = getRoomDataForSaving();
        rooms.clear();
        buildRoomGrid(roomsPerTower);
        restoreRoomStates(savedStates);
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

    // ========== Room States Wrapper ==========

    public record RoomStatesWrapper(
            java.util.List<RoomStateData> rooms,
            int version
    ) {}
}
