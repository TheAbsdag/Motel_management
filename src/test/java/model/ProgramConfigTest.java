package model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProgramConfig} — application configuration,
 * transaction counter, and printer configuration.
 */
class ProgramConfigTest {

    private ProgramConfig config;

    @BeforeEach
    void setUp() {
        config = new ProgramConfig();
    }

    // ========== Initial State ==========

    @Test
    void shouldStartWithZeroTransactions() {
        assertThat(config.getConsecutiveTransaction()).isZero();
    }

    @Test
    void shouldHaveEmptyProgramDataInitially() {
        assertThat(config.getProgramData()).isNotNull();
        assertThat(config.getProgramData().isEmpty()).isTrue();
    }

    // ========== Loading from JSON ==========

    @Test
    void shouldLoadAllFieldsFromJson() {
        JSONObject rawData = new JSONObject();
        rawData.put("consecutiveTransaction", 42);
        rawData.put("motelName", "Motel Paraiso");
        rawData.put("motelAddress", "Calle 123 #45-67");
        rawData.put("motelID", "NIT 900.123.456-7");
        rawData.put("printerName", "XP-80C");
        rawData.put("roomsPerTower", new org.json.JSONArray());

        config.loadFromJson(rawData);

        assertThat(config.getConsecutiveTransaction()).isEqualTo(42);
        assertThat(config.getMotelName()).isEqualTo("Motel Paraiso");
        assertThat(config.getMotelAddress()).isEqualTo("Calle 123 #45-67");
        assertThat(config.getMotelID()).isEqualTo("NIT 900.123.456-7");
        assertThat(config.getConfiguredPrinterName()).isEqualTo("XP-80C");
    }

    @Test
    void shouldSetPrinterNameToNullWhenNotInJson() {
        JSONObject rawData = new JSONObject();
        rawData.put("consecutiveTransaction", 0);
        rawData.put("motelName", "Test");
        rawData.put("motelAddress", "Addr");
        rawData.put("motelID", "NIT");

        config.loadFromJson(rawData);

        assertThat(config.getConfiguredPrinterName()).isNull();
    }

    // ========== Transaction Counter ==========

    @Test
    void shouldIncrementTransactionCounter() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        config.addConsecutiveTransaction();

