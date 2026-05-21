package model.modelManagers;

import model.modelManagers.FileManager;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import model.Item;
import model.ProgramConfig;
import model.Register;
import model.Room;
import model.Turn;
import org.json.JSONArray;
import org.json.JSONObject;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import model.dto.TurnActivityData;
import model.dto.TurnHistoryData;
import model.dto.TurnSummaryItemData;
import model.turn.RoomBookingActivity;
import model.turn.SaleActivity;
import model.turn.TurnActivity;
import model.turn.TurnDetails;

/**
 * Central model facade that coordinates sub-models, persistence, and printing.
 *
 * <p>Delegates to:
 * <ul>
 *   <li>{@link RoomManager} — room grid and room-level operations</li>
 *   <li>{@link ProgramConfig} — application configuration data</li>
 *   <li>{@link Register} — inventory and selling cart</li>
 *   <li>{@link Turn} — shift/turn management</li>
 *   <li>{@link Printer} — receipt printing</li>
 *   <li>{@link FileManager} — JSON file persistence</li>
 * </ul>
 *
 * @author Santiago
 */
public class MotelManagement {

    private final FileManager files;
    private final RoomManager roomManager;
    private final ProgramConfig programConfig;
    private final Printer printer;
    private final Register register;
    private final Turn turn;
    private Instant currentTime;
    private ZonedDateTime localizedTime;
    private final ZoneId zoneID;
    private List<Turn> turnHistory; //TODO: change, history management to be built on outside platform as well, turns can expand more than a thousand in a year, this would eat the memory like crazy 
    private List<String> overtimeList;

    public MotelManagement() {
        files = new FileManager();
        zoneID = ZoneId.of("America/Bogota");
        currentTime = Instant.now();
        localizedTime = currentTime.atZone(zoneID);
        turn = new Turn(currentTime, zoneID);
        register = new Register();
        roomManager = new RoomManager(zoneID);
        programConfig = new ProgramConfig();
        turnHistory = new ArrayList<>();
        printer = new Printer();
        overtimeList = new CopyOnWriteArrayList<>();
    }

    // ========== Initialization ==========

    public void prepareProgramData() {
        JSONObject rawConfig = files.getJsonData("applicationProperties");
        programConfig.loadFromJson(rawConfig);

        printer.setPrinterVariables(
                programConfig.getMotelName(),
                programConfig.getMotelAddress(),
                programConfig.getMotelID());

        String savedPrinter = programConfig.getConfiguredPrinterName();
        if (savedPrinter != null) {
            printer.setPrinterService(savedPrinter);
        }

        roomManager.buildRoomGrid(programConfig.getProgramData());
        roomManager.restoreRoomStates(files.getJsonData("roomsInformation"));
    }

