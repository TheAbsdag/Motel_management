package model;

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
