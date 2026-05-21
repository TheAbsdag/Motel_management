package view;

/**
 * Enum for CardLayout card identifiers used in {@link UserGUI}.
 * Replaces hardcoded string literals to prevent typos.
 */
public enum ViewCard {

    FLOOR_VIEW("floorView"),
    ROOM_VIEW("roomView"),
    SELLING_VIEW("sellingView"),
    TURN_SELECT_VIEW("turnSelectView"),
    TURN_MANAGER_VIEW("turnManagerView"),
    MANAGEMENT_SELECT_VIEW("managementSelectView"),
    INVENTORY_VIEW("inventoryView"),
    HISTORY_VIEW("historyView"),
    ROOM_CHANGE_VIEW("roomChangeView"),
    APP_OPTIONS_VIEW("appOptionsView"),
    SPENDING_REGISTER_VIEW("spendingRegisterView"),
    EXTRA_TURN_CHANGES_VIEW("extraTurnChangesView"),
    ROOM_SUMMARY_VIEW("roomSummaryView"),
    PRINTER_CONFIG_VIEW("printerConfigView"),
    MOTEL_DATA_CONFIG_VIEW("motelDataConfigView"),
    DATA_SAVING_CONFIG_VIEW("dataSavingConfigView"),
    FLOOR_CONFIG_VIEW("floorConfigView"),
    TIME_CONFIG_VIEW("timeConfigView");

    private final String cardName;

    ViewCard(String cardName) {
        this.cardName = cardName;
    }

    public String getCardName() {
        return cardName;
    }
}