    public boolean prepareTurnRegisterData() {
        JSONObject turnData = files.getJsonData("turn");
        JSONObject inventoryData = files.getJsonData("inventory");
        boolean validPreviousTurn = false;
        if (!(turnData == null || turnData.isEmpty())) {
            validPreviousTurn = turn.setPreviousTurnJSON(turnData);
            if (!validPreviousTurn) {
                System.out.println("Found previous turn, but it's no longer active, backing up");
                this.timeInformationUpdate();
                turn.turnEnd(currentTime);
                files.saveHistoryData(turn.getDetailedTurnInformationAsJson(), "turnClosedImproperly", localizedTime);
                TurnReportGenerator.generateReport(turn.getDetailedTurnInformation());
            }
        }
        if (!(inventoryData == null || inventoryData.isEmpty())) {
            JSONArray itemArray = inventoryData.getJSONArray("inventoryItems");
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject currentItem = itemArray.getJSONObject(i);
                register.createItem(
                        currentItem.getString("itemName"),
                        currentItem.getInt("price"),
                        currentItem.getInt("quantity"),
                        currentItem.getInt("itemID"));
            }
        }
        return validPreviousTurn;
    }

    // ========== Time Management ==========

    public void timeInformationUpdate() {
        currentTime = Instant.now();
        localizedTime = currentTime.atZone(zoneID);
    }

    public String getCurrentLocalizedTime() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("hh:mm:ss").appendLiteral(' ')
                .appendText(ChronoField.AMPM_OF_DAY, new HashMap<Long, String>() {{
                    put(0L, "\tAM"); put(1L, "\tPM");
                }})
                .toFormatter();
        return localizedTime.format(formatter);
    }

    public String getCurrentLocalizedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        return localizedTime.format(formatter);
    }

    // ========== Room Operations (delegated to RoomManager) ==========

    public int[][] getRoomsArray() {
        return roomManager.getRoomsArray();
    }

    public Room getRoom(int tower, int floor, int room) {
        return roomManager.getRoom(tower, floor, room);
    }

    public void registerRoomTimeAdded(int tower, int floor, int room, int service, long price, boolean print) {
        addConsecutiveTransaction();
        int currentExtension = roomManager.registerRoomTimeAdded(tower, floor, room, service, currentTime);
        RoomBookingActivity roomChange = turn.registerRoomChange(
                roomManager.getRoom(tower, floor, room), currentTime, price, currentExtension,
                programConfig.getConsecutiveTransaction());
        printer.printRoomTimeSell(roomChange, programConfig.getConsecutiveTransaction(), !print);
    }

    public void registerRoomTimeEnd(int tower, int floor, int room) {
        roomManager.registerRoomTimeEnd(tower, floor, room, currentTime);
        turn.registerRoomChange(roomManager.getRoom(tower, floor, room), currentTime, 0, 0, 0);
    }

    public boolean changeRoomTimeToAnother() {
        boolean valid = roomManager.changeRoomTimeToAnother(currentTime);
        if (valid) {
            turn.registerRoomSwap(roomManager.getCurrentRoom(), roomManager.getDesiredChangeRoom(), currentTime);
        }
        return valid;
    }

    public String getRemainingTimeRoom(int tower, int floor, int room) {
        return roomManager.getRemainingTimeRoom(tower, floor, room, currentTime);
    }

    public String getStartTimeRoom(int tower, int floor, int room) {
        return roomManager.getStartTimeRoom(tower, floor, room);
    }

    public String getStartDateRoom(int tower, int floor, int room) {
        return roomManager.getStartDateRoom(tower, floor, room);
    }

    // ========== Room View State (delegated to RoomManager) ==========

    public int getCurrentFloorViewed() { return roomManager.getCurrentFloorViewed(); }
    public int getCurrentRoomViewed() { return roomManager.getCurrentRoomViewed(); }
    public int getCurrentTowerViewed() { return roomManager.getCurrentTowerViewed(); }
    public int getCurrentServiceDesired() { return roomManager.getCurrentServiceDesired(); }

    public void setCurrentServiceDesired(int service) { roomManager.setCurrentServiceDesired(service); }

    public void setCurrentFloorRoom(int tower, int floor, int room) {
        roomManager.setCurrentFloorRoom(tower, floor, room);
    }

    public void setDesiredRoomChange(int tower, int floor, int room) {
        roomManager.setDesiredRoomChange(tower, floor, room);
    }

    public int getSelectedRoomChangeRoom() { return roomManager.getSelectedRoomChangeRoom(); }
    public int getSelectedRoomChangeFloor() { return roomManager.getSelectedRoomChangeFloor(); }
    public int getSelectedRoomChangeTower() { return roomManager.getSelectedRoomChangeTower(); }

    // ========== Turn Operations ==========

    public void setNewTurn(int turnNumber) {
        this.turn.setNewTurn(turnNumber, currentTime);
    }

    public void turnEnded() {
        turn.turnEnd(currentTime);
    }

    /**
     * Prints the current turn report without ending the turn.
     * Used for mid-turn printing (summarized or detailed).
     *
     * @param option 2 = summarized, 3 = detailed
     */
    public void turnPrintNoEnd(int option) {
        TurnDetails details = turn.getBasicTurnInformation();
        switch (option) {
            case 2 -> printer.printSummarizedCurrentTurn(details);
            case 3 -> printer.printDetailedCurrentTurn(details);
            default -> { /* no printing */ }
        }
    }

    public void turnEndPrint(int option) {
        TurnDetails details = turn.getDetailedTurnInformation();
        files.saveHistoryData(details.toJson(), "turn", localizedTime);
        TurnReportGenerator.generateReport(details);
        switch (option) {
            case 1 -> { printer.printSummarizedTurn(details, true);  printer.printDetailedTurn(details, true); }
            case 2 -> { printer.printSummarizedTurn(details, false); printer.printDetailedTurn(details, true); }
            case 3 -> { printer.printSummarizedTurn(details, true);  printer.printDetailedTurn(details, false); }
            default -> { /* no printing */ }
        }
        files.clearBackupFiles();
    }

    public void turnHistoryPrint(int option, int selectedRow) {
        TurnDetails details = turnHistory.get(selectedRow).getBasicTurnInformation();
        switch (option) {
            case 1 -> { printer.printSummarizedTurn(details, true);  printer.printDetailedTurn(details, true); }
            case 2 -> { printer.printSummarizedTurn(details, false); printer.printDetailedTurn(details, true); }
            case 3 -> { printer.printSummarizedTurn(details, true);  printer.printDetailedTurn(details, false); }
            default -> { /* no printing */ }
        }
    }

    public long getTurnNumber() {
        return turn.getTurnNumber();
    }

    // ========== Inventory / Selling Operations (delegated to Register) ==========

    public void restartSaleManager() {
        register.newSellingList();
    }

    public JSONObject getInventoryData() {
        return register.getInventoryData();
    }

    /** DTO-based method for saving item information. */
    public boolean saveItemInformation(InventoryItemData item) {
        return register.saveItemInformation(new Item(item.name(), item.price(), item.quantity(), item.itemID()));
    }

    /** DTO-based method for creating a new item. */
    public void newItemCreated(String name, long price, long quantity) {
        register.createNewItem(name, price, quantity);
    }

    /** DTO-based method for deleting an inventory item. */
    public void deleteItemFromInventory(long itemID) {
        register.deleteItemById(itemID);
    }

    public void addItemToSelling(long itemID, long quantity, boolean courtesySale) {
        if (!courtesySale) {
            register.addItemToList(register.getItemFromItemID(itemID), quantity);
        } else {
            register.addCourtesyItemToList(register.getItemFromItemID(itemID), quantity);
        }
    }

    public void removeItemToSelling(long itemID) {
        register.removeFromList(register.getItemFromItemID(itemID));
    }

    public long getCurrentTotalPriceSellingList() {
        return register.getTotalPriceRegisterList();
    }

    public void roomSaleFinished(boolean print) {
        addConsecutiveTransaction();
        Room roomSoldTo = roomManager.getRoomForSale();
        SaleActivity transaction = turn.saveTransactionInformation(
                new JSONArray(register.consumeRegisterListForSale()),
                roomSoldTo, currentTime, programConfig.getConsecutiveTransaction());
        printer.printItemSold(transaction, programConfig.getConsecutiveTransaction(), !print);
    }

    /**
     * DTO-based method for reverting an item sale from the current turn.
     */
    public void revertItemSale(TurnActivityData activity) {
        long itemID = activity.getItemID();
        long quantity = activity.getQuantity();
        Item currentItem = register.getItemFromItemID(itemID);
        if (currentItem != null) {
            currentItem.itemAdded(quantity);
        }
        TurnActivity turnActivity = turn.findActivity(activity.getConsecutiveTrans(), "sale");
        if (turnActivity != null) {
            turn.reverseItemSaleFromTurn(turnActivity, itemID, quantity);
        }
    }

    // ========== Spending / Extra Changes / Refunds ==========

    /**
     * Registers a spending (expense) transaction in the current turn.
     */
    public void addSpendingTransaction(String conceptSpending, long value) {
        addConsecutiveTransaction();
        turn.registerSpendingTransaction(conceptSpending, value * -1L,
                programConfig.getConsecutiveTransaction(), currentTime);
    }

    /**
     * Registers a bank transfer or safe deposit in the current turn.
     * @param type "bankTransfer" or "safeDeposit"
     */
    public void addExtraChangeTransaction(String description, long value, String type) {
        addConsecutiveTransaction();
        turn.registerExtraChangeTransaction(description, value * -1L, type,
                programConfig.getConsecutiveTransaction(), currentTime);
    }

    /**
     * Refunds a transaction from the current turn.
     * Handles both room and sale refunds, restores inventory stock for item sales.
     *
     * @param consecutiveTrans the transaction number to refund
     * @param changeType       "room" or "sale"
     * @param itemID           0 for room refunds; the item ID for sale refunds
     * @param itemQty          0 for room refunds; the item quantity for sale refunds
     */
    public void refundItemSale(int consecutiveTrans, String changeType, long itemID, long itemQty) {
        addConsecutiveTransaction();
        TurnActivity activity = turn.findActivity(consecutiveTrans, changeType);
        if (activity == null) return;
        if ("sale".equals(changeType) && itemID > 0) {
            Item currentItem = register.getItemFromItemID(itemID);
            if (currentItem != null) {
                currentItem.itemAdded(itemQty);
            }
        }
        turn.refundTransactionFromTurn(activity,
                programConfig.getConsecutiveTransaction(), currentTime, itemID, itemQty);
    }

    // ========== Overtime Tracking ==========

    public void addToOvertimeList(String roomString) {
        if (!overtimeList.contains(roomString)) {
            overtimeList.add(roomString);
        }
    }

    public void removeFromOvertimeList(String roomString) {
        overtimeList.remove(roomString);
    }

    public List<String> getOvertimeList() {
        return overtimeList;
    }

    // ========== Transaction Counter (delegated to ProgramConfig) ==========

    private void addConsecutiveTransaction() {
        programConfig.addConsecutiveTransaction();
    }

    // ========== Printer Operations ==========

    public List<String> getPrinterLists() {
        return printer.getPrinterServiceNameList();
    }

    public String getCurrentPrinterName() {
        return printer.getCurrentPrinterName();
    }

    public void setPrinter(String printerName) {
        printer.setPrinterService(printerName);
    }

    public boolean isPrinterAvailable() {
        return !"N/A".equals(printer.getCurrentPrinterName());
    }

    public String setFirstAvailablePrinter() {
        String name = printer.getFirstAvailablePrinterName();
        if (name != null) {
            printer.setPrinterService(name);
        }
        return name;
    }

    public String getConfiguredPrinterName() {
        return programConfig.getConfiguredPrinterName();
    }

    public void savePrinterConfiguration(String printerName) {
        programConfig.savePrinterConfiguration(printerName);
        files.saveJsonMainDataPath(programConfig.getProgramData(), "applicationProperties");
    }

    // ========== File Persistence ==========

    public void saveFilesForMainService() {
        JSONObject roomData = new JSONObject();
        roomData.put("rooms", roomManager.getRoomDataForSaving());

        Map<String, JSONObject> dataMap = new LinkedHashMap<>();
        dataMap.put("turn", turn.getDetailedTurnInformationAsJson());
        dataMap.put("inventory", register.getInventoryData());
        dataMap.put("roomsInformation", roomData);
        dataMap.put("applicationProperties", programConfig.getProgramData());

        files.saveAllMainDataAtomic(dataMap);
    }

    public void saveFilesForBackup(String saveType) {
        timeInformationUpdate();

        files.saveJsonBackupDataPath(turn.getDetailedTurnInformationAsJson(), "turn", localizedTime, saveType);
        files.saveJsonBackupDataPath(register.getInventoryData(), "inventory", localizedTime, saveType);

        JSONObject roomData = new JSONObject();
        roomData.put("rooms", roomManager.getRoomDataForSaving());
        files.saveJsonBackupDataPath(roomData, "roomsInformation", localizedTime, saveType);
        files.saveJsonBackupDataPath(programConfig.getProgramData(), "applicationProperties", localizedTime, saveType);
    }

    // ========== History Operations ==========

    public JSONArray getHistoryData() {
        JSONArray currentHistory = files.getHistoryFiles();
        turnHistory.clear();
        for (int i = 0; i < currentHistory.length(); i++) {
            JSONObject currentTurn = currentHistory.getJSONObject(i);
            JSONArray activityArray = currentTurn.getJSONArray("turnActivity");
            Instant start = ZonedDateTime.parse(currentTurn.getString("turnStart")).toInstant();
            Instant end = ZonedDateTime.parse(currentTurn.getString("turnEnd")).toInstant();
            int turnNum = currentTurn.getInt("turnNumber");
            Turn newTurn = new Turn(start, end, turnNum, zoneID, activityArray);
            turnHistory.add(newTurn);
        }
        return currentHistory;
    }

    public void generateHistoryTurnReport(int selectedRow) {
        TurnDetails details = turnHistory.get(selectedRow).getDetailedTurnInformation();
        TurnReportGenerator.generateReport(details);
    }

    // ========== DTO Access Methods ==========

    public List<InventoryItemData> getInventoryItemDataList() {
        return register.getInventoryItemDataList();
    }

    public List<SellingItemData> getSellingItemDataList() {
        return register.getSellingItemDataList();
    }

    public List<TurnActivityData> getTurnActivityDataList() {
        return turn.getActivityDataList();
    }

    /**
     * Returns the current turn's detailed information including all computed totals.
     * Used by TurnController to populate the turn manager view.
     */
    public TurnDetails getCurrentTurnDetailedInfo() {
        return turn.getDetailedTurnInformation();
    }

    public List<TurnSummaryItemData> getTurnSummaryDataList() {
        return turn.getSummaryDataList();
    }

    public List<TurnHistoryData> getTurnHistoryDataList() {
        JSONArray rawHistory = files.getHistoryFiles();
        List<TurnHistoryData> result = new ArrayList<>();
        for (int i = 0; i < rawHistory.length(); i++) {
            JSONObject currentTurn = rawHistory.getJSONObject(i);
            try {
                JSONArray activityArray = currentTurn.getJSONArray("turnActivity");
                Instant start = ZonedDateTime.parse(currentTurn.getString("turnStart")).toInstant();
                Instant end = ZonedDateTime.parse(currentTurn.getString("turnEnd")).toInstant();
                int turnNum = currentTurn.getInt("turnNumber");
                Turn historyTurn = new Turn(start, end, turnNum, zoneID, activityArray);
                turnHistory.add(historyTurn);

                ZonedDateTime turnStartZ = ZonedDateTime.parse(currentTurn.getString("turnStart"));
                ZonedDateTime turnEndZ = ZonedDateTime.parse(currentTurn.getString("turnEnd"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                Duration durationRaw = Duration.between(turnStartZ, turnEndZ);
                long hours = durationRaw.toHours();
                long minutes = durationRaw.minusHours(hours).toMinutes();
                String duration = hours + ":" + minutes;

                long totalSales = currentTurn.optLong("totalSales");
                long totalItems = currentTurn.optLong("totalItems");
                long totalRooms = currentTurn.optLong("totalRooms");
                long totalRefunds = currentTurn.optLong("totalRefunds");
                long totalSpending = currentTurn.optLong("totalSpending");
                long totalTurnVal = currentTurn.optLong("totalTurn");
                long totalBankTransfers = currentTurn.optLong("totalBankTransfers");
                long totalDeposits = currentTurn.optLong("totalDeposits");
                long totalNet = currentTurn.optLong("totalNet");

                result.add(new TurnHistoryData(
                        turnNum, turnStartZ, turnEndZ,
                        totalSales, totalItems, totalRooms,
                        totalRefunds, totalSpending, totalTurnVal,
                        totalBankTransfers, totalDeposits, totalNet,
                        turnStartZ.format(dateFormatter), duration,
                        turnStartZ.format(formatter), turnEndZ.format(formatter),
                        historyTurn.getActivityDataList()
                ));
            } catch (Exception e) {
                System.out.println("Skipping malformed history entry at index " + i);
            }
        }
        return result;
    }
}
