package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import model.json.FloorConfig;
import model.json.ObjectMapperFactory;
import model.json.RoomConfigData;
import model.json.TowerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

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
        assertThat(config.getSchemaVersion()).isZero();
        assertThat(config.isLegacyFormat()).isTrue();
    }

    @Test
    void shouldRecognizeVersionOne() {
        String json = "{\"consecutiveTransaction\":0,\"motelName\":\"Test Motel\","
                + "\"motelAddress\":\"Test Address\",\"motelID\":\"NIT 000.000.000-0\",\"version\":1}";
        config.loadFromJson(json);
        assertThat(config.getSchemaVersion()).isEqualTo(1);
        assertThat(config.isLegacyFormat()).isFalse();
    }

    @Test
    void ensureSchemaVersionShouldSetVersion() throws JsonProcessingException {
        config.loadFromJson(createBaseConfigJson());
        config.ensureSchemaVersion();
        assertThat(config.getSchemaVersion()).isEqualTo(2);
    }

    @Test
    void buildRoomStringShouldFormatCorrectly() {
        assertThat(ProgramConfig.buildRoomString(1, 0, 4)).isEqualTo("1-105");
    }

    @Test
    void buildRoomStringShouldHandleDifferentNumbers() {
        assertThat(ProgramConfig.buildRoomString(2, 1, 7)).isEqualTo("2-208");
    }

    // ========== Room Grid CRUD ==========

    @Test
    void addTowerShouldAppendToRoomsPerTower() {
        config.loadFromJson(createBaseConfigJson());
        config.addTower(1, 2, new ArrayList<>());
        assertThat(config.getTowerCount()).isEqualTo(1);
        assertThat(config.getTower(0).towerNumber()).isEqualTo(1);
        assertThat(config.getTower(0).towerFloors()).isEqualTo(2);
    }

    @Test
    void removeTowerShouldReduceCount() {
        config.loadFromJson(createBaseConfigJson());
        config.addTower(1, 1, new ArrayList<>());
        config.addTower(2, 1, new ArrayList<>());
        config.removeTower(0);
        assertThat(config.getTowerCount()).isEqualTo(1);
        assertThat(config.getTower(0).towerNumber()).isEqualTo(2);
    }

    @Test
    void addRoomToFloorShouldAppendRoom() {
        config.loadFromJson(createBaseConfigJson());
        List<FloorConfig> towerRooms = new ArrayList<>();
        towerRooms.add(new FloorConfig(0, new ArrayList<>()));
        config.addTower(1, 1, towerRooms);

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
        config.addTower(1, 1, towerRooms);

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
        config.addTower(1, 1, towerRooms);

        config.setRoomString(0, 0, 0, "VIP-1");

        String name = config.getTower(0).towerRooms().get(0).rooms().get(0).roomString();
        assertThat(name).isEqualTo("VIP-1");
    }

    // ========== Helper ==========

    private String createBaseConfigJson() {
        return "{\"consecutiveTransaction\":0,\"motelName\":\"Test Motel\","
                + "\"motelAddress\":\"Test Address\",\"motelID\":\"NIT 000.000.000-0\"}";
    }
}
