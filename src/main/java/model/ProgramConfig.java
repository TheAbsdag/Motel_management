package model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Manages application configuration data (motel info, printer, transaction counter)
 * and the room grid structure (towers, floors, rooms).
 *
 * @author Santiago
 */
public class ProgramConfig {

    private static final int SCHEMA_VERSION = 2;

    private JSONObject programData;
    private int consecutiveTransaction;
    private String motelName;
    private String motelAddress;
    private String motelID;
    private String configuredPrinterName;
    private int schemaVersion;

    public ProgramConfig() {
        this.programData = new JSONObject();
        this.consecutiveTransaction = 0;
        this.programData.put("consecutiveTransaction", 0);
        this.schemaVersion = SCHEMA_VERSION;
    }

    // ========== Initialization ==========

    public void loadFromJson(JSONObject rawData) {
        this.programData = rawData;
        this.consecutiveTransaction = programData.optInt("consecutiveTransaction", 0);
        this.motelName = programData.getString("motelName");
        this.motelAddress = programData.getString("motelAddress");
        this.motelID = programData.getString("motelID");

        if (programData.has("printerName")) {
            this.configuredPrinterName = programData.getString("printerName");
        } else {
            this.configuredPrinterName = null;
        }

        this.schemaVersion = programData.optInt("version", 0);
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
        programData.put("version", SCHEMA_VERSION);
        this.schemaVersion = SCHEMA_VERSION;
    }

    // ========== Transaction Counter ==========

    public void addConsecutiveTransaction() {
        consecutiveTransaction++;
        programData.put("consecutiveTransaction", consecutiveTransaction);
    }

    public int getConsecutiveTransaction() {
        return consecutiveTransaction;
    }

    // ========== Printer Configuration ==========

    public void savePrinterConfiguration(String printerName) {
        programData.put("printerName", printerName);
        configuredPrinterName = printerName;
    }

    public String getConfiguredPrinterName() {
        return configuredPrinterName;
    }

    // ========== Property Getters / Setters ==========

    public String getMotelName() { return motelName; }
    public String getMotelAddress() { return motelAddress; }
    public String getMotelID() { return motelID; }
    public JSONObject getProgramData() { return programData; }

    public void setMotelName(String motelName) {
        this.motelName = motelName;
        programData.put("motelName", motelName);
    }

    public void setMotelAddress(String motelAddress) {
        this.motelAddress = motelAddress;
        programData.put("motelAddress", motelAddress);
    }

    public void setMotelID(String motelID) {
        this.motelID = motelID;
        programData.put("motelID", motelID);
    }

    // ========== Room Grid Configuration ==========

    /**
     * @return the roomsPerTower JSON array, or null if absent
     */
    public JSONArray getRoomsPerTower() {
        return programData.optJSONArray("roomsPerTower");
    }

    /**
     * Replaces the full roomsPerTower structure.
     * @param roomsPerTower new tower/floor/room layout
     */
    public void setRoomsPerTower(JSONArray roomsPerTower) {
        programData.put("roomsPerTower", roomsPerTower);
    }

    /**
     * @return number of towers in the current configuration
     */
    public int getTowerCount() {
        JSONArray arr = getRoomsPerTower();
        return arr != null ? arr.length() : 0;
    }

    /**
     * @param index tower index
     * @return the tower JSON object at the given index, or null if out of bounds
     */
    public JSONObject getTower(int index) {
        JSONArray arr = getRoomsPerTower();
        if (arr == null || index < 0 || index >= arr.length()) {
            return null;
        }
        return arr.getJSONObject(index);
    }

    /**
     * Appends a new tower to the configuration.
     * @param towerNumber identifier for the tower
     * @param towerFloors  number of floors in the tower
     * @param towerRooms   pre-built rooms array per floor
     */
    public void addTower(int towerNumber, int towerFloors, JSONArray towerRooms) {
        JSONObject tower = new JSONObject();
        tower.put("towerNumber", towerNumber);
        tower.put("towerFloors", towerFloors);
        tower.put("towerRooms", towerRooms);

        JSONArray arr = getRoomsPerTower();
        if (arr == null) {
            arr = new JSONArray();
            programData.put("roomsPerTower", arr);
        }
        arr.put(tower);
    }

    /**
     * Removes a tower and all its floors/rooms from the configuration.
     * @param index tower index to remove
     */
    public void removeTower(int index) {
        JSONArray arr = getRoomsPerTower();
        if (arr != null && index >= 0 && index < arr.length()) {
            arr.remove(index);
        }
    }

    /**
     * Adds a new empty floor to a tower.
     * @param towerIndex  target tower index
     * @param floorNumber floor identifier
     * @param roomCount   initial number of rooms to create
     */
    public void addFloorToTower(int towerIndex, int floorNumber, int roomCount) {
        JSONArray arr = getRoomsPerTower();
        if (arr == null || towerIndex < 0 || towerIndex >= arr.length()) {
            return;
        }
        JSONObject tower = arr.getJSONObject(towerIndex);
        int towerFloors = tower.getInt("towerFloors") + 1;
        tower.put("towerFloors", towerFloors);

        JSONArray towerRooms = tower.getJSONArray("towerRooms");
        JSONObject floorData = new JSONObject();
        floorData.put("floor", floorNumber);
        JSONArray rooms = new JSONArray();
        for (int i = 0; i < roomCount; i++) {
            JSONObject roomJson = new JSONObject();
            roomJson.put("roomString", buildRoomString(towerNumberFromIndex(towerIndex), floorNumber, i));
            roomJson.put("roomFloor", floorNumber);
            roomJson.put("roomNumber", i);
            roomJson.put("customTimeData", defaultTimeDataJson());
            rooms.put(roomJson);
        }
        floorData.put("rooms", rooms);
        towerRooms.put(floorData);
    }

