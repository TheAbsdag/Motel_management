package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.json.AppProperties;
import model.json.FloorConfig;
import model.json.ObjectMapperFactory;
import model.json.RoomConfigData;
import model.json.TimeSlotConfig;
import model.json.TowerConfig;

/**
 * Manages application configuration data (motel info, printer, transaction counter)
 * and the room grid structure (towers, floors, rooms).
 *
 * @author Santiago
 */
public class ProgramConfig {

    private static final int SCHEMA_VERSION = 2;
    private static final Logger logger = Logger.getLogger(ProgramConfig.class.getName());

    private int consecutiveTransaction;
    private String motelName;
    private String motelAddress;
    private String motelID;
    private String configuredPrinterName;
    private int schemaVersion;
    private List<TowerConfig> roomsPerTower;

    public ProgramConfig() {
        this.consecutiveTransaction = 0;
        this.schemaVersion = SCHEMA_VERSION;
        this.roomsPerTower = new ArrayList<>();
    }

    public void loadFromJson(String json) {
        try {
            AppProperties props = ObjectMapperFactory.get().readValue(json, AppProperties.class);
            this.consecutiveTransaction = props.consecutiveTransaction();
            this.motelName = props.motelName();
            this.motelAddress = props.motelAddress();
            this.motelID = props.motelID();
            this.configuredPrinterName = props.printerName();
            this.schemaVersion = props.version();
            this.roomsPerTower = new ArrayList<>(props.roomsPerTower());
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to load application properties", e);
        }
    }

    public String toJson() {
        try {
            AppProperties props = new AppProperties(
                    consecutiveTransaction, motelName, motelAddress, motelID,
                    configuredPrinterName, schemaVersion, roomsPerTower);
            return ObjectMapperFactory.get().writeValueAsString(props);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to serialize application properties", e);
            return "{}";
        }
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public boolean isLegacyFormat() {
        return schemaVersion < 1;
    }

    public void ensureSchemaVersion() {
        this.schemaVersion = SCHEMA_VERSION;
    }

    public void addConsecutiveTransaction() {
        consecutiveTransaction++;
    }

    public int getConsecutiveTransaction() {
        return consecutiveTransaction;
    }

    public void savePrinterConfiguration(String printerName) {
        configuredPrinterName = printerName;
    }

    public String getConfiguredPrinterName() {
        return configuredPrinterName;
    }

    public String getMotelName() { return motelName; }
    public String getMotelAddress() { return motelAddress; }
    public String getMotelID() { return motelID; }

    public void setMotelName(String motelName) { this.motelName = motelName; }
    public void setMotelAddress(String motelAddress) { this.motelAddress = motelAddress; }
    public void setMotelID(String motelID) { this.motelID = motelID; }

    // ========== Room Grid Configuration ==========

    public List<TowerConfig> getRoomsPerTower() {
        return roomsPerTower;
    }

    public void setRoomsPerTower(List<TowerConfig> roomsPerTower) {
        this.roomsPerTower = roomsPerTower;
    }

    public int getTowerCount() {
        return roomsPerTower.size();
    }

    public TowerConfig getTower(int index) {
        if (index < 0 || index >= roomsPerTower.size()) {
            return null;
        }
        return roomsPerTower.get(index);
    }

    public void addTower(int towerNumber, int towerFloors, List<FloorConfig> towerRooms) {
        TowerConfig tower = new TowerConfig(towerNumber, towerFloors, towerRooms);
        roomsPerTower.add(tower);
    }

    public void removeTower(int index) {
        if (index >= 0 && index < roomsPerTower.size()) {
            roomsPerTower.remove(index);
        }
    }

    public void addFloorToTower(int towerIndex, int floorNumber, int roomCount) {
        if (towerIndex < 0 || towerIndex >= roomsPerTower.size()) return;

        TowerConfig tower = roomsPerTower.get(towerIndex);
        int towerFloors = tower.towerFloors() + 1;
        List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());

        List<RoomConfigData> rooms = new ArrayList<>();
        for (int i = 0; i < roomCount; i++) {
            rooms.add(new RoomConfigData(
                    buildRoomString(towerNumberFromIndex(towerIndex), floorNumber, i),
                    floorNumber, i, defaultTimeData()));
        }
        FloorConfig floorData = new FloorConfig(floorNumber, rooms);
        towerRooms.add(floorData);

        roomsPerTower.set(towerIndex, new TowerConfig(tower.towerNumber(), towerFloors, towerRooms));
    }

