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
import model.json.CurrencyConfig;
import model.json.TowerConfig;

/**
 * Manages application configuration data (motel info, printer, transaction counter)
 * and the room grid structure (towers, floors, rooms).
 *
 * @author Santiago
 */
public class ProgramConfig {

    private static final int SCHEMA_VERSION = 3;
    private static final Logger logger = Logger.getLogger(ProgramConfig.class.getName());

    private int consecutiveTransaction;
    private String motelName;
    private String motelAddress;
    private String motelID;
    private String configuredPrinterName;
    private int schemaVersion;
    private CurrencyConfig currencyConfig;
    private List<TowerConfig> roomsPerTower;
    private boolean keypadEnabled;

    public ProgramConfig() {
        this.consecutiveTransaction = 0;
        this.schemaVersion = SCHEMA_VERSION;
        this.currencyConfig = CurrencyConfig.defaultConfig();
        this.roomsPerTower = new ArrayList<>();
        this.keypadEnabled = true;
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
            this.currencyConfig = props.currencyConfig() != null ? props.currencyConfig() : CurrencyConfig.defaultConfig();
            this.keypadEnabled = props.keypadEnabled() == null || props.keypadEnabled();
            migrateIfNeeded();
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to load application properties", e);
        }
    }

    /**
     * Migrates persisted data from older schema versions to the current version.
     * <p>
     * Schema v2 → v3: TowerConfig.towerNumber changed from 1-based to 0-based.
     * Each tower number is decremented by 1.
     */
    private void migrateIfNeeded() {
        if (schemaVersion >= SCHEMA_VERSION) return;
        if (schemaVersion < 3) {
            List<TowerConfig> migrated = new ArrayList<>();
            for (TowerConfig tower : roomsPerTower) {
                migrated.add(new TowerConfig(
                        tower.towerNumber() - 1,
                        tower.towerFloors(),
                        tower.towerRooms(),
                        tower.defaultTimeData()));
            }
            roomsPerTower = migrated;
        }
        schemaVersion = SCHEMA_VERSION;
    }

    public String toJson() {
        try {
            AppProperties props = new AppProperties(
                    consecutiveTransaction, motelName, motelAddress, motelID,
                    configuredPrinterName, schemaVersion, roomsPerTower, currencyConfig, keypadEnabled);
            return ObjectMapperFactory.get().writeValueAsString(props);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to serialize application properties", e);
            return "{}";
        }
    }

    // ========== Version ==========

    /**
     * @return the schema version loaded from disk, or 0 for legacy files
     */
    public int getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * @return true if the loaded data file lacks a version field (pre-v1 format)
     */
    public boolean isLegacyFormat() {
        return schemaVersion < 1;
    }

    /**
     * Ensures the version field is written on next save.
     */
    public void ensureSchemaVersion() {
        this.schemaVersion = SCHEMA_VERSION;
    }

    // ========== Transaction Counter ==========

    public void addConsecutiveTransaction() {
        consecutiveTransaction++;
    }

    public int getConsecutiveTransaction() {
        return consecutiveTransaction;
    }

    // ========== Printer Configuration ==========

    public void savePrinterConfiguration(String printerName) {
        configuredPrinterName = printerName;
    }

    public String getConfiguredPrinterName() {
        return configuredPrinterName;
    }

    // ========== Property Getters / Setters ==========

    public String getMotelName() { return motelName; }
    public String getMotelAddress() { return motelAddress; }
    public String getMotelID() { return motelID; }

    public void setMotelName(String motelName) { this.motelName = motelName; }
    public void setMotelAddress(String motelAddress) { this.motelAddress = motelAddress; }
    public void setMotelID(String motelID) { this.motelID = motelID; }

    // ========== Room Grid Configuration ==========

    /**
     * @return the list of tower configurations
     */
    public List<TowerConfig> getRoomsPerTower() {
        return roomsPerTower;
    }

    /**
     * Replaces the full roomsPerTower structure.
     * @param roomsPerTower new tower/floor/room layout
     */
    public void setRoomsPerTower(List<TowerConfig> roomsPerTower) {
        this.roomsPerTower = roomsPerTower;
    }

    /**
     * @return number of towers in the current configuration
     */
    public int getTowerCount() {
        return roomsPerTower.size();
    }

    /**
     * @param index tower index
     * @return the tower configuration at the given index, or null if out of bounds
     */
    public TowerConfig getTower(int index) {
        if (index < 0 || index >= roomsPerTower.size()) {
            return null;
        }
        return roomsPerTower.get(index);
    }

    /**
     * Appends a new tower to the configuration.
     * @param towerNumber identifier for the tower
     * @param towerFloors  number of floors in the tower
     * @param towerRooms   pre-built rooms list per floor
     */
    public void addTower(int towerNumber, int towerFloors, List<FloorConfig> towerRooms) {
        TowerConfig tower = new TowerConfig(towerNumber, towerFloors, towerRooms);
        roomsPerTower.add(tower);
    }

    /**
     * Removes a tower and all its floors/rooms from the configuration.
     * @param index tower index to remove
     */
    public void removeTower(int index) {
        if (index >= 0 && index < roomsPerTower.size()) {
            roomsPerTower.remove(index);
        }
    }

    /**
     * Adds a new empty floor to a tower.
     * @param towerIndex  target tower index
     * @param floorNumber floor identifier
     * @param roomCount   initial number of rooms to create
     */
    public void addFloorToTower(int towerIndex, int floorNumber, int roomCount) {
        if (towerIndex < 0 || towerIndex >= roomsPerTower.size()) return;

        TowerConfig tower = roomsPerTower.get(towerIndex);
        int towerFloors = tower.towerFloors() + 1;
        List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());

        List<TimeSlotConfig> towerDefault = towerDefaultTimeData(towerIndex);
        List<RoomConfigData> rooms = new ArrayList<>();
        for (int i = 0; i < roomCount; i++) {
            rooms.add(new RoomConfigData(
                    buildRoomString(towerNumberFromIndex(towerIndex), floorNumber, i),
                    floorNumber, i, towerDefault));
        }
        FloorConfig floorData = new FloorConfig(floorNumber, rooms);
        towerRooms.add(floorData);

        roomsPerTower.set(towerIndex, new TowerConfig(
                tower.towerNumber(), towerFloors, towerRooms, tower.defaultTimeData()));
    }

    /**
     * Removes a floor and all its rooms from a tower.
     * @param towerIndex     target tower index
     * @param floorDataIndex index within the tower's towerRooms list
     */
    public void removeFloorFromTower(int towerIndex, int floorDataIndex) {
        if (towerIndex < 0 || towerIndex >= roomsPerTower.size()) return;
        TowerConfig tower = roomsPerTower.get(towerIndex);
        List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());
        if (floorDataIndex >= 0 && floorDataIndex < towerRooms.size()) {
            towerRooms.remove(floorDataIndex);
            roomsPerTower.set(towerIndex, new TowerConfig(
                    tower.towerNumber(), tower.towerFloors() - 1, towerRooms, tower.defaultTimeData()));
        }
    }

    /**
     * Appends a new room to a floor's room list.
     * @param towerIndex     target tower index
     * @param floorDataIndex index within the tower's towerRooms list
     * @param roomString     display identifier (e.g. "1-105")
     * @param floorNumber    floor number within the tower
     * @param roomNumber     room number within the floor
     */
    public void addRoomToFloor(int towerIndex, int floorDataIndex, String roomString, int floorNumber, int roomNumber) {
        if (towerIndex < 0 || towerIndex >= roomsPerTower.size()) return;
        TowerConfig tower = roomsPerTower.get(towerIndex);
        List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());
        if (floorDataIndex < 0 || floorDataIndex >= towerRooms.size()) return;

        FloorConfig floorData = towerRooms.get(floorDataIndex);
        List<RoomConfigData> rooms = new ArrayList<>(floorData.rooms());
        rooms.add(new RoomConfigData(roomString, floorNumber, roomNumber, towerDefaultTimeData(towerIndex)));

        towerRooms.set(floorDataIndex, new FloorConfig(floorData.floor(), rooms));
        roomsPerTower.set(towerIndex, tower.withTowerRooms(towerRooms));
    }

    /**
     * Removes a specific room from a floor.
     * @param towerIndex     target tower index
     * @param floorDataIndex index within the tower's towerRooms list
     * @param roomIndex      room index within the floor's rooms list
     */
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
            roomsPerTower.set(towerIndex, tower.withTowerRooms(towerRooms));
        }
    }

    /**
     * Renames a room in the persistent configuration.
     * @param towerIndex     target tower index
     * @param floorDataIndex index within the tower's towerRooms list
     * @param roomIndex      room index within the floor
     * @param newRoomString  new display identifier
     */
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
            roomsPerTower.set(towerIndex, tower.withTowerRooms(towerRooms));
        }
    }

    private int towerNumberFromIndex(int towerIndex) {
        if (towerIndex >= 0 && towerIndex < roomsPerTower.size()) {
            return roomsPerTower.get(towerIndex).towerNumber();
        }
        return towerIndex + 1;
    }

    // ========== Room Time Data ==========

    /**
     * The built-in time and price slots a room starts from when nothing else is configured.
     *
     * @return 3 time slots (3 h / 12 h / 24 h)
     */
    public static List<TimeSlotConfig> defaultTimeData() {
        return toTimeSlotConfigs(RoomTime.getDefaultTimeSlots());
    }

    /**
     * Converts runtime time slots into their persisted form.
     *
     * @param slots room time slots, possibly null
     * @return the same values as config records
     */
    public static List<TimeSlotConfig> toTimeSlotConfigs(RoomTime[] slots) {
        List<TimeSlotConfig> list = new ArrayList<>();
        if (slots == null) return list;
        for (RoomTime slot : slots) {
            list.add(new TimeSlotConfig(slot.getPrice(), slot.getTimeSeconds()));
        }
        return list;
    }

    /**
     * Converts persisted time slots into runtime objects.
     *
     * @param slots config records, possibly null or empty
     * @return the same values as room time slots
     */
    public static RoomTime[] toRoomTimes(List<TimeSlotConfig> slots) {
        if (slots == null || slots.isEmpty()) return new RoomTime[0];
        RoomTime[] times = new RoomTime[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            times[i] = new RoomTime(slots.get(i).price(), slots.get(i).timeSeconds());
        }
        return times;
    }

    /**
     * Time and price a new room on this tower starts from: the per-tower default set in
     * the floor configuration, or the built-in defaults when the tower has none.
     *
     * @param towerIndex index within {@link #getRoomsPerTower()}
     * @return 3 time slots
     */
    public List<TimeSlotConfig> towerDefaultTimeData(int towerIndex) {
        if (towerIndex >= 0 && towerIndex < roomsPerTower.size()) {
            List<TimeSlotConfig> stored = roomsPerTower.get(towerIndex).defaultTimeData();
            if (stored != null && !stored.isEmpty()) {
                return stored;
            }
        }
        return defaultTimeData();
    }

    /**
     * Stores the time and price a new room on this tower starts from.
     *
     * @param towerIndex index within {@link #getRoomsPerTower()}
     * @param timeData   3 time slots
     */
    public void setTowerDefaultTimeData(int towerIndex, List<TimeSlotConfig> timeData) {
        if (towerIndex < 0 || towerIndex >= roomsPerTower.size()) return;
        roomsPerTower.set(towerIndex, roomsPerTower.get(towerIndex).withDefaultTimeData(timeData));
    }

    /**
     * Copies every room's runtime time pricing into the room configuration, which is what
     * a save writes. The runtime grid is the source of truth, so a price edited in the
     * room configuration screen replaces the value stored when the room was created
     * instead of being dropped on the next save.
     *
     * @param runtimeRooms rooms by tower, floor and room, as held by the room grid
     */
    public void syncRoomTimeData(ArrayList<ArrayList<ArrayList<Room>>> runtimeRooms) {
        for (int t = 0; t < roomsPerTower.size() && t < runtimeRooms.size(); t++) {
            TowerConfig tower = roomsPerTower.get(t);
            List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());
            for (int fd = 0; fd < towerRooms.size(); fd++) {
                FloorConfig floorData = towerRooms.get(fd);
                int floorNumber = floorData.floor();
                if (floorNumber >= runtimeRooms.get(t).size()) continue;

                List<RoomConfigData> configRooms = new ArrayList<>(floorData.rooms());
                ArrayList<Room> runtimeFloorRooms = runtimeRooms.get(t).get(floorNumber);
                for (int r = 0; r < configRooms.size() && r < runtimeFloorRooms.size(); r++) {
                    RoomConfigData roomJson = configRooms.get(r);
                    configRooms.set(r, new RoomConfigData(
                            roomJson.roomString(), roomJson.roomFloor(), roomJson.roomNumber(),
                            toTimeSlotConfigs(runtimeFloorRooms.get(r).getCustomRoomTimeData())));
                }
                towerRooms.set(fd, new FloorConfig(floorNumber, configRooms));
            }
            roomsPerTower.set(t, tower.withTowerRooms(towerRooms));
        }
    }

    // ========== Currency Configuration ==========

    public CurrencyConfig getCurrencyConfig() { return currencyConfig; }

    public void setCurrencyConfig(CurrencyConfig currencyConfig) {
        this.currencyConfig = currencyConfig;
    }

    // ========== On-screen keypad ==========

    /**
     * Whether tapping a numeric field opens the on-screen keypad. Installations that have a
     * keyboard turn it off.
     *
     * @return the stored setting, enabled when the data file has none
     */
    public boolean isKeypadEnabled() { return keypadEnabled; }

    /**
     * Stores the on-screen keypad setting.
     *
     * @param keypadEnabled true to use the keypad on numeric fields
     */
    public void setKeypadEnabled(boolean keypadEnabled) { this.keypadEnabled = keypadEnabled; }

    /**
     * Builds a standard room identifier string from tower, floor, and room numbers.
     * @param towerNumber tower identifier
     * @param floorNumber zero-based floor number
     * @param roomNumber  zero-based room number
     * @return formatted string like "1-105"
     */
    public static String buildRoomString(int towerNumber, int floorNumber, int roomNumber) {
        return (towerNumber + 1) + "-" + (floorNumber + 1) + "0" + (roomNumber + 1);
    }
}
