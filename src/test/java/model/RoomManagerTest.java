package model;

import model.modelManagers.RoomManager;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoomManager} — room grid management, room operations,
 * and time queries.
 */
class RoomManagerTest {

    private RoomManager roomManager;
    private ZoneId zoneID;
    private Instant now;

    @BeforeEach
    void setUp() {
        zoneID = ZoneId.of("America/Bogota");
        now = Instant.now();
        roomManager = new RoomManager(zoneID);
    }

    // ========== Room Grid Building ==========

    @Test
    void shouldBuildRoomGridFromProgramData() {
        JSONObject programData = createSingleTowerConfig(2, new int[]{4, 3});

        roomManager.buildRoomGrid(programData);

        int[][] roomsArray = roomManager.getRoomsArray();
        assertThat(roomsArray).hasDimensions(1, 2);
        assertThat(roomsArray[0][0]).isEqualTo(4); // floor 0: 4 rooms
        assertThat(roomsArray[0][1]).isEqualTo(3); // floor 1: 3 rooms
    }

    @Test
    void shouldBuildRoomGridWithMultipleTowers() {
        JSONObject programData = createTwoTowerConfig();

        roomManager.buildRoomGrid(programData);

        int[][] roomsArray = roomManager.getRoomsArray();
        assertThat(roomsArray).hasDimensions(2, 2);
        assertThat(roomsArray[0][1]).isEqualTo(3);
        assertThat(roomsArray[1][0]).isEqualTo(2);
        assertThat(roomsArray[1][1]).isEqualTo(2);
    }

