package model.modelManagers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import model.dto.TurnActivityData;
import model.dto.TurnHistoryData;
import model.dto.TurnSummaryItemData;
import model.json.ObjectMapperFactory;
import model.turn.ActivityType;
import model.turn.ExtraChangeType;
import model.turn.RoomBookingActivity;
import model.turn.SaleActivity;
import model.turn.TurnActivity;
import model.turn.TurnDetails;
import model.json.CurrencyConfig;
import model.json.FloorConfig;
import model.json.RoomConfigData;
import model.json.TimeSlotConfig;
import model.json.TowerConfig;
import view.helpers.TimeFormatter;

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

    private boolean firstBoot;

    private static final Logger logger = Logger.getLogger(MotelManagement.class.getName());

    public MotelManagement() {
        firstBoot = false;
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

    public SellingService getSellingService() { return sellingService; }
    public TurnService getTurnService() { return turnService; }
    public HistoryService getHistoryService() { return historyService; }
    public ProgramConfig getProgramConfig() { return programConfig; }
    public RoomManager getRoomManager() { return roomManager; }
    public FileManager getFileManager() { return files; }

    // ========== Initialization ==========

    public void prepareProgramData() {
        String rawConfig = files.getJsonData("applicationProperties");
        if (rawConfig == null) {
            firstBoot = true;
            return;
        }
        programConfig.loadFromJson(rawConfig);

        printer.setPrinterVariables(
                programConfig.getMotelName(),
                programConfig.getMotelAddress(),
                programConfig.getMotelID());

        String savedPrinter = programConfig.getConfiguredPrinterName();
        if (savedPrinter != null) {
            printer.setPrinterService(savedPrinter);
        }

        roomManager.buildRoomGrid(programConfig.getRoomsPerTower());
        roomManager.restoreRoomStates(files.getJsonData("roomsInformation"));
    }

    public boolean isFirstBoot() {
        return firstBoot;
    }

    public void initializeDefaultConfiguration() {
        List<TimeSlotConfig> timeData = new ArrayList<>();
        for (RoomTime rt : RoomTime.getDefaultTimeSlots()) {
            timeData.add(new TimeSlotConfig(rt.getPrice(), rt.getTimeSeconds()));
        }
        List<RoomConfigData> roomsList = new ArrayList<>();
        roomsList.add(new RoomConfigData("1-101", 0, 0, timeData));
        List<FloorConfig> towerRooms = new ArrayList<>();
        towerRooms.add(new FloorConfig(0, roomsList));
        List<TowerConfig> roomsPerTower = new ArrayList<>();
        roomsPerTower.add(new TowerConfig(1, 1, towerRooms));
        programConfig.setRoomsPerTower(roomsPerTower);
        roomManager.buildRoomGrid(programConfig.getRoomsPerTower());
    }

    public boolean prepareTurnRegisterData() {
        String turnData = files.getJsonData("turn");
        String inventoryData = files.getJsonData("inventory");
        boolean validPreviousTurn = false;
        if (turnData != null && !turnData.isEmpty()) {
            validPreviousTurn = turn.setPreviousTurnJSON(turnData);
            if (!validPreviousTurn) {
                logger.log(Level.INFO, "Found previous turn, but it's no longer active, backing up");
                this.timeInformationUpdate();
                turn.turnEnd(currentTime);
                files.saveHistoryData(turn.getDetailedTurnInformationAsJson(), "turnClosedImproperly", localizedTime);
                TurnReportGenerator.generateReport(turn.getDetailedTurnInformation());
            }
        }
        if (inventoryData != null && !inventoryData.isEmpty()) {
            try {
                JsonNode root = ObjectMapperFactory.get().readTree(inventoryData);
                JsonNode itemArray = root.get("inventoryItems");
                if (itemArray != null && itemArray.isArray()) {
                    for (JsonNode currentItem : itemArray) {
                        register.createItem(
                                currentItem.get("itemName").asText(),
                                currentItem.get("price").asLong(),
                                currentItem.get("quantity").asLong(),
                                currentItem.get("itemID").asLong());
                    }
                }
            } catch (JsonProcessingException e) {
                logger.log(Level.SEVERE, "Failed to parse inventory data", e);
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

    // ========== Room Operations ==========

    public int[][] getRoomsArray() {
        return roomManager.getRoomsArray();
    }

    public Room getRoom(int tower, int floor, int room) {
        return roomManager.getRoom(tower, floor, room);
    }

    public void registerRoomTimeAdded(int tower, int floor, int room, long serviceDuration, long price, boolean print) {
        timeInformationUpdate();
        turnService.addConsecutiveTransaction();
        long currentExtension = roomManager.registerRoomTimeAdded(tower, floor, room, serviceDuration, currentTime);
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

    // ========== Room View State ==========

    public int getCurrentFloorViewed() { return roomManager.getCurrentFloorViewed(); }
    public int getCurrentRoomViewed() { return roomManager.getCurrentRoomViewed(); }
    public int getCurrentTowerViewed() { return roomManager.getCurrentTowerViewed(); }
    public long getCurrentServiceDesired() { return roomManager.getCurrentServiceDesired(); }

    public void setCurrentServiceDesired(long service) { roomManager.setCurrentServiceDesired(service); }

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

    // ========== Inventory / Selling Operations ==========

    public void restartSaleManager() {
        sellingService.restartSaleManager();
    }

    public String getInventoryData() {
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
        files.saveJsonMainDataPath(programConfig.toJson(), "applicationProperties");
    }

    // ========== File Persistence ==========

    public void saveFilesForMainService() {
        programConfig.ensureSchemaVersion();
        populateConfigTimeData();

        Map<String, String> dataMap = new LinkedHashMap<>();
        dataMap.put("turn", turnService.getDetailedTurnInformationAsJson());
        dataMap.put("inventory", sellingService.getInventoryData());
        dataMap.put("roomsInformation", roomManager.getRoomDataForSaving());
        dataMap.put("applicationProperties", programConfig.toJson());

        files.saveAllMainDataAtomic(dataMap);
    }

    public void saveFilesForBackup(String saveType) {
        timeInformationUpdate();

        files.saveJsonBackupDataPath(turnService.getDetailedTurnInformationAsJson(), "turn", localizedTime, saveType);
        files.saveJsonBackupDataPath(sellingService.getInventoryData(), "inventory", localizedTime, saveType);
        files.saveJsonBackupDataPath(roomManager.getRoomDataForSaving(), "roomsInformation", localizedTime, saveType);
        files.saveJsonBackupDataPath(programConfig.toJson(), "applicationProperties", localizedTime, saveType);
    }

    // ========== History Operations ==========

    public String getHistoryData() {
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

    public void saveMotelDataConfiguration(String name, String address, String id) {
        programConfig.setMotelName(name);
        programConfig.setMotelAddress(address);
        programConfig.setMotelID(id);
        printer.setPrinterVariables(name, address, id);
        files.saveJsonMainDataPath(programConfig.toJson(), "applicationProperties");
    }

    public void rebuildRoomGridFromConfig() {
        roomManager.rebuildRoomGrid(programConfig.getRoomsPerTower());
    }

    private void populateConfigTimeData() {
        List<TowerConfig> roomsPerTower = programConfig.getRoomsPerTower();
        if (roomsPerTower == null) return;

        ArrayList<ArrayList<ArrayList<Room>>> rooms = roomManager.getRooms();
        for (int t = 0; t < roomsPerTower.size() && t < rooms.size(); t++) {
            TowerConfig tower = roomsPerTower.get(t);
            List<FloorConfig> towerRooms = new ArrayList<>(tower.towerRooms());
            for (int fd = 0; fd < towerRooms.size(); fd++) {
                FloorConfig floorData = towerRooms.get(fd);
                int floorNum = floorData.floor();
                if (floorNum >= rooms.get(t).size()) continue;
                List<RoomConfigData> configRooms = new ArrayList<>(floorData.rooms());
                ArrayList<Room> runtimeRooms = rooms.get(t).get(floorNum);
                boolean modified = false;
                for (int r = 0; r < configRooms.size() && r < runtimeRooms.size(); r++) {
                    RoomConfigData roomJson = configRooms.get(r);
                    if (roomJson.customTimeData() != null && !roomJson.customTimeData().isEmpty()) continue;
                    RoomTime[] timeData = runtimeRooms.get(r).getCustomRoomTimeData();
                    List<TimeSlotConfig> arr = new ArrayList<>();
                    for (RoomTime rt : timeData) {
                        arr.add(new TimeSlotConfig(rt.getPrice(), rt.getTimeSeconds()));
                    }
                    configRooms.set(r, new RoomConfigData(roomJson.roomString(), roomJson.roomFloor(), roomJson.roomNumber(), arr));
                    modified = true;
                }
                if (modified) {
                    towerRooms.set(fd, new FloorConfig(floorData.floor(), configRooms));
                }
            }
            roomsPerTower.set(t, new TowerConfig(tower.towerNumber(), tower.towerFloors(), towerRooms));
        }
    }

    public void saveCurrencyConfiguration(CurrencyConfig config) {
        programConfig.setCurrencyConfig(config);
        files.saveJsonMainDataPath(programConfig.toJson(), "applicationProperties");
    }

    public void revertToSavedConfig() {
        String rawConfig = files.getJsonData("applicationProperties");
        programConfig.loadFromJson(rawConfig);
        roomManager.rebuildRoomGrid(programConfig.getRoomsPerTower());
    }
}