    public void removeFloorFromTower(int towerIndex, int floorDataIndex) {
        if (towerIndex < 0 || towerIndex >= roomsPerTower.size()) return;
        TowerConfig tower = roomsPerTower.get(towerIndex);
        List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());
        if (floorDataIndex >= 0 && floorDataIndex < towerRooms.size()) {
            towerRooms.remove(floorDataIndex);
            roomsPerTower.set(towerIndex, new TowerConfig(tower.towerNumber(), tower.towerFloors() - 1, towerRooms));
        }
    }

    public void addRoomToFloor(int towerIndex, int floorDataIndex, String roomString, int floorNumber, int roomNumber) {
        if (towerIndex < 0 || towerIndex >= roomsPerTower.size()) return;
        TowerConfig tower = roomsPerTower.get(towerIndex);
        List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());
        if (floorDataIndex < 0 || floorDataIndex >= towerRooms.size()) return;

        FloorConfig floorData = towerRooms.get(floorDataIndex);
        List<RoomConfigData> rooms = new ArrayList<>(floorData.rooms());
        rooms.add(new RoomConfigData(roomString, floorNumber, roomNumber, defaultTimeData()));

        towerRooms.set(floorDataIndex, new FloorConfig(floorData.floor(), rooms));
        roomsPerTower.set(towerIndex, new TowerConfig(tower.towerNumber(), tower.towerFloors(), towerRooms));
    }

    public void removeRoomFromFloor(int towerIndex, int floorDataIndex, int roomIndex) {
        if (towerIndex < 0 || towerIndex >= roomsPerTower.size()) return;
        TowerConfig tower = roomsPerTower.get(towerIndex);
        List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());
        if (floorDataIndex < 0 || floorDataIndex >= towerRooms.size()) return;

        FloorConfig floorData = towerRooms.get(floorDataIndex);
        List<RoomConfigData> rooms = new ArrayList<>(floorData.rooms());
        if (roomIndex >= 0 && roomIndex < rooms.size()) {
            rooms.remove(roomIndex);
            towerRooms.set(floorDataIndex, new FloorConfig(floorData.floor(), rooms));
            roomsPerTower.set(towerIndex, new TowerConfig(tower.towerNumber(), tower.towerFloors(), towerRooms));
        }
    }

    public void setRoomString(int towerIndex, int floorDataIndex, int roomIndex, String newRoomString) {
        if (towerIndex < 0 || towerIndex >= roomsPerTower.size()) return;
        TowerConfig tower = roomsPerTower.get(towerIndex);
        List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());
        if (floorDataIndex < 0 || floorDataIndex >= towerRooms.size()) return;

        FloorConfig floorData = towerRooms.get(floorDataIndex);
        List<RoomConfigData> rooms = new ArrayList<>(floorData.rooms());
        if (roomIndex >= 0 && roomIndex < rooms.size()) {
            RoomConfigData old = rooms.get(roomIndex);
            rooms.set(roomIndex, new RoomConfigData(newRoomString, old.roomFloor(), old.roomNumber(), old.customTimeData()));
            towerRooms.set(floorDataIndex, new FloorConfig(floorData.floor(), rooms));
            roomsPerTower.set(towerIndex, new TowerConfig(tower.towerNumber(), tower.towerFloors(), towerRooms));
        }
    }

    private int towerNumberFromIndex(int towerIndex) {
        if (towerIndex >= 0 && towerIndex < roomsPerTower.size()) {
            return roomsPerTower.get(towerIndex).towerNumber();
        }
        return towerIndex + 1;
    }

    public List<TimeSlotConfig> defaultTimeData() {
        RoomTime[] defaults = RoomTime.getDefaultTimeSlots();
        List<TimeSlotConfig> list = new ArrayList<>();
        for (RoomTime rt : defaults) {
            list.add(new TimeSlotConfig(rt.getPrice(), rt.getTimeSeconds()));
        }
        return list;
    }

    public static String buildRoomString(int towerNumber, int floorNumber, int roomNumber) {
        return towerNumber + "-" + (floorNumber + 1) + "0" + (roomNumber + 1);
    }
}