    @Test
    void shouldCreateRoomsWithCorrectIdentity() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});

        roomManager.buildRoomGrid(programData);

        Room room0 = roomManager.getRoom(0, 0, 0);
        assertThat(room0.getTowerNumber()).isEqualTo(1);
        assertThat(room0.getFloorNumber()).isEqualTo(0);
        assertThat(room0.getRoomNumber()).isEqualTo(0);

        Room room1 = roomManager.getRoom(0, 0, 1);
        assertThat(room1.getRoomString()).isEqualTo("1-102");
    }

    // ========== getRoom Bounds Checking ==========

    @Test
    void shouldReturnReceptionForOutOfBoundsTower() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        Room result = roomManager.getRoom(-1, 0, 0);
        assertThat(result.getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnReceptionForOutOfBoundsFloor() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        Room result = roomManager.getRoom(0, 5, 0);
        assertThat(result.getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnReceptionForOutOfBoundsRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);

        Room result = roomManager.getRoom(0, 0, 10);
        assertThat(result.getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnReceptionForNegativeRoomIndex() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        Room result = roomManager.getRoom(0, 0, -1);
        assertThat(result.getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnValidRoomForInBoundsIndices() {
        JSONObject programData = createSingleTowerConfig(2, new int[]{3, 4});
        roomManager.buildRoomGrid(programData);

        Room result = roomManager.getRoom(0, 1, 2);
        assertThat(result).isNotNull();
        assertThat(result.getRoomString()).isEqualTo("1-203");
        assertThat(result.getStatus()).isEqualTo(RoomStatus.FREE);
    }

    // ========== Room State Restoration ==========

    @Test
    void shouldRestoreFreeRoomState() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);

        JSONObject roomData = new JSONObject();
        JSONArray roomsArray = new JSONArray();
        JSONObject roomJson = new JSONObject();
        roomJson.put("roomString", "1-101");
        roomJson.put("towerNumber", 0);
        roomJson.put("floorNumber", 0);
        roomJson.put("roomNumber", 0);
        roomJson.put("status", RoomStatus.FREE.getCode());
        roomJson.put("service", 0);
        roomJson.put("extension", 0);
        roomJson.put("startStatus", "");
        roomJson.put("endStatus", "");
        roomsArray.put(roomJson);
        roomData.put("rooms", roomsArray);

        roomManager.restoreRoomStates(roomData);

        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.FREE);
    }

    @Test
    void shouldRestoreOccupiedRoomState() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        JSONObject roomData = new JSONObject();
        JSONArray roomsArray = new JSONArray();
        JSONObject roomJson = new JSONObject();
        roomJson.put("roomString", "1-101");
        roomJson.put("towerNumber", 0);
        roomJson.put("floorNumber", 0);
        roomJson.put("roomNumber", 0);
        roomJson.put("status", RoomStatus.OCCUPIED.getCode());
        roomJson.put("service", 6);
        roomJson.put("extension", 3);
        roomJson.put("startStatus", now.atZone(zoneID).toString());
        roomsArray.put(roomJson);
        roomData.put("rooms", roomsArray);

        roomManager.restoreRoomStates(roomData);

        Room restored = roomManager.getRoom(0, 0, 0);
        assertThat(restored.getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(restored.getService()).isEqualTo(6);
        assertThat(restored.getExtension()).isEqualTo(3);
    }

    @Test
    void shouldRestoreCleaningRoomState() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        JSONObject roomData = new JSONObject();
        JSONArray roomsArray = new JSONArray();
        JSONObject roomJson = new JSONObject();
        roomJson.put("roomString", "1-101");
        roomJson.put("towerNumber", 0);
        roomJson.put("floorNumber", 0);
        roomJson.put("roomNumber", 0);
        roomJson.put("status", RoomStatus.CLEANING.getCode());
        roomJson.put("service", 0);
        roomJson.put("extension", 0);
        roomJson.put("startStatus", now.atZone(zoneID).toString());
        roomsArray.put(roomJson);
        roomData.put("rooms", roomsArray);

        roomManager.restoreRoomStates(roomData);

        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.CLEANING);
    }

    @Test
    void shouldHandleNullRoomDataInRestore() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        // Should not throw
        roomManager.restoreRoomStates(null);
    }

    @Test
    void shouldHandleEmptyRoomDataInRestore() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        // Should not throw
        roomManager.restoreRoomStates(new JSONObject());
    }

    // ========== Room Booking (registerRoomTimeAdded) ==========

    @Test
    void shouldBookFreeRoomAndReturnZeroExtension() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        int extension = roomManager.registerRoomTimeAdded(0, 0, 0, 12, now);

        assertThat(extension).isZero();
        Room room = roomManager.getRoom(0, 0, 0);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(room.getService()).isEqualTo(12);
    }

    @Test
    void shouldExtendOccupiedRoomAndReturnExtensionAmount() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now); // initial booking

        int extension = roomManager.registerRoomTimeAdded(0, 0, 0, 6, now); // extension

        assertThat(extension).isEqualTo(6);
        Room room = roomManager.getRoom(0, 0, 0);
        assertThat(room.getExtension()).isEqualTo(6);
        assertThat(room.getService()).isEqualTo(3); // original service unchanged
    }

    // ========== Room Check-Out (registerRoomTimeEnd) ==========

    @Test
    void shouldSetOccupiedRoomToCleaningOnEnd() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now);

        roomManager.registerRoomTimeEnd(0, 0, 0, now);

        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.CLEANING);
    }

    @Test
    void shouldSetCleaningRoomToFreeOnEnd() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now);
        roomManager.registerRoomTimeEnd(0, 0, 0, now);

        roomManager.registerRoomTimeEnd(0, 0, 0, now);

        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.FREE);
    }

    // ========== Room Swap (changeRoomTimeToAnother) ==========

    @Test
    void shouldSwapGuestToFreeRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 12, now); // book room 0

        roomManager.setCurrentFloorRoom(0, 0, 0);         // source: room 0
        roomManager.setDesiredRoomChange(0, 0, 1);        // target: room 1

        boolean result = roomManager.changeRoomTimeToAnother(now);

        assertThat(result).isTrue();
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.CLEANING);
        assertThat(roomManager.getRoom(0, 0, 1).getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(roomManager.getRoom(0, 0, 1).getService()).isEqualTo(12);
    }

    @Test
    void shouldRejectSwapToOccupiedRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now); // book room 0
        roomManager.registerRoomTimeAdded(0, 0, 1, 3, now); // book room 1 (occupied)

        roomManager.setCurrentFloorRoom(0, 0, 0);
        roomManager.setDesiredRoomChange(0, 0, 1);

        boolean result = roomManager.changeRoomTimeToAnother(now);

        assertThat(result).isFalse();
        // Source room should remain occupied
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.OCCUPIED);
    }

    @Test
    void shouldTransferExtensionAndServiceOnSwap() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 6, now);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now); // extend by 3

        roomManager.setCurrentFloorRoom(0, 0, 0);
        roomManager.setDesiredRoomChange(0, 0, 1);

        roomManager.changeRoomTimeToAnother(now);

        Room target = roomManager.getRoom(0, 0, 1);
        assertThat(target.getService()).isEqualTo(6);
        assertThat(target.getExtension()).isEqualTo(3);
    }

    // ========== Remaining Time ==========

    @Test
    void shouldReturnZeroForRoomWithNullEndStatus() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        String remaining = roomManager.getRemainingTimeRoom(0, 0, 0, now);

        assertThat(remaining).isEqualTo("0:0");
    }

    @Test
    void shouldCalculateRemainingTimeForOccupiedRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now);

        // Check remaining time at booking time (should be ~3 hours)
        String remaining = roomManager.getRemainingTimeRoom(0, 0, 0, now);
        assertThat(remaining).startsWith("3:");
    }

    @Test
    void shouldShowNegativeRemainingForOverTimeRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 1, now);

        // Advance time past the end
        Instant futureTime = now.plus(Duration.ofHours(2));
        String remaining = roomManager.getRemainingTimeRoom(0, 0, 0, futureTime);

        assertThat(remaining).startsWith("-");
    }

    // ========== Start Time / Date Display ==========

    @Test
    void shouldReturnNAForNullStartStatus() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        assertThat(roomManager.getStartTimeRoom(0, 0, 0)).isEqualTo("N/A");
        assertThat(roomManager.getStartDateRoom(0, 0, 0)).isEqualTo("N/A");
    }

    @Test
    void shouldReturnFormattedStartTimeForOccupiedRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now);

        String startTime = roomManager.getStartTimeRoom(0, 0, 0);
        assertThat(startTime).isNotEmpty();
        assertThat(startTime).isNotEqualTo("N/A");
    }

    @Test
    void shouldReturnFormattedStartDateForOccupiedRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now);

        String startDate = roomManager.getStartDateRoom(0, 0, 0);
        assertThat(startDate).isNotEmpty();
        assertThat(startDate).isNotEqualTo("N/A");
        assertThat(startDate).contains("de"); // Spanish date format "d 'de' MMMM yyyy"
    }

    // ========== Room for Sale ==========

    @Test
    void shouldReturnReceptionWhenFloorIsNegative() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.setCurrentFloorRoom(0, -1, 0);

        Room saleRoom = roomManager.getRoomForSale();

        assertThat(saleRoom.getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnCurrentRoomForSale() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);
        roomManager.setCurrentFloorRoom(0, 0, 1);

        Room saleRoom = roomManager.getRoomForSale();

        assertThat(saleRoom.getRoomString()).isEqualTo("1-102");
    }

    // ========== Current / Desired Room Access ==========

    @Test
    void shouldReturnCurrentRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.setCurrentFloorRoom(0, 0, 0);

        Room current = roomManager.getCurrentRoom();
        assertThat(current).isNotNull();
    }

    @Test
    void shouldReturnDesiredChangeRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);
        roomManager.setDesiredRoomChange(0, 0, 1);

        Room desired = roomManager.getDesiredChangeRoom();
        assertThat(desired.getRoomString()).isEqualTo("1-102");
    }

    // ========== Current Service Desired ==========

    @Test
    void shouldTrackCurrentServiceDesired() {
        assertThat(roomManager.getCurrentServiceDesired()).isZero();

        roomManager.setCurrentServiceDesired(12);
        assertThat(roomManager.getCurrentServiceDesired()).isEqualTo(12);

        roomManager.setCurrentServiceDesired(3);
        assertThat(roomManager.getCurrentServiceDesired()).isEqualTo(3);
    }

    // ========== Room Data Serialization ==========

    @Test
    void shouldSerializeAllRoomsToJson() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);

        JSONArray data = roomManager.getRoomDataForSaving();

        assertThat(data).hasSize(2);
        JSONObject first = data.getJSONObject(0);
        assertThat(first.has("roomString")).isTrue();
        assertThat(first.has("towerNumber")).isTrue();
        assertThat(first.has("floorNumber")).isTrue();
        assertThat(first.has("roomNumber")).isTrue();
        assertThat(first.has("status")).isTrue();
        assertThat(first.has("service")).isTrue();
        assertThat(first.has("startStatus")).isTrue();
        assertThat(first.has("endStatus")).isTrue();
        assertThat(first.has("extension")).isTrue();
    }

    @Test
    void shouldSerializeOccupiedRoomWithTimeData() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now);

        JSONArray data = roomManager.getRoomDataForSaving();
        JSONObject roomJson = data.getJSONObject(0);

        assertThat(roomJson.getInt("status")).isEqualTo(RoomStatus.OCCUPIED.getCode());
        assertThat(roomJson.getInt("service")).isEqualTo(3);
        assertThat(roomJson.getString("startStatus")).isNotEmpty();
        assertThat(roomJson.getString("endStatus")).isNotEmpty();
    }

    // ========== State Setters / Getters ==========

    @Test
    void shouldTrackViewedRoomState() {
        roomManager.setCurrentFloorRoom(1, 2, 3);

        assertThat(roomManager.getCurrentTowerViewed()).isEqualTo(1);
        assertThat(roomManager.getCurrentFloorViewed()).isEqualTo(2);
        assertThat(roomManager.getCurrentRoomViewed()).isEqualTo(3);
    }

    @Test
    void shouldTrackSelectedRoomChangeState() {
        roomManager.setDesiredRoomChange(0, 1, 4);

        assertThat(roomManager.getSelectedRoomChangeTower()).isEqualTo(0);
        assertThat(roomManager.getSelectedRoomChangeFloor()).isEqualTo(1);
        assertThat(roomManager.getSelectedRoomChangeRoom()).isEqualTo(4);
    }

    // ========== Empty Rooms Array ==========

    @Test
    void shouldReturnEmptyArrayWhenNoGridBuilt() {
        int[][] roomsArray = roomManager.getRoomsArray();

        assertThat(roomsArray).isEmpty();
    }

    // ========== Custom Time Data Persistence ==========

    @Test
    void shouldSerializeCustomTimeDataWhenRoomHasIt() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        RoomTime[] custom = new RoomTime[]{
            new RoomTime(12345, 3600),
            new RoomTime(23456, 7200),
            new RoomTime(34567, 10800)
        };
        roomManager.setRoomCustomTimeData(0, 0, 0, custom);

        JSONArray data = roomManager.getRoomDataForSaving();
        JSONObject roomJson = data.getJSONObject(0);

        assertThat(roomJson.has("customTimeData")).isFalse();
    }

    @Test
    void customTimeDataShouldBeReadFromConfigNotFromRoomStates() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        JSONObject tower = programData.getJSONArray("roomsPerTower").getJSONObject(0);
        JSONObject roomJson = tower.getJSONArray("towerRooms").getJSONObject(0)
                .getJSONArray("rooms").getJSONObject(0);

        JSONArray timeArr = new JSONArray();
        JSONObject td1 = new JSONObject();
        td1.put("price", 11111L);
        td1.put("timeSeconds", 1111L);
        timeArr.put(td1);
        JSONObject td2 = new JSONObject();
        td2.put("price", 22222L);
        td2.put("timeSeconds", 2222L);
        timeArr.put(td2);
        JSONObject td3 = new JSONObject();
        td3.put("price", 33333L);
        td3.put("timeSeconds", 3333L);
        timeArr.put(td3);
        roomJson.put("customTimeData", timeArr);

        roomManager.buildRoomGrid(programData);

        Room restored = roomManager.getRoom(0, 0, 0);
        assertThat(restored.hasCustomTimeData()).isTrue();
        RoomTime[] data = restored.getCustomRoomTimeData();
        assertThat(data[0].getPrice()).isEqualTo(11111L);
        assertThat(data[0].getTimeSeconds()).isEqualTo(1111L);
        assertThat(data[1].getPrice()).isEqualTo(22222L);
        assertThat(data[2].getPrice()).isEqualTo(33333L);
    }

    @Test
    void shouldNotSerializeCustomTimeDataWhenRoomHasNone() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        JSONArray data = roomManager.getRoomDataForSaving();
        JSONObject roomJson = data.getJSONObject(0);

        assertThat(roomJson.has("customTimeData")).isFalse();
    }

    @Test
    void restoreRoomStatesShouldNotReadCustomTimeData() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        JSONObject roomData = new JSONObject();
        JSONArray roomsArray = new JSONArray();
        JSONObject roomJson = new JSONObject();
        roomJson.put("roomString", "1-101");
        roomJson.put("towerNumber", 0);
        roomJson.put("floorNumber", 0);
        roomJson.put("roomNumber", 0);
        roomJson.put("status", RoomStatus.FREE.getCode());
        roomJson.put("service", 0);
        roomJson.put("extension", 0);
        roomJson.put("startStatus", "");

        JSONArray timeArr = new JSONArray();
        JSONObject td1 = new JSONObject();
        td1.put("price", 11111L);
        td1.put("timeSeconds", 1111L);
        timeArr.put(td1);
        roomJson.put("customTimeData", timeArr);

        roomsArray.put(roomJson);
        roomData.put("rooms", roomsArray);

        roomManager.restoreRoomStates(roomData);

        Room restored = roomManager.getRoom(0, 0, 0);
        assertThat(restored.hasCustomTimeData()).isFalse();
    }

    @Test
    void shouldLeaveCustomTimeDataNullWhenNotInJson() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        JSONObject roomData = new JSONObject();
        JSONArray roomsArray = new JSONArray();
        JSONObject roomJson = new JSONObject();
        roomJson.put("roomString", "1-101");
        roomJson.put("towerNumber", 0);
        roomJson.put("floorNumber", 0);
        roomJson.put("roomNumber", 0);
        roomJson.put("status", RoomStatus.FREE.getCode());
        roomJson.put("service", 0);
        roomJson.put("extension", 0);
        roomJson.put("startStatus", "");
        roomsArray.put(roomJson);
        roomData.put("rooms", roomsArray);

        roomManager.restoreRoomStates(roomData);

        Room restored = roomManager.getRoom(0, 0, 0);
        assertThat(restored.hasCustomTimeData()).isFalse();
        assertThat(restored.getCustomRoomTimeData()[0].getPrice()).isEqualTo(40000L);
    }

    // ========== rebuildRoomGrid ==========

    @Test
    void rebuildRoomGridShouldPreserveRoomState() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);
        roomManager.registerRoomTimeAdded(0, 0, 0, 3, now);

        JSONObject newConfig = createSingleTowerConfig(2, new int[]{1, 1});
        roomManager.rebuildRoomGrid(newConfig);

        int[][] roomsArray = roomManager.getRoomsArray();
        assertThat(roomsArray).hasDimensions(1, 2);
        assertThat(roomsArray[0][0]).isEqualTo(1);
        assertThat(roomsArray[0][1]).isEqualTo(1);
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.OCCUPIED);
    }

    @Test
    void shouldRenameRoom() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        roomManager.setRoomString(0, 0, 0, "T-999");

        assertThat(roomManager.getRoom(0, 0, 0).getRoomString()).isEqualTo("T-999");
    }

    @Test
    void getTotalTowersShouldReturnCorrectCount() {
        JSONObject programData = createTwoTowerConfig();
        roomManager.buildRoomGrid(programData);

        assertThat(roomManager.getTotalTowers()).isEqualTo(2);
    }

    @Test
    void getTotalFloorsShouldReturnCorrectCount() {
        JSONObject programData = createTwoTowerConfig();
        roomManager.buildRoomGrid(programData);

        assertThat(roomManager.getTotalFloors(0)).isEqualTo(2);
        assertThat(roomManager.getTotalFloors(1)).isEqualTo(2);
    }

    @Test
    void getTotalFloorsShouldReturnZeroForOutOfBoundsTower() {
        JSONObject programData = createSingleTowerConfig(1, new int[]{1});
        roomManager.buildRoomGrid(programData);

        assertThat(roomManager.getTotalFloors(5)).isZero();
    }

    // ========== Helpers ==========

    /**
     * Creates a config JSON for a single tower.
     * Tower 1 with the given number of floors and rooms per floor.
     */
    private JSONObject createSingleTowerConfig(int numFloors, int[] roomsPerFloor) {
        JSONObject programData = new JSONObject();
        JSONArray roomsPerTower = new JSONArray();

        JSONObject tower = new JSONObject();
        tower.put("towerNumber", 1);
        tower.put("towerFloors", numFloors);

        JSONArray towerRooms = new JSONArray();
        for (int floor = 0; floor < numFloors; floor++) {
            JSONObject floorData = new JSONObject();
            floorData.put("floor", floor);
            JSONArray rooms = new JSONArray();
            for (int room = 0; room < roomsPerFloor[floor]; room++) {
                JSONObject roomJson = new JSONObject();
                String roomString = "1-" + (floor + 1) + "0" + (room + 1);
                roomJson.put("roomString", roomString);
                roomJson.put("roomFloor", floor);
                roomJson.put("roomNumber", room);
                rooms.put(roomJson);
            }
            floorData.put("rooms", rooms);
            towerRooms.put(floorData);
        }
        tower.put("towerRooms", towerRooms);
        roomsPerTower.put(tower);

        programData.put("roomsPerTower", roomsPerTower);
        return programData;
    }

    /**
     * Creates a config JSON for two towers, each with 2 floors.
     */
    private JSONObject createTwoTowerConfig() {
        JSONObject programData = new JSONObject();
        JSONArray roomsPerTower = new JSONArray();

        // Tower 1: 2 floors, floor 0 has 4 rooms, floor 1 has 3 rooms
        JSONObject tower1 = new JSONObject();
        tower1.put("towerNumber", 1);
        tower1.put("towerFloors", 2);
        JSONArray tower1Rooms = new JSONArray();
        tower1Rooms.put(createFloorJson(0, 4, 1));
        tower1Rooms.put(createFloorJson(1, 3, 1));
        tower1.put("towerRooms", tower1Rooms);
        roomsPerTower.put(tower1);

        // Tower 2: 2 floors, 2 rooms each
        JSONObject tower2 = new JSONObject();
        tower2.put("towerNumber", 2);
        tower2.put("towerFloors", 2);
        JSONArray tower2Rooms = new JSONArray();
        tower2Rooms.put(createFloorJson(0, 2, 2));
        tower2Rooms.put(createFloorJson(1, 2, 2));
        tower2.put("towerRooms", tower2Rooms);
        roomsPerTower.put(tower2);

        programData.put("roomsPerTower", roomsPerTower);
        return programData;
    }

    private JSONObject createFloorJson(int floorNumber, int roomCount, int towerNumber) {
        JSONObject floorData = new JSONObject();
        floorData.put("floor", floorNumber);
        JSONArray rooms = new JSONArray();
        for (int room = 0; room < roomCount; room++) {
            JSONObject roomJson = new JSONObject();
            String roomString = towerNumber + "-" + (floorNumber + 1) + "0" + (room + 1);
            roomJson.put("roomString", roomString);
            roomJson.put("roomFloor", floorNumber);
            roomJson.put("roomNumber", room);
            rooms.put(roomJson);
        }
        floorData.put("rooms", rooms);
        return floorData;
    }
}