        assertThat(config.getConsecutiveTransaction()).isEqualTo(1);
    }

    @Test
    void shouldIncrementMultipleTimes() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        config.addConsecutiveTransaction();
        config.addConsecutiveTransaction();
        config.addConsecutiveTransaction();

        assertThat(config.getConsecutiveTransaction()).isEqualTo(3);
    }

    @Test
    void shouldPersistCounterInProgramData() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        config.addConsecutiveTransaction();

        assertThat(config.getProgramData().getInt("consecutiveTransaction")).isEqualTo(1);
    }

    @Test
    void shouldContinueFromLoadedCounter() {
        JSONObject rawData = createBaseConfig();
        rawData.put("consecutiveTransaction", 100);
        config.loadFromJson(rawData);

        config.addConsecutiveTransaction();

        assertThat(config.getConsecutiveTransaction()).isEqualTo(101);
        assertThat(config.getProgramData().getInt("consecutiveTransaction")).isEqualTo(101);
    }

    // ========== Printer Configuration ==========

    @Test
    void shouldSavePrinterName() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        config.savePrinterConfiguration("ThermalPrinter-2000");

        assertThat(config.getConfiguredPrinterName()).isEqualTo("ThermalPrinter-2000");
        assertThat(config.getProgramData().getString("printerName")).isEqualTo("ThermalPrinter-2000");
    }

    @Test
    void shouldUpdateExistingPrinterName() {
        JSONObject rawData = createBaseConfig();
        rawData.put("printerName", "OldPrinter");
        config.loadFromJson(rawData);

        config.savePrinterConfiguration("NewPrinter");

        assertThat(config.getConfiguredPrinterName()).isEqualTo("NewPrinter");
    }

    // ========== Property Getters from Loaded Config ==========

    @Test
    void shouldReturnMotelProperties() {
        JSONObject rawData = createBaseConfig();
        rawData.put("motelName", "Motel El Descanso");
        rawData.put("motelAddress", "Av. Siempre Viva 742");
        rawData.put("motelID", "NIT 800.456.789-1");

        config.loadFromJson(rawData);

        assertThat(config.getMotelName()).isEqualTo("Motel El Descanso");
        assertThat(config.getMotelAddress()).isEqualTo("Av. Siempre Viva 742");
        assertThat(config.getMotelID()).isEqualTo("NIT 800.456.789-1");
    }

    // ========== Program Data Reference ==========

    @Test
    void shouldReturnSameProgramDataObject() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        JSONObject data = config.getProgramData();
        data.put("customField", "test");

        assertThat(config.getProgramData().getString("customField")).isEqualTo("test");
    }

    // ========== Schema Version ==========

    @Test
    void shouldDetectLegacyFormatWhenVersionIsMissing() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        assertThat(config.getSchemaVersion()).isZero();
        assertThat(config.isLegacyFormat()).isTrue();
    }

    @Test
    void shouldRecognizeVersionOne() {
        JSONObject rawData = createBaseConfig();
        rawData.put("version", 1);
        config.loadFromJson(rawData);

        assertThat(config.getSchemaVersion()).isEqualTo(1);
        assertThat(config.isLegacyFormat()).isFalse();
    }

    @Test
    void ensureSchemaVersionShouldWriteVersionField() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        config.ensureSchemaVersion();

        assertThat(config.getProgramData().getInt("version")).isEqualTo(2);
        assertThat(config.getSchemaVersion()).isEqualTo(2);
    }

    // ========== buildRoomString ==========

    @Test
    void buildRoomStringShouldFormatCorrectly() {
        String result = ProgramConfig.buildRoomString(1, 0, 4);

        assertThat(result).isEqualTo("1-105");
    }

    @Test
    void buildRoomStringShouldHandleDifferentNumbers() {
        String result = ProgramConfig.buildRoomString(2, 1, 7);

        assertThat(result).isEqualTo("2-208");
    }

    // ========== Room Grid CRUD ==========

    @Test
    void addTowerShouldAppendToRoomsPerTower() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        config.addTower(1, 2, new JSONArray());

        assertThat(config.getTowerCount()).isEqualTo(1);
        assertThat(config.getTower(0).getInt("towerNumber")).isEqualTo(1);
        assertThat(config.getTower(0).getInt("towerFloors")).isEqualTo(2);
    }

    @Test
    void removeTowerShouldReduceCount() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);
        config.addTower(1, 1, new JSONArray());
        config.addTower(2, 1, new JSONArray());

        config.removeTower(0);

        assertThat(config.getTowerCount()).isEqualTo(1);
        assertThat(config.getTower(0).getInt("towerNumber")).isEqualTo(2);
    }

    @Test
    void addRoomToFloorShouldAppendRoom() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        JSONArray towerRooms = new JSONArray();
        JSONObject floorData = new JSONObject();
        floorData.put("floor", 0);
        floorData.put("rooms", new JSONArray());
        towerRooms.put(floorData);
        config.addTower(1, 1, towerRooms);

        config.addRoomToFloor(0, 0, "1-102", 0, 1);

        JSONArray rooms = config.getTower(0).getJSONArray("towerRooms")
                .getJSONObject(0).getJSONArray("rooms");
        assertThat(rooms).hasSize(1);
        assertThat(rooms.getJSONObject(0).getString("roomString")).isEqualTo("1-102");
    }

    @Test
    void removeRoomFromFloorShouldReduceCount() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        JSONArray towerRooms = new JSONArray();
        JSONObject floorData = new JSONObject();
        floorData.put("floor", 0);
        JSONArray rooms = new JSONArray();
        JSONObject roomJson = new JSONObject();
        roomJson.put("roomString", "1-101");
        roomJson.put("roomFloor", 0);
        roomJson.put("roomNumber", 0);
        rooms.put(roomJson);
        floorData.put("rooms", rooms);
        towerRooms.put(floorData);
        config.addTower(1, 1, towerRooms);

        config.removeRoomFromFloor(0, 0, 0);

        JSONArray updated = config.getTower(0).getJSONArray("towerRooms")
                .getJSONObject(0).getJSONArray("rooms");
        assertThat(updated).isEmpty();
    }

    @Test
    void setRoomStringShouldRenameRoom() {
        JSONObject rawData = createBaseConfig();
        config.loadFromJson(rawData);

        JSONArray towerRooms = new JSONArray();
        JSONObject floorData = new JSONObject();
        floorData.put("floor", 0);
        JSONArray rooms = new JSONArray();
        JSONObject roomJson = new JSONObject();
        roomJson.put("roomString", "1-101");
        roomJson.put("roomFloor", 0);
        roomJson.put("roomNumber", 0);
        rooms.put(roomJson);
        floorData.put("rooms", rooms);
        towerRooms.put(floorData);
        config.addTower(1, 1, towerRooms);

        config.setRoomString(0, 0, 0, "VIP-1");

        String name = config.getTower(0).getJSONArray("towerRooms")
                .getJSONObject(0).getJSONArray("rooms")
                .getJSONObject(0).getString("roomString");
        assertThat(name).isEqualTo("VIP-1");
    }

    // ========== Helper ==========

    private JSONObject createBaseConfig() {
        JSONObject data = new JSONObject();
        data.put("consecutiveTransaction", 0);
        data.put("motelName", "Test Motel");
        data.put("motelAddress", "Test Address");
        data.put("motelID", "NIT 000.000.000-0");
        return data;
    }
}
