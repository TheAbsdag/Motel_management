package model;

import org.json.JSONObject;

/**
 * Manages application configuration data (motel info, printer, transaction counter).
 * Extracted from {@link MotelManagement} for single-responsibility.
 *
 * @author Santiago
 */
public class ProgramConfig {

    private JSONObject programData;
    private int consecutiveTransaction;
    private String motelName;
    private String motelAddress;
    private String motelID;
    private String configuredPrinterName;

    public ProgramConfig() {
        this.programData = new JSONObject();
        this.consecutiveTransaction = 0;
    }

    // ========== Initialization ==========

    /**
     * Loads application properties from JSON data.
     *
     * @param rawData the JSON object from FileManager
     */
    public void loadFromJson(JSONObject rawData) {
        this.programData = rawData;
        this.consecutiveTransaction = programData.getInt("consecutiveTransaction");
        this.motelName = programData.getString("motelName");
        this.motelAddress = programData.getString("motelAddress");
        this.motelID = programData.getString("motelID");

        if (programData.has("printerName")) {
            this.configuredPrinterName = programData.getString("printerName");
        } else {
            this.configuredPrinterName = null;
        }
    }

    // ========== Transaction Counter ==========

    /**
     * Increments the consecutive transaction counter in memory.
     * Does NOT save to disk (the periodic auto-save handles persistence).
     */
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

    // ========== Property Getters ==========

    public String getMotelName() { return motelName; }
    public String getMotelAddress() { return motelAddress; }
    public String getMotelID() { return motelID; }
    public JSONObject getProgramData() { return programData; }
}
