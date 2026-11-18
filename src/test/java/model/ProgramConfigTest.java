package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import model.json.FloorConfig;
import model.json.ObjectMapperFactory;
import model.json.RoomConfigData;
import model.json.TimeSlotConfig;
import model.json.TowerConfig;
import model.modelManagers.RoomManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramConfigTest {

    private ProgramConfig config;

    @BeforeEach
    void setUp() {
        config = new ProgramConfig();
    }

    @Test
    void shouldStartWithZeroTransactions() {
        assertThat(config.getConsecutiveTransaction()).isZero();
    }

    @Test
    void shouldStartWithZeroTransactionsInJson() throws JsonProcessingException {
        assertThat(config.toJson()).isNotNull();
        JsonNode node = ObjectMapperFactory.get().readTree(config.toJson());
        assertThat(node.get("consecutiveTransaction").asInt()).isZero();
    }

    @Test
    void shouldLoadAllFieldsFromJson() throws JsonProcessingException {
        String rawJson = "{\"consecutiveTransaction\":42,\"motelName\":\"Motel Paraiso\","
                + "\"motelAddress\":\"Calle 123 #45-67\",\"motelID\":\"NIT 900.123.456-7\","
                + "\"printerName\":\"XP-80C\",\"roomsPerTower\":[]}";

        config.loadFromJson(rawJson);

        assertThat(config.getConsecutiveTransaction()).isEqualTo(42);
        assertThat(config.getMotelName()).isEqualTo("Motel Paraiso");
        assertThat(config.getMotelAddress()).isEqualTo("Calle 123 #45-67");
        assertThat(config.getMotelID()).isEqualTo("NIT 900.123.456-7");
        assertThat(config.getConfiguredPrinterName()).isEqualTo("XP-80C");
    }

    @Test
    void shouldSetPrinterNameToNullWhenNotInJson() {
        String rawJson = "{\"consecutiveTransaction\":0,\"motelName\":\"Test\","
                + "\"motelAddress\":\"Addr\",\"motelID\":\"NIT\"}";

        config.loadFromJson(rawJson);

        assertThat(config.getConfiguredPrinterName()).isNull();
    }

    @Test
    void shouldIncrementTransactionCounter() {
        config.loadFromJson(createBaseConfigJson());
        config.addConsecutiveTransaction();
        assertThat(config.getConsecutiveTransaction()).isEqualTo(1);
    }

    @Test
    void shouldIncrementMultipleTimes() {
        config.loadFromJson(createBaseConfigJson());
        config.addConsecutiveTransaction();
        config.addConsecutiveTransaction();
        config.addConsecutiveTransaction();
        assertThat(config.getConsecutiveTransaction()).isEqualTo(3);
    }

    @Test
    void shouldPersistCounterInJson() throws JsonProcessingException {
        config.loadFromJson(createBaseConfigJson());
        config.addConsecutiveTransaction();
        JsonNode node = ObjectMapperFactory.get().readTree(config.toJson());
        assertThat(node.get("consecutiveTransaction").asInt()).isEqualTo(1);
    }

    @Test
    void shouldContinueFromLoadedCounter() throws JsonProcessingException {
        String json = "{\"consecutiveTransaction\":100,\"motelName\":\"Test Motel\","
                + "\"motelAddress\":\"Test Address\",\"motelID\":\"NIT 000.000.000-0\"}";
        config.loadFromJson(json);
        config.addConsecutiveTransaction();
        assertThat(config.getConsecutiveTransaction()).isEqualTo(101);
        JsonNode node = ObjectMapperFactory.get().readTree(config.toJson());
        assertThat(node.get("consecutiveTransaction").asInt()).isEqualTo(101);
    }

    @Test
    void shouldSavePrinterName() throws JsonProcessingException {
        config.loadFromJson(createBaseConfigJson());
        config.savePrinterConfiguration("ThermalPrinter-2000");
        assertThat(config.getConfiguredPrinterName()).isEqualTo("ThermalPrinter-2000");
        JsonNode node = ObjectMapperFactory.get().readTree(config.toJson());
        assertThat(node.get("printerName").asText()).isEqualTo("ThermalPrinter-2000");
    }

    @Test
    void shouldUpdateExistingPrinterName() {
        String json = "{\"consecutiveTransaction\":0,\"motelName\":\"Test Motel\","
                + "\"motelAddress\":\"Test Address\",\"motelID\":\"NIT 000.000.000-0\","
                + "\"printerName\":\"OldPrinter\"}";
        config.loadFromJson(json);
        config.savePrinterConfiguration("NewPrinter");
        assertThat(config.getConfiguredPrinterName()).isEqualTo("NewPrinter");
    }

    @Test
    void shouldReturnMotelProperties() {
        String json = "{\"consecutiveTransaction\":0,\"motelName\":\"Motel El Descanso\","
                + "\"motelAddress\":\"Av. Siempre Viva 742\",\"motelID\":\"NIT 800.456.789-1\"}";
        config.loadFromJson(json);
        assertThat(config.getMotelName()).isEqualTo("Motel El Descanso");
        assertThat(config.getMotelAddress()).isEqualTo("Av. Siempre Viva 742");
        assertThat(config.getMotelID()).isEqualTo("NIT 800.456.789-1");
    }

    @Test
    void shouldDetectLegacyFormatWhenVersionIsMissing() {
        config.loadFromJson(createBaseConfigJson());
        assertThat(config.getSchemaVersion()).isEqualTo(3);
        assertThat(config.isLegacyFormat()).isFalse();
    }

    @Test
    void shouldRecognizeVersionOne() {
        String json = "{\"consecutiveTransaction\":0,\"motelName\":\"Test Motel\","
                + "\"motelAddress\":\"Test Address\",\"motelID\":\"NIT 000.000.000-0\",\"version\":1}";
        config.loadFromJson(json);
        assertThat(config.getSchemaVersion()).isEqualTo(3);
        assertThat(config.isLegacyFormat()).isFalse();
    }

    @Test
    void ensureSchemaVersionShouldSetVersion() throws JsonProcessingException {
        config.loadFromJson(createBaseConfigJson());
        config.ensureSchemaVersion();
        assertThat(config.getSchemaVersion()).isEqualTo(3);
    }

    @Test
    void buildRoomStringShouldFormatCorrectly() {
        assertThat(ProgramConfig.buildRoomString(0, 0, 4)).isEqualTo("1-105");
    }

    @Test
    void buildRoomStringShouldHandleDifferentNumbers() {
        assertThat(ProgramConfig.buildRoomString(1, 1, 7)).isEqualTo("2-208");
    }

    // ========== Room Grid CRUD ==========

    @Test
    void addTowerShouldAppendWithTheNextNumber() {
        config.loadFromJson(createBaseConfigJson());
        config.addTower(List.of(new FloorConfig(0, new ArrayList<>())));

        assertThat(config.getTowerCount()).isEqualTo(1);
        assertThat(config.getTower(0).towerNumber()).isZero();
        assertThat(config.getTower(0).towerFloors()).isEqualTo(1);
    }

    @Test
    void removeTowerShouldReduceCount() {
        config.loadFromJson(createBaseConfigJson());
        config.addTower(List.of(new FloorConfig(0, new ArrayList<>())));
        config.addTower(List.of(new FloorConfig(0, new ArrayList<>())));

        config.removeTower(0);

        assertThat(config.getTowerCount()).isEqualTo(1);
        assertThat(config.getTower(0).towerNumber()).isZero();
    }

    @Test
    void addRoomToFloorShouldAppendRoom() {
        config.loadFromJson(createBaseConfigJson());
        List<FloorConfig> towerRooms = new ArrayList<>();
        towerRooms.add(new FloorConfig(0, new ArrayList<>()));
        config.addTower(towerRooms);

        config.addRoomToFloor(0, 0, "1-102", 0, 1);

        List<RoomConfigData> rooms = config.getTower(0).towerRooms().get(0).rooms();
        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).roomString()).isEqualTo("1-102");
    }

    @Test
    void removeRoomFromFloorShouldReduceCount() {
        config.loadFromJson(createBaseConfigJson());
        List<RoomConfigData> rooms = new ArrayList<>();
        rooms.add(new RoomConfigData("1-101", 0, 0, new ArrayList<>()));
        List<FloorConfig> towerRooms = new ArrayList<>();
        towerRooms.add(new FloorConfig(0, rooms));
        config.addTower(towerRooms);

        config.removeRoomFromFloor(0, 0, 0);

        List<RoomConfigData> updated = config.getTower(0).towerRooms().get(0).rooms();
        assertThat(updated).isEmpty();
    }

    @Test
    void setRoomStringShouldRenameRoom() {
        config.loadFromJson(createBaseConfigJson());
        List<RoomConfigData> rooms = new ArrayList<>();
        rooms.add(new RoomConfigData("1-101", 0, 0, new ArrayList<>()));
        List<FloorConfig> towerRooms = new ArrayList<>();
        towerRooms.add(new FloorConfig(0, rooms));
        config.addTower(towerRooms);

        config.setRoomString(0, 0, 0, "VIP-1");

        String name = config.getTower(0).towerRooms().get(0).rooms().get(0).roomString();
        assertThat(name).isEqualTo("VIP-1");
    }

    // ========== Per-tower pricing ==========

    /**
     * Verifies that a room added to a tower is created with the time and price of the
     * tower default instead of the built-in values.
     * Expected: The stored custom time data of the new room is the tower default.
     * Failure: New rooms keep using the built-in 3 h / 12 h / 24 h pricing.
     */
    @Test
    void towerDefaultShouldBeUsedByNewRooms() {
        config.loadFromJson(createBaseConfigJson());
        addTowerWithOneEmptyFloor();
        List<TimeSlotConfig> towerDefault = List.of(new TimeSlotConfig(99000L, 7200L));
        config.setTowerDefaultTimeData(0, towerDefault);

        config.addRoomToFloor(0, 0, "1-101", 0, 0);

        List<TimeSlotConfig> stored = config.getTower(0).towerRooms().get(0).rooms().get(0).customTimeData();
        assertThat(stored).hasSize(1);
        assertThat(stored.get(0).price()).isEqualTo(99000L);
        assertThat(stored.get(0).timeSeconds()).isEqualTo(7200L);
    }

    /**
     * Verifies that the tower default is not lost when rooms and floors of that tower are
     * added or renamed, since the tower record is rebuilt on each of those operations.
     * Expected: The tower default is still the stored one after the changes.
     * Failure: Rebuilding the tower drops its default and new rooms fall back to the built-in values.
     */
    @Test
    void towerDefaultShouldSurviveRoomAndFloorChanges() {
        config.loadFromJson(createBaseConfigJson());
        addTowerWithOneEmptyFloor();
        config.setTowerDefaultTimeData(0, List.of(new TimeSlotConfig(99000L, 7200L)));

        config.addRoomToFloor(0, 0, "1-101", 0, 0);
        config.setRoomString(0, 0, 0, "VIP-1");
        config.addFloorToTower(0, 1, 1);
        config.removeFloorFromTower(0, 1);

        assertThat(config.getTower(0).defaultTimeData()).hasSize(1);
        assertThat(config.towerDefaultTimeData(0).get(0).price()).isEqualTo(99000L);
    }

    /**
     * Verifies that a tower without a stored default prices its rooms with the built-in
     * 3 h / 12 h / 24 h values, which is the state of every tower saved before 0.1.5.2.
     * Expected: 3 slots, the first one 40.000 for 3 h.
     * Failure: A tower without a default returns no time data at all.
     */
    @Test
    void towerDefaultShouldFallBackToTheBuiltInValues() {
        config.loadFromJson(createBaseConfigJson());
        addTowerWithOneEmptyFloor();

        List<TimeSlotConfig> towerDefault = config.towerDefaultTimeData(0);

        assertThat(towerDefault).hasSize(3);
        assertThat(towerDefault.get(0).price()).isEqualTo(40000L);
        assertThat(towerDefault.get(0).timeSeconds()).isEqualTo(10800L);
    }

    /**
     * Verifies that the tower default is written to the JSON and read back.
     * Expected: The reloaded configuration returns the same default.
     * Failure: The field is not serialized, so the default is lost on the next start.
     */
    @Test
    void towerDefaultShouldSurviveAJsonRoundTrip() throws JsonProcessingException {
        config.loadFromJson(createBaseConfigJson());
        addTowerWithOneEmptyFloor();
        config.setTowerDefaultTimeData(0, List.of(new TimeSlotConfig(77000L, 3600L)));

        ProgramConfig reloaded = new ProgramConfig();
        reloaded.loadFromJson(config.toJson());

        assertThat(reloaded.getTower(0).defaultTimeData()).hasSize(1);
        assertThat(reloaded.towerDefaultTimeData(0).get(0).price()).isEqualTo(77000L);
        assertThat(reloaded.towerDefaultTimeData(0).get(0).timeSeconds()).isEqualTo(3600L);
    }

    /**
     * Verifies that an {@code applicationProperties} file written before the per-tower
     * default existed loads unchanged.
     * Expected: The tower has no stored default (null) and still reports the built-in values.
     * Failure: The missing field breaks loading or yields an empty default.
     */
    @Test
    void towerWithoutAPricingDefaultShouldStillLoad() {
        String json = "{\"consecutiveTransaction\":0,\"version\":3,\"roomsPerTower\":["
                + "{\"towerNumber\":0,\"towerFloors\":1,\"towerRooms\":[{\"floor\":0,\"rooms\":[]}]}]}";

        config.loadFromJson(json);

        assertThat(config.getTower(0).defaultTimeData()).isNull();
        assertThat(config.towerDefaultTimeData(0)).hasSize(3);
    }

    /**
     * Verifies that saving copies the runtime price of a room over the value stored when
     * the room was created. Regression: rooms whose stored data was not empty were skipped,
     * so a price changed in the room configuration screen was dropped by the next save.
     * Expected: The stored time data is the one held by the room grid.
     * Failure: The edited price is not persisted and the room reloads with the old value.
     */
    @Test
    void syncingRoomTimeDataShouldReplaceTheStoredPricing() {
        config.loadFromJson(createBaseConfigJson());
        addTowerWithOneEmptyFloor();
        config.addRoomToFloor(0, 0, "1-101", 0, 0);

        RoomManager roomManager = new RoomManager(ZoneId.of("America/Bogota"));
        roomManager.buildRoomGrid(config.getRoomsPerTower());
        roomManager.setRoomCustomTimeData(0, 0, 0, new RoomTime[]{new RoomTime(55555L, 3600L)});

        config.syncRoomTimeData(roomManager.getRooms());

        RoomConfigData stored = config.getTower(0).towerRooms().get(0).rooms().get(0);
        assertThat(stored.roomString()).isEqualTo("1-101");
        assertThat(stored.customTimeData()).hasSize(1);
        assertThat(stored.customTimeData().get(0).price()).isEqualTo(55555L);
        assertThat(stored.customTimeData().get(0).timeSeconds()).isEqualTo(3600L);
    }

    // ========== On-screen keypad ==========

    /**
     * Verifies that an installation whose data file predates the setting keeps the
     * on-screen keypad on, which is what a touchscreen without a keyboard needs.
     * Expected: the keypad is enabled.
     * Failure: a kiosk loses the keypad after updating.
     */
    @Test
    void keypadShouldDefaultToEnabledWhenTheFileHasNoSetting() {
        config.loadFromJson(createBaseConfigJson());

        assertThat(config.isKeypadEnabled()).isTrue();
    }

    /**
     * Verifies that turning the keypad off is written to the data file and read back.
     * Expected: the reloaded configuration reports the keypad disabled.
     * Failure: the setting is lost on restart and the keypad keeps opening.
     */
    @Test
    void keypadSettingShouldSurviveAJsonRoundTrip() {
        config.loadFromJson(createBaseConfigJson());
        config.setKeypadEnabled(false);

        ProgramConfig reloaded = new ProgramConfig();
        reloaded.loadFromJson(config.toJson());

        assertThat(reloaded.isKeypadEnabled()).isFalse();
    }

    // ========== Numbering after a deletion ==========

    /**
     * Verifies that deleting a tower closes the gap: the towers after it move one
     * position back and their automatic room names follow.
     * Expected: two towers numbered 0 and 1, the second named 2-101.
     * Failure: labels, exports and printed room strings disagree after a deletion.
     */
    @Test
    void deletingATowerShouldRenumberTheRemainingOnes() {
        config.loadFromJson(createBaseConfigJson());
        config.setRoomsPerTower(new ArrayList<>(List.of(
                towerConfig(0, floorConfig(0, room("1-101", 0, 0))),
                towerConfig(1, floorConfig(0, room("2-101", 0, 0))),
                towerConfig(2, floorConfig(0, room("3-101", 0, 0))))));

        config.removeTower(1);

        assertThat(config.getTowerCount()).isEqualTo(2);
        assertThat(config.getTower(0).towerNumber()).isZero();
        assertThat(config.getTower(1).towerNumber()).isEqualTo(1);
        assertThat(config.getTower(1).towerRooms().get(0).rooms().get(0).roomString())
                .isEqualTo("2-101");
    }

    /**
     * Verifies that deleting a floor renumbers the floors below it, including the room
     * floor of every room and the automatic room names.
     * Expected: the former third floor becomes floor 1 with its room named 1-201.
     * Failure: the saved data keeps a hole the room grid cannot index.
     */
    @Test
    void deletingAFloorShouldRenumberTheFloorsBelowIt() {
        config.loadFromJson(createBaseConfigJson());
        config.setRoomsPerTower(new ArrayList<>(List.of(towerConfig(0,
                floorConfig(0, room("1-101", 0, 0)),
                floorConfig(1, room("1-201", 1, 0)),
                floorConfig(2, room("1-301", 2, 0))))));

        config.removeFloorFromTower(0, 1);

        assertThat(config.getTower(0).towerFloors()).isEqualTo(2);
        assertThat(config.getTower(0).towerRooms().get(1).floor()).isEqualTo(1);
        RoomConfigData renamed = config.getTower(0).towerRooms().get(1).rooms().get(0);
        assertThat(renamed.roomFloor()).isEqualTo(1);
        assertThat(renamed.roomString()).isEqualTo("1-201");
    }

    /**
     * Verifies that deleting a room renumbers the rooms after it.
     * Expected: the former third room becomes room 1 named 1-102.
     * Failure: the room numbers and the automatic names drift apart.
     */
    @Test
    void deletingARoomShouldRenumberTheRoomsAfterIt() {
        config.loadFromJson(createBaseConfigJson());
        config.setRoomsPerTower(new ArrayList<>(List.of(towerConfig(0,
                floorConfig(0, room("1-101", 0, 0), room("1-102", 0, 1), room("1-103", 0, 2))))));

        config.removeRoomFromFloor(0, 0, 1);

        List<RoomConfigData> rooms = config.getTower(0).towerRooms().get(0).rooms();
        assertThat(rooms).hasSize(2);
        assertThat(rooms.get(1).roomNumber()).isEqualTo(1);
        assertThat(rooms.get(1).roomString()).isEqualTo("1-102");
    }

    /**
     * Verifies that a room the operator renamed keeps its name when a deletion moves it.
     * Expected: the remaining room is still called "Suite".
     * Failure: a deletion overwrites a name typed on the room configuration screen.
     */
    @Test
    void aRenamedRoomShouldKeepItsNameWhenTheNumberingMoves() {
        config.loadFromJson(createBaseConfigJson());
        config.setRoomsPerTower(new ArrayList<>(List.of(towerConfig(0,
                floorConfig(0, room("VIP", 0, 0), room("Suite", 0, 1))))));

        config.removeRoomFromFloor(0, 0, 0);

        List<RoomConfigData> rooms = config.getTower(0).towerRooms().get(0).rooms();
        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).roomNumber()).isZero();
        assertThat(rooms.get(0).roomString()).isEqualTo("Suite");
    }

    /**
     * Verifies that normalization repairs a stored configuration whose numbers no longer
     * match their positions, which is how a file saved after a middle deletion loads.
     * Expected: the second tower is number 1 and its room is named 2-101.
     * Failure: the app loads a hole and crashes or labels the wrong tower.
     */
    @Test
    void normalizePositionsShouldRepairAStoredGap() {
        config.loadFromJson(createBaseConfigJson());
        config.setRoomsPerTower(new ArrayList<>(List.of(
                towerConfig(0, floorConfig(0, room("1-101", 0, 0))),
                towerConfig(2, floorConfig(0, room("3-101", 0, 0))))));

        config.normalizePositions();

        assertThat(config.getTower(1).towerNumber()).isEqualTo(1);
        assertThat(config.getTower(1).towerRooms().get(0).rooms().get(0).roomString())
                .isEqualTo("2-101");
    }

    /**
     * Verifies that loading a stored configuration whose numbers no longer match their
     * positions repairs it, which is the upgrade path for a file saved after a deletion.
     * Expected: the second tower loads as tower 1 with its room renamed 2-101.
     * Failure: the load hook is removed and the app keeps the hole that used to crash.
     */
    @Test
    void loadFromJsonShouldRepairAStoredGap() {
        config.loadFromJson(createBaseConfigJson());
        config.setRoomsPerTower(new ArrayList<>(List.of(
                towerConfig(0, floorConfig(0, room("1-101", 0, 0))),
                towerConfig(2, floorConfig(0, room("3-101", 0, 0))))));
        config.ensureSchemaVersion();

        ProgramConfig reloaded = new ProgramConfig();
        reloaded.loadFromJson(config.toJson());

        assertThat(reloaded.getTower(1).towerNumber()).isEqualTo(1);
        assertThat(reloaded.getTower(1).towerRooms().get(0).rooms().get(0).roomString())
                .isEqualTo("2-101");
    }

    // ========== Helper ==========

    private void addTowerWithOneEmptyFloor() {
        List<FloorConfig> towerRooms = new ArrayList<>();
        towerRooms.add(new FloorConfig(0, new ArrayList<>()));
        config.addTower(towerRooms);
    }

    private String createBaseConfigJson() {
        return "{\"consecutiveTransaction\":0,\"motelName\":\"Test Motel\","
                + "\"motelAddress\":\"Test Address\",\"motelID\":\"NIT 000.000.000-0\"}";
    }

    private static TowerConfig towerConfig(int towerNumber, FloorConfig... floors) {
        return new TowerConfig(towerNumber, floors.length, List.of(floors));
    }

    private static FloorConfig floorConfig(int floorNumber, RoomConfigData... rooms) {
        return new FloorConfig(floorNumber, List.of(rooms));
    }

    private static RoomConfigData room(String roomString, int floor, int number) {
        return new RoomConfigData(roomString, floor, number, null);
    }
}
