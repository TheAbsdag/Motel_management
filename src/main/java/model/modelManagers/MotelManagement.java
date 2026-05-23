package model.modelManagers;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.CartItem;
import model.Item;
import model.ProgramConfig;
import model.Register;
import model.Room;
import model.RoomTime;
import model.Turn;
import org.json.JSONArray;
import org.json.JSONObject;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import model.dto.TurnActivityData;
import model.dto.TurnHistoryData;
import model.dto.TurnSummaryItemData;
import model.turn.ActivityType;
import model.turn.ExtraChangeType;
import model.turn.RoomBookingActivity;
import model.turn.SaleActivity;
import model.turn.TurnActivity;
import model.turn.TurnDetails;
import view.helpers.TimeFormatter;

/**
 * Central model facade that coordinates sub-models, services, persistence, and printing.
 *
 * <p>Domain logic extracted into services:
 * <ul>
 *   <li>{@link SellingService} — inventory and selling-cart operations</li>
 *   <li>{@link TurnService} — turn lifecycle, reporting, and reversals</li>
 *   <li>{@link HistoryService} — historical turn browsing and DTOs</li>
 * </ul>
 *
 * <p>The facade retains cross-cutting coordination (room booking, sale completion,
 * refunds), time management, overtime tracking, printer selection, and persistence.
 *
 * @author Santiago
 */
public class MotelManagement implements ISellingService, IHistoryService {

    private final FileManager files;
    private final RoomManager roomManager;
    private final ProgramConfig programConfig;
    private final Printer printer;
    private final Register register;
    private final Turn turn;
    private final SellingService sellingService;
    private final TurnService turnService;
    private final HistoryService historyService;
    private Instant currentTime;
    private ZonedDateTime localizedTime;
    private final ZoneId zoneID;
    private List<String> overtimeList;

    private static final Logger logger = Logger.getLogger(MotelManagement.class.getName());

    public MotelManagement() {
        files = new FileManager();
        zoneID = ZoneId.of("America/Bogota");
        currentTime = Instant.now();
        localizedTime = currentTime.atZone(zoneID);
        turn = new Turn(currentTime, zoneID);
        register = new Register();
        roomManager = new RoomManager(zoneID);
        programConfig = new ProgramConfig();
        printer = new Printer();
        overtimeList = new CopyOnWriteArrayList<>();

        sellingService = new SellingService(register);
        turnService = new TurnService(turn, programConfig, printer, files);
        historyService = new HistoryService(files, zoneID);
    }

    // ========== Service Accessors ==========

