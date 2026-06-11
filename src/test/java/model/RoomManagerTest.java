package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import model.json.FloorConfig;
import model.json.ObjectMapperFactory;
import model.json.RoomConfigData;
import model.json.RoomStateData;
import model.json.TowerConfig;
import model.modelManagers.RoomManager;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void shouldBuildRoomGridFromProgramData() {
        List<TowerConfig> programData = createSingleTowerConfig(2, new int[]{4, 3});
        roomManager.buildRoomGrid(programData);
        int[][] roomsArray = roomManager.getRoomsArray();
        assertThat(roomsArray).hasDimensions(1, 2);
        assertThat(roomsArray[0][0]).isEqualTo(4);
        assertThat(roomsArray[0][1]).isEqualTo(3);
    }

    @Test
    void shouldBuildRoomGridWithMultipleTowers() {
        List<TowerConfig> programData = createTwoTowerConfig();
        roomManager.buildRoomGrid(programData);
        int[][] roomsArray = roomManager.getRoomsArray();
        assertThat(roomsArray).hasDimensions(2, 2);
        assertThat(roomsArray[0][1]).isEqualTo(3);
        assertThat(roomsArray[1][0]).isEqualTo(2);
        assertThat(roomsArray[1][1]).isEqualTo(2);
    }

    @Test
    void shouldCreateRoomsWithCorrectIdentity() {
        List<TowerConfig> programData = createSingleTowerConfig(1, new int[]{2});
        roomManager.buildRoomGrid(programData);
        Room room0 = roomManager.getRoom(0, 0, 0);
        assertThat(room0.getTowerNumber()).isEqualTo(0);
        assertThat(room0.getFloorNumber()).isEqualTo(0);
        assertThat(room0.getRoomNumber()).isEqualTo(0);
        Room room1 = roomManager.getRoom(0, 0, 1);
        assertThat(room1.getRoomString()).isEqualTo("1-102");
    }

    @Test
    void shouldReturnReceptionForOutOfBoundsTower() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        assertThat(roomManager.getRoom(-1, 0, 0).getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnReceptionForOutOfBoundsFloor() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        assertThat(roomManager.getRoom(0, 5, 0).getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnReceptionForOutOfBoundsRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{2}));
        assertThat(roomManager.getRoom(0, 0, 10).getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnReceptionForNegativeRoomIndex() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        assertThat(roomManager.getRoom(0, 0, -1).getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnValidRoomForInBoundsIndices() {
        roomManager.buildRoomGrid(createSingleTowerConfig(2, new int[]{3, 4}));
        Room result = roomManager.getRoom(0, 1, 2);
        assertThat(result).isNotNull();
        assertThat(result.getRoomString()).isEqualTo("1-203");
        assertThat(result.getStatus()).isEqualTo(RoomStatus.FREE);
    }

    @Test
    void shouldRestoreFreeRoomState() throws JsonProcessingException {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{2}));
        List<RoomStateData> rooms = new ArrayList<>();
        rooms.add(new RoomStateData("1-101", 0, 0, 0, RoomStatus.FREE.getCode(), 0L, "", "", 0L));
        String json = ObjectMapperFactory.get().writeValueAsString(new RoomManager.RoomStatesWrapper(rooms, 2));
        roomManager.restoreRoomStates(json);
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.FREE);
    }

    @Test
    void shouldRestoreOccupiedRoomState() throws JsonProcessingException {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        List<RoomStateData> rooms = new ArrayList<>();
        rooms.add(new RoomStateData("1-101", 0, 0, 0, RoomStatus.OCCUPIED.getCode(), 21600L,
                now.atZone(zoneID).toString(), "", 10800L));
        String json = ObjectMapperFactory.get().writeValueAsString(new RoomManager.RoomStatesWrapper(rooms, 2));
        roomManager.restoreRoomStates(json);
        Room restored = roomManager.getRoom(0, 0, 0);
        assertThat(restored.getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(restored.getServiceDuration()).isEqualTo(21600L);
        assertThat(restored.getExtensionDuration()).isEqualTo(10800L);
    }

    @Test
    void shouldRestoreCleaningRoomState() throws JsonProcessingException {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        List<RoomStateData> rooms = new ArrayList<>();
        rooms.add(new RoomStateData("1-101", 0, 0, 0, RoomStatus.CLEANING.getCode(), 0L,
                now.atZone(zoneID).toString(), "", 0L));
        String json = ObjectMapperFactory.get().writeValueAsString(new RoomManager.RoomStatesWrapper(rooms, 2));
        roomManager.restoreRoomStates(json);
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.CLEANING);
    }

    @Test
    void shouldHandleNullRoomDataInRestore() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.restoreRoomStates(null);
    }

    @Test
    void shouldHandleEmptyRoomDataInRestore() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.restoreRoomStates("");
    }

    @Test
    void shouldBookFreeRoomAndReturnZeroExtension() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        long extension = roomManager.registerRoomTimeAdded(0, 0, 0, 43200L, now);
        assertThat(extension).isZero();
        Room room = roomManager.getRoom(0, 0, 0);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(room.getServiceDuration()).isEqualTo(43200L);
    }

    @Test
    void shouldExtendOccupiedRoomAndReturnExtensionAmount() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        long extension = roomManager.registerRoomTimeAdded(0, 0, 0, 21600L, now);
        assertThat(extension).isEqualTo(21600L);
        Room room = roomManager.getRoom(0, 0, 0);
        assertThat(room.getExtensionDuration()).isEqualTo(21600L);
        assertThat(room.getServiceDuration()).isEqualTo(10800L);
    }

    @Test
    void shouldSetOccupiedRoomToCleaningOnEnd() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        roomManager.registerRoomTimeEnd(0, 0, 0, now);
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.CLEANING);
    }

    @Test
    void shouldSetCleaningRoomToFreeOnEnd() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        roomManager.registerRoomTimeEnd(0, 0, 0, now);
        roomManager.registerRoomTimeEnd(0, 0, 0, now);
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.FREE);
    }

    @Test
    void shouldSwapGuestToFreeRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{2}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 43200L, now);
        roomManager.setCurrentFloorRoom(0, 0, 0);
        roomManager.setDesiredRoomChange(0, 0, 1);
        boolean result = roomManager.changeRoomTimeToAnother(now);
        assertThat(result).isTrue();
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.CLEANING);
        assertThat(roomManager.getRoom(0, 0, 1).getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(roomManager.getRoom(0, 0, 1).getServiceDuration()).isEqualTo(43200L);
    }

    @Test
    void shouldRejectSwapToOccupiedRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{2}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        roomManager.registerRoomTimeAdded(0, 0, 1, 10800L, now);
        roomManager.setCurrentFloorRoom(0, 0, 0);
        roomManager.setDesiredRoomChange(0, 0, 1);
        boolean result = roomManager.changeRoomTimeToAnother(now);
        assertThat(result).isFalse();
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.OCCUPIED);
    }

    @Test
    void shouldTransferExtensionAndServiceOnSwap() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{2}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 21600L, now);
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        roomManager.setCurrentFloorRoom(0, 0, 0);
        roomManager.setDesiredRoomChange(0, 0, 1);
        roomManager.changeRoomTimeToAnother(now);
        Room target = roomManager.getRoom(0, 0, 1);
        assertThat(target.getServiceDuration()).isEqualTo(21600L);
        assertThat(target.getExtensionDuration()).isEqualTo(10800L);
    }

    @Test
    void shouldReturnZeroForRoomWithNullEndStatus() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        assertThat(roomManager.getRemainingTimeRoom(0, 0, 0, now)).isEqualTo("0:0");
    }

    @Test
    void shouldCalculateRemainingTimeForOccupiedRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        String remaining = roomManager.getRemainingTimeRoom(0, 0, 0, now);
        assertThat(remaining).startsWith("3:");
    }

    @Test
    void shouldShowNegativeRemainingForOverTimeRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 3600L, now);
        Instant futureTime = now.plus(Duration.ofHours(2));
        String remaining = roomManager.getRemainingTimeRoom(0, 0, 0, futureTime);
        assertThat(remaining).startsWith("-");
    }

    @Test
    void shouldReturnNAForNullStartStatus() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        assertThat(roomManager.getStartTimeRoom(0, 0, 0)).isEqualTo("N/A");
        assertThat(roomManager.getStartDateRoom(0, 0, 0)).isEqualTo("N/A");
    }

    @Test
    void shouldReturnFormattedStartTimeForOccupiedRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        assertThat(roomManager.getStartTimeRoom(0, 0, 0)).isNotEmpty();
        assertThat(roomManager.getStartTimeRoom(0, 0, 0)).isNotEqualTo("N/A");
    }

    @Test
    void shouldReturnFormattedStartDateForOccupiedRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        String startDate = roomManager.getStartDateRoom(0, 0, 0);
        assertThat(startDate).isNotEmpty();
        assertThat(startDate).isNotEqualTo("N/A");
        assertThat(startDate).contains("de");
    }

    @Test
    void shouldReturnReceptionWhenFloorIsNegative() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.setCurrentFloorRoom(0, -1, 0);
        assertThat(roomManager.getRoomForSale().getRoomString()).isEqualTo("Recepcion");
    }

    @Test
    void shouldReturnCurrentRoomForSale() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{2}));
        roomManager.setCurrentFloorRoom(0, 0, 1);
        assertThat(roomManager.getRoomForSale().getRoomString()).isEqualTo("1-102");
    }

    @Test
    void shouldReturnCurrentRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.setCurrentFloorRoom(0, 0, 0);
        assertThat(roomManager.getCurrentRoom()).isNotNull();
    }

    @Test
    void shouldReturnDesiredChangeRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{2}));
        roomManager.setDesiredRoomChange(0, 0, 1);
        assertThat(roomManager.getDesiredChangeRoom().getRoomString()).isEqualTo("1-102");
    }

    @Test
    void shouldTrackCurrentServiceDesired() {
        assertThat(roomManager.getCurrentServiceDesired()).isZero();
        roomManager.setCurrentServiceDesired(43200L);
        assertThat(roomManager.getCurrentServiceDesired()).isEqualTo(43200L);
        roomManager.setCurrentServiceDesired(10800L);
        assertThat(roomManager.getCurrentServiceDesired()).isEqualTo(10800L);
    }

    @Test
    void shouldSerializeAllRoomsToJson() throws JsonProcessingException {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{2}));
        String json = roomManager.getRoomDataForSaving();
        JsonNode node = ObjectMapperFactory.get().readTree(json);
        assertThat(node.get("rooms")).hasSize(2);
        JsonNode first = node.get("rooms").get(0);
        assertThat(first.has("roomString")).isTrue();
        assertThat(first.has("towerNumber")).isTrue();
        assertThat(first.has("floorNumber")).isTrue();
        assertThat(first.has("roomNumber")).isTrue();
        assertThat(first.has("status")).isTrue();
        assertThat(first.has("serviceDuration")).isTrue();
        assertThat(first.has("startStatus")).isTrue();
        assertThat(first.has("endStatus")).isTrue();
        assertThat(first.has("extensionDuration")).isTrue();
    }

    @Test
    void shouldSerializeOccupiedRoomWithTimeData() throws JsonProcessingException {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        String json = roomManager.getRoomDataForSaving();
        JsonNode node = ObjectMapperFactory.get().readTree(json);
        JsonNode roomJson = node.get("rooms").get(0);
        assertThat(roomJson.get("status").asInt()).isEqualTo(RoomStatus.OCCUPIED.getCode());
        assertThat(roomJson.get("serviceDuration").asLong()).isEqualTo(10800L);
        assertThat(roomJson.get("startStatus").asText()).isNotEmpty();
        assertThat(roomJson.get("endStatus").asText()).isNotEmpty();
    }

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

    @Test
    void shouldReturnEmptyArrayWhenNoGridBuilt() {
        assertThat(roomManager.getRoomsArray()).isEmpty();
    }

    @Test
    void rebuildRoomGridShouldPreserveRoomState() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{2}));
        roomManager.registerRoomTimeAdded(0, 0, 0, 10800L, now);
        List<TowerConfig> newConfig = createSingleTowerConfig(2, new int[]{1, 1});
        roomManager.rebuildRoomGrid(newConfig);
        int[][] roomsArray = roomManager.getRoomsArray();
        assertThat(roomsArray).hasDimensions(1, 2);
        assertThat(roomsArray[0][0]).isEqualTo(1);
        assertThat(roomsArray[0][1]).isEqualTo(1);
        assertThat(roomManager.getRoom(0, 0, 0).getStatus()).isEqualTo(RoomStatus.OCCUPIED);
    }

    @Test
    void shouldRenameRoom() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        roomManager.setRoomString(0, 0, 0, "T-999");
        assertThat(roomManager.getRoom(0, 0, 0).getRoomString()).isEqualTo("T-999");
    }

    @Test
    void getTotalTowersShouldReturnCorrectCount() {
        roomManager.buildRoomGrid(createTwoTowerConfig());
        assertThat(roomManager.getTotalTowers()).isEqualTo(2);
    }

    @Test
    void getTotalFloorsShouldReturnCorrectCount() {
        roomManager.buildRoomGrid(createTwoTowerConfig());
        assertThat(roomManager.getTotalFloors(0)).isEqualTo(2);
        assertThat(roomManager.getTotalFloors(1)).isEqualTo(2);
    }

    @Test
    void getTotalFloorsShouldReturnZeroForOutOfBoundsTower() {
        roomManager.buildRoomGrid(createSingleTowerConfig(1, new int[]{1}));
        assertThat(roomManager.getTotalFloors(5)).isZero();
    }

    // ========== Helpers ==========

    private static List<TowerConfig> createSingleTowerConfig(int numFloors, int[] roomsPerFloor) {
        List<FloorConfig> towerRooms = new ArrayList<>();
        for (int floor = 0; floor < numFloors; floor++) {
            List<RoomConfigData> rooms = new ArrayList<>();
            for (int room = 0; room < roomsPerFloor[floor]; room++) {
                String roomString = "1-" + (floor + 1) + "0" + (room + 1);
                rooms.add(new RoomConfigData(roomString, floor, room, null));
            }
            towerRooms.add(new FloorConfig(floor, rooms));
        }
        List<TowerConfig> result = new ArrayList<>();
        result.add(new TowerConfig(0, numFloors, towerRooms));
        return result;
    }

    private static List<TowerConfig> createTwoTowerConfig() {
        List<TowerConfig> result = new ArrayList<>();

        List<FloorConfig> tower1Rooms = new ArrayList<>();
        tower1Rooms.add(createFloorConfig(0, 4, 0));
        tower1Rooms.add(createFloorConfig(1, 3, 0));
        result.add(new TowerConfig(0, 2, tower1Rooms));

        List<FloorConfig> tower2Rooms = new ArrayList<>();
        tower2Rooms.add(createFloorConfig(0, 2, 1));
        tower2Rooms.add(createFloorConfig(1, 2, 1));
        result.add(new TowerConfig(1, 2, tower2Rooms));

        return result;
    }

    private static FloorConfig createFloorConfig(int floorNumber, int roomCount, int towerNumber) {
        List<RoomConfigData> rooms = new ArrayList<>();
        for (int room = 0; room < roomCount; room++) {
            String roomString = (towerNumber + 1) + "-" + (floorNumber + 1) + "0" + (room + 1);
            rooms.add(new RoomConfigData(roomString, floorNumber, room, null));
        }
        return new FloorConfig(floorNumber, rooms);
    }
}