    /**
     * Removes a floor and all its rooms from a tower.
     * @param towerIndex     target tower index
     * @param floorDataIndex index within the tower's towerRooms array
     */
    public void removeFloorFromTower(int towerIndex, int floorDataIndex) {
        JSONArray arr = getRoomsPerTower();
        if (arr == null || towerIndex < 0 || towerIndex >= arr.length()) {
            return;
        }
        JSONObject tower = arr.getJSONObject(towerIndex);
        JSONArray towerRooms = tower.getJSONArray("towerRooms");
        if (floorDataIndex >= 0 && floorDataIndex < towerRooms.length()) {
            towerRooms.remove(floorDataIndex);
            tower.put("towerFloors", tower.getInt("towerFloors") - 1);
        }
    }

    /**
     * Appends a new room to a floor's room list.
     * @param towerIndex     target tower index
     * @param floorDataIndex index within the tower's towerRooms array
     * @param roomString     display identifier (e.g. "1-105")
     * @param floorNumber    floor number within the tower
     * @param roomNumber     room number within the floor
     */
    public void addRoomToFloor(int towerIndex, int floorDataIndex, String roomString, int floorNumber, int roomNumber) {
        JSONArray arr = getRoomsPerTower();
        if (arr == null || towerIndex < 0 || towerIndex >= arr.length()) {
            return;
        }
        JSONObject tower = arr.getJSONObject(towerIndex);
        JSONArray towerRooms = tower.getJSONArray("towerRooms");
        if (floorDataIndex < 0 || floorDataIndex >= towerRooms.length()) {
            return;
        }
        JSONObject floorData = towerRooms.getJSONObject(floorDataIndex);
        JSONArray rooms = floorData.getJSONArray("rooms");
        JSONObject roomJson = new JSONObject();
        roomJson.put("roomString", roomString);
        roomJson.put("roomFloor", floorNumber);
        roomJson.put("roomNumber", roomNumber);
        roomJson.put("customTimeData", defaultTimeDataJson());
        rooms.put(roomJson);
    }

    /**
     * Removes a specific room from a floor.
     * @param towerIndex     target tower index
     * @param floorDataIndex index within the tower's towerRooms array
     * @param roomIndex      room index within the floor's rooms array
     */
    public void removeRoomFromFloor(int towerIndex, int floorDataIndex, int roomIndex) {
        JSONArray arr = getRoomsPerTower();
        if (arr == null || towerIndex < 0 || towerIndex >= arr.length()) {
            return;
        }
        JSONObject tower = arr.getJSONObject(towerIndex);
        JSONArray towerRooms = tower.getJSONArray("towerRooms");
        if (floorDataIndex < 0 || floorDataIndex >= towerRooms.length()) {
            return;
        }
        JSONObject floorData = towerRooms.getJSONObject(floorDataIndex);
        JSONArray rooms = floorData.getJSONArray("rooms");
        if (roomIndex >= 0 && roomIndex < rooms.length()) {
            rooms.remove(roomIndex);
        }
    }

    /**
     * Renames a room in the persistent configuration.
     * @param towerIndex     target tower index
     * @param floorDataIndex index within the tower's towerRooms array
     * @param roomIndex      room index within the floor
     * @param newRoomString  new display identifier
     */
    public void setRoomString(int towerIndex, int floorDataIndex, int roomIndex, String newRoomString) {
        JSONArray arr = getRoomsPerTower();
        if (arr == null || towerIndex < 0 || towerIndex >= arr.length()) {
            return;
        }
        JSONObject tower = arr.getJSONObject(towerIndex);
        JSONArray towerRooms = tower.getJSONArray("towerRooms");
        if (floorDataIndex < 0 || floorDataIndex >= towerRooms.length()) {
            return;
        }
        JSONObject floorData = towerRooms.getJSONObject(floorDataIndex);
        JSONArray rooms = floorData.getJSONArray("rooms");
        if (roomIndex >= 0 && roomIndex < rooms.length()) {
            rooms.getJSONObject(roomIndex).put("roomString", newRoomString);
        }
    }

    private int towerNumberFromIndex(int towerIndex) {
        JSONArray arr = getRoomsPerTower();
        if (arr != null && towerIndex >= 0 && towerIndex < arr.length()) {
            return arr.getJSONObject(towerIndex).getInt("towerNumber");
        }
        return towerIndex + 1;
    }

    private JSONArray defaultTimeDataJson() {
        RoomTime[] defaults = RoomTime.getDefaultTimeSlots();
        JSONArray arr = new JSONArray();
        for (RoomTime rt : defaults) {
            JSONObject td = new JSONObject();
            td.put("price", rt.getPrice());
            td.put("timeSeconds", rt.getTimeSeconds());
            arr.put(td);
        }
        return arr;
    }

    /**
     * Builds a standard room identifier string from tower, floor, and room numbers.
     * @param towerNumber tower identifier
     * @param floorNumber zero-based floor number
     * @param roomNumber  zero-based room number
     * @return formatted string like "1-105"
     */
    public static String buildRoomString(int towerNumber, int floorNumber, int roomNumber) {
        return towerNumber + "-" + (floorNumber + 1) + "0" + (roomNumber + 1);
    }
}