    public SellingService getSellingService() { return sellingService; }
    public TurnService getTurnService() { return turnService; }
    public HistoryService getHistoryService() { return historyService; }
    public ProgramConfig getProgramConfig() { return programConfig; }
    public RoomManager getRoomManager() { return roomManager; }

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
                logger.log(Level.INFO, "Found previous turn, but it's no longer active, backing up");
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
        return TimeFormatter.formatTime(localizedTime);
    }

    public String getCurrentLocalizedDate() {
        return TimeFormatter.formatDate(localizedTime);
    }

    // ========== Room Operations (delegated to RoomManager) ==========

    public int[][] getRoomsArray() {
        return roomManager.getRoomsArray();
    }

    public Room getRoom(int tower, int floor, int room) {
        return roomManager.getRoom(tower, floor, room);
    }

    public void registerRoomTimeAdded(int tower, int floor, int room, int service, long price, boolean print) {
        timeInformationUpdate();
        turnService.addConsecutiveTransaction();
        int currentExtension = roomManager.registerRoomTimeAdded(tower, floor, room, service, currentTime);
        RoomBookingActivity roomChange = turn.registerRoomChange(
                roomManager.getRoom(tower, floor, room), currentTime, price, currentExtension,
                programConfig.getConsecutiveTransaction());
        printer.printRoomTimeSell(roomChange, programConfig.getConsecutiveTransaction(), !print);
    }

    public void registerRoomTimeEnd(int tower, int floor, int room) {
        timeInformationUpdate();
        roomManager.registerRoomTimeEnd(tower, floor, room, currentTime);
        turn.registerRoomChange(roomManager.getRoom(tower, floor, room), currentTime, 0, 0, 0);
    }

    public boolean changeRoomTimeToAnother() {
        timeInformationUpdate();
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

    // ========== Turn Operations (delegated to TurnService) ==========

    public void setNewTurn(int turnNumber) {
        turnService.setNewTurn(turnNumber, currentTime);
    }

    public void turnEnded() {
        turnService.turnEnded(currentTime);
    }

    public void turnPrintNoEnd(int option) {
        turnService.turnPrintNoEnd(option);
    }

    public void turnEndPrint(int option) {
        turnService.turnEndPrint(option, localizedTime);
    }

    public long getTurnNumber() {
        return turnService.getTurnNumber();
    }

    // ========== Inventory / Selling Operations (delegated to SellingService) ==========

    public void restartSaleManager() {
        sellingService.restartSaleManager();
    }

    public JSONObject getInventoryData() {
        return sellingService.getInventoryData();
    }

    public boolean saveItemInformation(InventoryItemData item) {
        return sellingService.saveItemInformation(item);
    }

    public void newItemCreated(String name, long price, long quantity) {
        sellingService.newItemCreated(name, price, quantity);
    }

    public void deleteItemFromInventory(long itemID) {
        sellingService.deleteItemFromInventory(itemID);
    }

    public void addItemToSelling(long itemID, long quantity, boolean courtesySale) {
        sellingService.addItemToSelling(itemID, quantity, courtesySale);
    }

    public void removeItemToSelling(long itemID) {
        sellingService.removeItemToSelling(itemID);
    }

    public long getCurrentTotalPriceSellingList() {
        return sellingService.getCurrentTotalPriceSellingList();
    }

    public void roomSaleFinished(boolean print) {
        timeInformationUpdate();
        turnService.addConsecutiveTransaction();
        Room roomSoldTo = roomManager.getRoomForSale();
        List<CartItem> sellingItems;
        try {
            sellingItems = sellingService.consumeRegisterListForSale();
        } catch (IllegalStateException e) {
            logger.log(Level.WARNING, "Selling list already consumed", e);
            return;
        }
        SaleActivity transaction = turnService.saveTransactionInformation(
                sellingItems, roomSoldTo, currentTime, programConfig.getConsecutiveTransaction());
        printer.printItemSold(transaction, programConfig.getConsecutiveTransaction(), !print);
    }

    public void revertItemSale(TurnActivityData activity) {
        long itemID = activity.getItemID();
        long quantity = activity.getQuantity();
        Item currentItem = sellingService.getItemFromItemID(itemID);
        if (currentItem != null) {
            currentItem.itemAdded(quantity);
        }
        TurnActivity turnActivity = turnService.findActivity(activity.getConsecutiveTrans(), ActivityType.SALE);
        if (turnActivity != null) {
            turnService.reverseItemSaleFromTurn(turnActivity, itemID, quantity);
        }
    }

    // ========== Spending / Extra Changes / Refunds ==========

    public void addSpendingTransaction(String conceptSpending, long value) {
        timeInformationUpdate();
        turnService.addConsecutiveTransaction();
        turnService.addSpendingTransaction(conceptSpending, value,
                programConfig.getConsecutiveTransaction(), currentTime);
    }

    public void addExtraChangeTransaction(String description, long value, ExtraChangeType changeType) {
        timeInformationUpdate();
        turnService.addConsecutiveTransaction();
        turnService.addExtraChangeTransaction(description, value, changeType,
                programConfig.getConsecutiveTransaction(), currentTime);
    }

    public void refundItemSale(int consecutiveTrans, ActivityType changeType, long itemID, long itemQty) {
        timeInformationUpdate();
        turnService.addConsecutiveTransaction();
        TurnActivity activity = turnService.findActivity(consecutiveTrans, changeType);
        if (activity == null) return;
        if (changeType == ActivityType.SALE && itemID > 0) {
            Item currentItem = sellingService.getItemFromItemID(itemID);
            if (currentItem != null) {
                currentItem.itemAdded(itemQty);
            }
        }
        turnService.refundTransactionFromTurn(activity,
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
        programConfig.ensureSchemaVersion();
        populateConfigTimeData();

        JSONObject roomData = new JSONObject();
        roomData.put("rooms", roomManager.getRoomDataForSaving());
        roomData.put("version", 1);

        Map<String, JSONObject> dataMap = new LinkedHashMap<>();
        dataMap.put("turn", turnService.getDetailedTurnInformationAsJson());
        dataMap.put("inventory", sellingService.getInventoryData());
        dataMap.put("roomsInformation", roomData);
        dataMap.put("applicationProperties", programConfig.getProgramData());

        files.saveAllMainDataAtomic(dataMap);
    }

    public void saveFilesForBackup(String saveType) {
        timeInformationUpdate();

        files.saveJsonBackupDataPath(turnService.getDetailedTurnInformationAsJson(), "turn", localizedTime, saveType);
        files.saveJsonBackupDataPath(sellingService.getInventoryData(), "inventory", localizedTime, saveType);

        JSONObject roomData = new JSONObject();
        roomData.put("rooms", roomManager.getRoomDataForSaving());
        roomData.put("version", 1);
        files.saveJsonBackupDataPath(roomData, "roomsInformation", localizedTime, saveType);
        files.saveJsonBackupDataPath(programConfig.getProgramData(), "applicationProperties", localizedTime, saveType);
    }

    // ========== History Operations (delegated to HistoryService) ==========

    public JSONArray getHistoryData() {
        return historyService.getHistoryData();
    }

    public void generateHistoryTurnReport(int selectedRow) {
        historyService.generateHistoryTurnReport(selectedRow);
    }

    public List<TurnActivityData> getTurnActivityDataList() {
        return turnService.getTurnActivityDataList();
    }

    public TurnDetails getCurrentTurnDetailedInfo() {
        return turnService.getCurrentTurnDetailedInfo();
    }

    public List<TurnSummaryItemData> getTurnSummaryDataList() {
        return turnService.getTurnSummaryDataList();
    }

    public List<TurnHistoryData> getTurnHistoryDataList() {
        return historyService.getTurnHistoryDataList();
    }

    public void turnHistoryPrint(int option, int selectedRow) {
        TurnDetails details = historyService.getHistoryTurn(selectedRow).getDetailedTurnInformation();
        turnService.turnHistoryPrint(option, details);
    }

    // ========== DTO Access Methods ==========

    public List<InventoryItemData> getInventoryItemDataList() {
        return sellingService.getInventoryItemDataList();
    }

    public List<SellingItemData> getSellingItemDataList() {
        return sellingService.getSellingItemDataList();
    }

    // ========== Configuration Delegation ==========

    /**
     * Updates motel identification data (name, NIT, address) in
     * ProgramConfig, the Printer, and persists to disk immediately.
     */
    public void saveMotelDataConfiguration(String name, String address, String id) {
        programConfig.setMotelName(name);
        programConfig.setMotelAddress(address);
        programConfig.setMotelID(id);
        printer.setPrinterVariables(name, address, id);
        files.saveJsonMainDataPath(programConfig.getProgramData(), "applicationProperties");
    }

    public void rebuildRoomGridFromConfig() {
        roomManager.rebuildRoomGrid(programConfig.getProgramData());
    }

    /**
     * Ensures all rooms in ProgramConfig have customTimeData populated
     * from the runtime Room objects. Used before saving to migrate old
     * schemas and keep config in sync with runtime changes.
     */
    private void populateConfigTimeData() {
        JSONArray roomsPerTower = programConfig.getRoomsPerTower();
        if (roomsPerTower == null) return;

        ArrayList<ArrayList<ArrayList<Room>>> rooms = roomManager.getRooms();
        for (int t = 0; t < roomsPerTower.length() && t < rooms.size(); t++) {
            JSONObject tower = roomsPerTower.getJSONObject(t);
            JSONArray towerRooms = tower.getJSONArray("towerRooms");
            for (int fd = 0; fd < towerRooms.length(); fd++) {
                JSONObject floorData = towerRooms.getJSONObject(fd);
                int floorNum = floorData.getInt("floor");
                if (floorNum >= rooms.get(t).size()) continue;
                JSONArray configRooms = floorData.getJSONArray("rooms");
                ArrayList<Room> runtimeRooms = rooms.get(t).get(floorNum);
                for (int r = 0; r < configRooms.length() && r < runtimeRooms.size(); r++) {
                    JSONObject roomJson = configRooms.getJSONObject(r);
                    if (roomJson.has("customTimeData")) continue;
                    RoomTime[] timeData = runtimeRooms.get(r).getCustomRoomTimeData();
                    JSONArray arr = new JSONArray();
                    for (RoomTime rt : timeData) {
                        JSONObject td = new JSONObject();
                        td.put("price", rt.getPrice());
                        td.put("timeSeconds", rt.getTimeSeconds());
                        arr.put(td);
                    }
                    roomJson.put("customTimeData", arr);
                }
            }
        }
    }

    /**
     * Reloads ProgramConfig from disk and rebuilds the room grid, effectively
     * reverting any unsaved in-memory config changes.
     */
    public void revertToSavedConfig() {
        JSONObject rawConfig = files.getJsonData("applicationProperties");
        programConfig.loadFromJson(rawConfig);
        roomManager.rebuildRoomGrid(programConfig.getProgramData());
    }
}
