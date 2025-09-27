package model;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Santiago
 */
public class MotelManagement {

    private final FileManager files;
    //private final ArrayList<ArrayList<Room>> rooms;
    private final ArrayList<ArrayList<ArrayList<Room>>> rooms;
    private final Printer printer;
    private final Register register;
    private final Turn turn;
    private int currentFloorViewed;
    private int currentRoomViewed;
    private int currentTowerViewed;
    private int currentServiceDesired;
    private Instant currentTime;
    private ZonedDateTime localizedTime;
    private final ZoneId zoneID;
    private final Room reception;
    private JSONObject programData;
    private ArrayList<Turn> turnHistory;

    //Special for room change
    private int selectedRoomChangeRoom;
    private int selectedRoomChangeFloor;
    private int selectedRoomChangeTower;

    private int consecutiveTransaction;
    
    //Motel information
    private  String motelName;
    private  String motelAddress;
    private  String motelID;

    public MotelManagement() {
        files = new FileManager();
        zoneID = ZoneId.of("America/Bogota");
        rooms = new ArrayList<>();
        currentTime = Instant.now();
        localizedTime = currentTime.atZone(zoneID);
        turn = new Turn(currentTime, zoneID);
        register = new Register();
        reception = new Room("Recepcion", -1, -1, -1);
        consecutiveTransaction = 0;
        programData = new JSONObject();
        turnHistory = new ArrayList<>();
        printer = new Printer();
    }

    public void prepareProgramData() {       
        programData = files.getJsonData("applicationProperties");
        consecutiveTransaction = programData.getInt("consecutiveTransaction");  
        motelName = programData.getString("motelName");
        motelAddress = programData.getString("motelAddress");
        motelID = programData.getString("motelID");
        printer.setPrinterVariables(motelName, motelAddress, motelID);
        
        // Get the roomsPerTower array
        JSONArray roomsPerTower = programData.getJSONArray("roomsPerTower");

        // Initialize rooms structure: towers -> floors -> rooms
        for (int towerIndex = 0; towerIndex < roomsPerTower.length(); towerIndex++) {
            JSONObject tower = roomsPerTower.getJSONObject(towerIndex);
            int towerNumber = tower.getInt("towerNumber");
            int towerFloors = tower.getInt("towerFloors");

            // Add new tower (list of floors)
            rooms.add(new ArrayList<>());

            // Initialize floors for this tower
            for (int floorIndex = 0; floorIndex < towerFloors; floorIndex++) {
                // Add new floor (list of rooms) for this tower
                rooms.get(towerIndex).add(new ArrayList<>());
            }

            // Process the actual room data for this tower
            JSONArray towerRooms = tower.getJSONArray("towerRooms");

            // Process each floor in the tower
            for (int floorDataIndex = 0; floorDataIndex < towerRooms.length(); floorDataIndex++) {
                JSONObject floorData = towerRooms.getJSONObject(floorDataIndex);
                int floorNumber = floorData.getInt("floor");
                JSONArray roomsArray = floorData.getJSONArray("rooms");

                // Process each room on this floor
                for (int roomIndex = 0; roomIndex < roomsArray.length(); roomIndex++) {
                    JSONObject roomJson = roomsArray.getJSONObject(roomIndex);
                    String roomString = roomJson.getString("roomString");
                    int roomFloor = roomJson.getInt("roomFloor");
                    int roomNumber = roomJson.getInt("roomNumber");

                    // Create room object and add to the appropriate tower and floor
                    Room currentRoom = new Room(roomString, roomFloor, roomNumber, towerNumber);
                    rooms.get(towerIndex).get(floorNumber).add(currentRoom);
                }
            }
        }

        /*
        For the format of the room data, it would be as follows:
        {
        "rooms":[
                {
        "roomString": "1-105",
        "towerNumber" : 1,
        "floorNumber": 0,
        "roomNumber": 0,
        "status": 3,
        "startStatus": ZonedDateTime String,
        "service": 12,
        "endStatus": ZonedDateTime String (with the duration of the service amount)
                }
            ]
        }
         */
        JSONObject roomData = files.getJsonData("roomsInformation");
        if (!(roomData == null || roomData.isEmpty())) {
            JSONArray roomsArray = roomData.getJSONArray("rooms");
            for (int i = 0; i < roomsArray.length(); i++) {
                JSONObject room = roomsArray.getJSONObject(i);
                String roomString = room.getString("roomString");
                int towerNum = room.getInt("towerNumber");
                int floor = room.getInt("floorNumber");
                int roomNum = room.getInt("roomNumber");
                int status = room.getInt("status");

                // Find the room using tower -> floor -> room indices
                if (towerNum >= 0 && towerNum < rooms.size()
                        && floor >= 0 && floor < rooms.get(towerNum).size()
                        && roomNum >= 0 && roomNum < rooms.get(towerNum).get(floor).size()) {

                    Room targetRoom = rooms.get(towerNum).get(floor).get(roomNum);

                    switch (status) {
                        case 1:
                            targetRoom.setRoomStatus(status);
                            break;
                        case 2:
                            targetRoom.setRoomStatus(status, ZonedDateTime.parse(room.getString("startStatus")).toInstant());
                            break;
                        case 3:
                            targetRoom.setRoomStatus(status, ZonedDateTime.parse(room.getString("startStatus")).toInstant(), room.getInt("service"));
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid status: " + status);
                    }
                    int extension = room.getInt("extension");
                    if (extension != 0) {
                        targetRoom.extendRoomTime(extension);
                    }
                } else {
                    // Room not found, corrupted
                    System.out.println("Room not found: " + roomString + " (Tower: " + towerNum + ", Floor: " + floor + ", Room: " + roomNum + ")");
                }
            }
        }
    }

    public void setNewTurn(int turn) {
        this.turn.setNewTurn(turn, currentTime);
    }

    public void registerRoomTimeAdded(int tower, int floor, int room, int service, long price, boolean print) {
        addConsecutiveTransaction();
        int currentStatus = rooms.get(tower).get(floor).get(room).getStatus();
        int currentExtension = 0;
        //A time increase was done if the room is currently booked
        if (currentStatus == 3) {
            rooms.get(tower).get(floor).get(room).extendRoomTime(service);
            currentExtension = service;
        } else {
            rooms.get(tower).get(floor).get(room).setRoomStatus(3, currentTime, service);
        }
        JSONObject roomChange = turn.registerRoomChange(rooms.get(tower).get(floor).get(room), currentTime, price, currentExtension);
        if (print) {
            printer.printRoomTimeSell(roomChange, consecutiveTransaction, false);
        } else {
            printer.printRoomTimeSell(roomChange, consecutiveTransaction, true);
        }
    }

    public void registerRoomTimeEnd(int tower, int floor, int room) {
        int currentStatus = rooms.get(tower).get(floor).get(room).getStatus();
        //If it was being cleaned it will set it up as free
        if (currentStatus == 2) {
            rooms.get(tower).get(floor).get(room).setRoomStatus(1);
        } else {
            //Otherwise it will be setup as cleaning
            rooms.get(tower).get(floor).get(room).setRoomStatus(2, currentTime);
        }
        turn.registerRoomChange(rooms.get(tower).get(floor).get(room), currentTime, 0, 0);
    }

    public boolean prepareTurnRegisterData() {
        JSONObject turnData = files.getJsonData("turn");
        JSONObject inventoryData = files.getJsonData("inventory");
        boolean validPreviousTurn = false;
        //Validation if the file is valid or not, if it'snot, a previous turn was not found.
        if (!(turnData == null || turnData.isEmpty())) {
            validPreviousTurn = turn.setPreviousTurnJSON(turnData);
            if (!validPreviousTurn) {
                System.out.println("Found previosu turn, but it's no longer active, backuing up");
                this.timeInformationUpdate();
                turn.turnEnd(currentTime);
                files.saveHistoryData(turn.getDetailedTurnInformation(), "turnClosedImproperly", localizedTime);
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

    public void saveFilesForMainService() {
        JSONObject turnData = turn.getDetailedTurnInformation();
        files.saveJsonMainDataPath(turnData, "turn");
        JSONObject inventoryData = register.getInventoryData();
        files.saveJsonMainDataPath(inventoryData, "inventory");

        // Creation for room data
        JSONObject roomData = new JSONObject();
        JSONArray roomDataArray = new JSONArray();

        // Iterate through towers -> floors -> rooms
        for (int tower = 0; tower < rooms.size(); tower++) {
            for (int floor = 0; floor < rooms.get(tower).size(); floor++) {
                for (int roomIndex = 0; roomIndex < rooms.get(tower).get(floor).size(); roomIndex++) {
                    Room currentRoomObj = rooms.get(tower).get(floor).get(roomIndex);
                    JSONObject currentRoom = new JSONObject();
                    currentRoom.put("roomString", currentRoomObj.getRoomString());
                    currentRoom.put("towerNumber", tower); // Save tower index
                    currentRoom.put("floorNumber", currentRoomObj.getFloorNumber());
                    currentRoom.put("roomNumber", currentRoomObj.getRoomNumber());
                    currentRoom.put("status", currentRoomObj.getStatus());
                    currentRoom.put("service", currentRoomObj.getService());

                    Instant startStatus = currentRoomObj.getStartStatus();
                    if (startStatus == null) {
                        currentRoom.put("startStatus", "");
                    } else {
                        currentRoom.put("startStatus", startStatus.atZone(zoneID).toString());
                    }

                    Instant endStatus = currentRoomObj.getEndStatus();
                    if (endStatus == null) {
                        currentRoom.put("endStatus", "");
                    } else {
                        currentRoom.put("endStatus", endStatus.atZone(zoneID).toString());
                    }

                    currentRoom.put("extension", currentRoomObj.getExtension());
                    roomDataArray.put(currentRoom);
                }
            }
        }

        roomData.put("rooms", roomDataArray);
        files.saveJsonMainDataPath(roomData, "roomsInformation");
    }

    public void saveFilesForBackup(String saveType) {
        timeInformationUpdate();
        JSONObject turnData = turn.getDetailedTurnInformation();
        files.saveJsonBackupDataPath(turnData, "turn", localizedTime, saveType);
        JSONObject inventoryData = register.getInventoryData();
        files.saveJsonBackupDataPath(inventoryData, "inventory", localizedTime, saveType);

        // Creation for room data
        JSONObject roomData = new JSONObject();
        JSONArray roomDataArray = new JSONArray();

        // Iterate through towers -> floors -> rooms
        for (int tower = 0; tower < rooms.size(); tower++) {
            for (int floor = 0; floor < rooms.get(tower).size(); floor++) {
                for (int roomIndex = 0; roomIndex < rooms.get(tower).get(floor).size(); roomIndex++) {
                    Room currentRoomObj = rooms.get(tower).get(floor).get(roomIndex);
                    JSONObject currentRoom = new JSONObject();
                    currentRoom.put("roomString", currentRoomObj.getRoomString());
                    currentRoom.put("towerNumber", tower); // Save tower index
                    currentRoom.put("floorNumber", currentRoomObj.getFloorNumber());
                    currentRoom.put("roomNumber", currentRoomObj.getRoomNumber());
                    currentRoom.put("status", currentRoomObj.getStatus());
                    currentRoom.put("service", currentRoomObj.getService());

                    Instant startStatus = currentRoomObj.getStartStatus();
                    if (startStatus == null) {
                        currentRoom.put("startStatus", "");
                    } else {
                        currentRoom.put("startStatus", startStatus.atZone(zoneID).toString());
                    }

                    Instant endStatus = currentRoomObj.getEndStatus();
                    if (endStatus == null) {
                        currentRoom.put("endStatus", "");
                    } else {
                        currentRoom.put("endStatus", endStatus.atZone(zoneID).toString());
                    }

                    currentRoom.put("extension", currentRoomObj.getExtension());
                    roomDataArray.put(currentRoom);
                }
            }
        }

        roomData.put("rooms", roomDataArray);
        files.saveJsonBackupDataPath(roomData, "roomsInformation", localizedTime, saveType);
        files.saveJsonBackupDataPath(programData, "applicationProperties", localizedTime, saveType);
    }

    public void timeInformationUpdate() {
        currentTime = Instant.now();
        localizedTime = currentTime.atZone(zoneID);
    }

    public String getCurrentLocalizedTime() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("hh:mm:ss").appendLiteral(' ')
                .appendText(ChronoField.AMPM_OF_DAY, new HashMap<Long, String>() {
                    {
                        put(0L, "\tAM");
                        put(1L, "\tPM");
                    }
                })
                .toFormatter();

        // Format the ZonedDateTime to the desired format
        return localizedTime.format(formatter);
    }

    public String getCurrentLocalizedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        // Format the ZonedDateTime
        return localizedTime.format(formatter);
    }

    public int[][] getRoomsArray() {
        int[][] arr = new int[rooms.size()][];
        for (int tower = 0; tower < rooms.size(); tower++) {
            arr[tower] = new int[rooms.get(tower).size()];
            for (int floor = 0; floor < rooms.get(tower).size(); floor++) {
                arr[tower][floor] = rooms.get(tower).get(floor).size();
            }
        }
        return arr;
    }

    public Room getRoom(int tower, int floor, int room) {
        if (floor < 0 || room < 0) {
            return reception;
        }
        return rooms.get(tower).get(floor).get(room);
    }

    /**
     * @return the currentFloorViewed
     */
    public int getCurrentFloorViewed() {
        return currentFloorViewed;
    }

    /**
     * @return the currentRoomViewed
     */
    public int getCurrentRoomViewed() {
        return currentRoomViewed;
    }

    /**
     * @return the currentServiceDesired
     */
    public int getCurrentServiceDesired() {
        return currentServiceDesired;
    }

    /**
     * @param currentServiceDesired the currentServiceDesired to set
     */
    public void setCurrentServiceDesired(int currentServiceDesired) {
        this.currentServiceDesired = currentServiceDesired;
    }

    public void setCurrentFloorRoom(int tower, int floor, int room) {
        this.currentFloorViewed = floor;
        this.currentRoomViewed = room;
        this.currentTowerViewed = tower;
    }

    public String getRemainingTimeRoom(int tower, int floor, int room) {
        String output = new String();
        Duration duration = Duration.between(currentTime, getRoom(tower, floor, room).getEndStatus());
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        output = String.valueOf(hours + ":" + minutes);
        return output;
    }

    public String getStartTimeRoom(int tower, int floor, int room) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("hh:mm:ss").appendLiteral(' ')
                .appendText(ChronoField.AMPM_OF_DAY, new HashMap<Long, String>() {
                    {
                        put(0L, "\tAM");
                        put(1L, "\tPM");
                    }
                })
                .toFormatter();
        String output = new String();
        ZonedDateTime start = getRoom(tower, floor, room).getStartStatus().atZone(zoneID);
        output = start.format(formatter);
        return output;
    }

    public String getStartDateRoom(int tower, int floor, int room) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", new Locale("es", "ES"));
        String output = new String();
        ZonedDateTime start = getRoom(tower, floor, room).getStartStatus().atZone(zoneID);
        output = start.format(formatter);
        return output;
    }

    public void restartSaleManager() {
        register.newSellingList();
    }

    //Related to inventory management
    public JSONObject getInventoryData() {
        return register.getInventoryData();
    }

    public void saveItemInformation(JSONObject itemInformation) {
        Item updatedItem = new Item(
                itemInformation.getString("itemName"),
                itemInformation.getLong("price"),
                itemInformation.getLong("quantity"),
                itemInformation.getLong("itemID")
        );
        register.saveItemInformation(updatedItem);
    }

    public void newItemCreated(JSONObject itemInformation) {
        register.createNewItem(
                itemInformation.getString("itemName"),
                itemInformation.getLong("price"),
                itemInformation.getLong("quantity"));
    }

    public void deleteItemFromInventory(JSONObject selectedItem) {
        Item updatedItem = new Item(
                selectedItem.getString("itemName"),
                selectedItem.getLong("price"),
                selectedItem.getLong("quantity"),
                selectedItem.getLong("itemID")
        );
        register.deleteItemInformation(updatedItem);
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
        Room roomSoldTo = null;
        if (currentFloorViewed == -1) {
            roomSoldTo = reception;
        } else {
            roomSoldTo = rooms.get(currentTowerViewed).get(currentFloorViewed).get(currentRoomViewed);
        }
        JSONObject transaction = turn.saveTransactionInformation(new JSONArray(register.getRegisterListSaleMade()),
                roomSoldTo,
                currentTime, consecutiveTransaction);
        //print logic TODO
        if (print) {
            printer.printItemSold(transaction, consecutiveTransaction, false);
        } else {
            printer.printItemSold(transaction, consecutiveTransaction, true);
        }
    }

    public JSONArray getCurrentSellingList() {
        return register.getCurrentRegisterList();
    }

    public JSONObject getCurrentTurnData() {
        return turn.getDetailedTurnInformation();
    }

    private void addConsecutiveTransaction() {
        consecutiveTransaction++;
        programData.put("consecutiveTransaction", consecutiveTransaction);
        files.saveJsonMainDataPath(programData, "applicationProperties");
    }

    public void turnEnded() {
        turn.turnEnd(currentTime);
    }

    public void turnEndPrint(int option) {
        JSONObject summarizedTurn = turn.getBasicTurnInformation();
        JSONObject detailedTurn = turn.getDetailedTurnInformation();
        files.saveHistoryData(detailedTurn, "turn", localizedTime);
        switch (option) {
            case 1:
                printer.printSummarizedTurn(summarizedTurn, true);
                printer.printDetailedTurn(detailedTurn, true);
                break;
            case 2:
                printer.printSummarizedTurn(summarizedTurn, false);
                printer.printDetailedTurn(detailedTurn, true);
                break;
            case 3:
                printer.printSummarizedTurn(summarizedTurn, true);
                printer.printDetailedTurn(detailedTurn, false);
                break;
            default:
                break;
        }
        files.clearBackupFiles();
    }

    //Exclusive for turnHistory
    public void turnHistoryPrint(int option, int selectedRow) {
        JSONObject summarizedTurn = turnHistory.get(selectedRow).getBasicTurnInformation();
        JSONObject detailedTurn = turnHistory.get(selectedRow).getDetailedTurnInformation();
        switch (option) {
            case 1:
                printer.printSummarizedTurn(summarizedTurn, true);
                printer.printDetailedTurn(detailedTurn, true);
                break;
            case 2:
                printer.printSummarizedTurn(summarizedTurn, false);
                printer.printDetailedTurn(detailedTurn, true);
                break;
            case 3:
                printer.printSummarizedTurn(summarizedTurn, true);
                printer.printDetailedTurn(detailedTurn, false);
                break;
            default:
                break;
        }
    }

    public void setDesiredRoomChange(int currentTower,int currentFloor, int currentRoom) {
        selectedRoomChangeTower = currentTower;
        selectedRoomChangeFloor = currentFloor;
        selectedRoomChangeRoom = currentRoom;
    }

    /**
     * @return the selectedRoomChangeRoom
     */
    public int getSelectedRoomChangeRoom() {
        return selectedRoomChangeRoom;
    }

    /**
     * @return the selectedRoomChangeFloor
     */
    public int getSelectedRoomChangeFloor() {
        return selectedRoomChangeFloor;
    }
    
    public int getSelectedRoomChangeTower(){
        return selectedRoomChangeTower;
    }

    public boolean changeRoomTimeToAnother() {
        boolean validReturn = false;
        Room currentRoom = rooms.get(currentTowerViewed).get(currentFloorViewed).get(currentRoomViewed);
        Room desiredChangeRoom = rooms.get(selectedRoomChangeTower).get(selectedRoomChangeFloor).get(selectedRoomChangeRoom);
        if (desiredChangeRoom.getStatus() != 3) {
            validReturn = true;
            int currentService = currentRoom.getService();
            int currentTotalExtension = currentRoom.getExtension();
            Instant currentStartTime = currentRoom.getStartStatus();
            desiredChangeRoom.setRoomStatus(3, currentStartTime, currentService);
            desiredChangeRoom.setExtension(currentTotalExtension);
            currentRoom.setRoomStatus(2, currentTime);
            turn.registerRoomSwap(currentRoom, desiredChangeRoom, currentTime);
        }

        return validReturn;
    }

    public JSONArray getHistoryData() {
        JSONArray currentHistory = files.getHistoryFiles();
        //Creation for turn to base off logic for printing if required
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

    public JSONObject getBasicTurnHistoryData(int selectedRow) {
        JSONObject output = new JSONObject();
        JSONObject currentTurn = turnHistory.get(selectedRow).getDetailedTurnInformation();
        ZonedDateTime turnStart = ZonedDateTime.parse(currentTurn.getString("turnStart"));
        ZonedDateTime turnEnd = ZonedDateTime.parse(currentTurn.getString("turnEnd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        Duration durationRaw = Duration.between(turnStart, turnEnd);
        long hours = durationRaw.toHours();
        long minutes = durationRaw.minusHours(hours).toMinutes();
        String duration = String.valueOf(hours + ":" + minutes);

        String formattedStart = turnStart.format(formatter);
        String formattedEnd = turnEnd.format(formatter);
        String startDate = turnStart.format(dateFormatter);
        output.put("startDate", startDate);
        output.put("duration", duration);
        output.put("totalSales", currentTurn.getLong("totalSales"));
        output.put("totalItems", currentTurn.getLong("totalItems"));
        output.put("totalRooms", currentTurn.getLong("totalRooms"));
        output.put("startString", formattedStart);
        output.put("endString", formattedEnd);
        return output;
    }

    public JSONObject getDetailedTurnHistoryData(int selectedRow) {
        JSONObject output = new JSONObject(turnHistory.get(selectedRow).getDetailedTurnInformation().toString());

        JSONObject currentTurn = turnHistory.get(selectedRow).getDetailedTurnInformation();
        ZonedDateTime turnStart = ZonedDateTime.parse(currentTurn.getString("turnStart"));
        ZonedDateTime turnEnd = ZonedDateTime.parse(currentTurn.getString("turnEnd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        Duration durationRaw = Duration.between(turnStart, turnEnd);
        long hours = durationRaw.toHours();
        long minutes = durationRaw.minusHours(hours).toMinutes();
        String duration = String.valueOf(hours + ":" + minutes);

        String formattedStart = turnStart.format(formatter);
        String formattedEnd = turnEnd.format(formatter);
        String startDate = turnStart.format(dateFormatter);
        output.put("startDate", startDate);
        output.put("duration", duration);
        output.put("startString", formattedStart);
        output.put("endString", formattedEnd);
        return output;
    }

    public long getTurnNumber() {
        return turn.getTurnNumber();
    }

    public void revertItemSale(JSONObject selectedFilteredItem) {
        long itemID = selectedFilteredItem.getLong("itemID");
        long quantity = selectedFilteredItem.getLong("quantity");
        Item currentItem = register.getItemFromItemID(itemID);
        currentItem.itemAdded(quantity);
        turn.reverseItemSaleFromTurn(selectedFilteredItem);
    }

    public JSONObject getCurrentSummarizedTurn() {
        return turn.getBasicTurnInformation();
    }

    public List<String> getPrinterLists() {
        return printer.getPrinterServiceNameList();
    }

    public String getCurrentPrinterName() {
        return printer.getCurrentPrinterName();
    }

    public void setPrinter(String printerName) {
        printer.setPrinterService(printerName);
    }

    public int getCurrentTowerViewed() {
       return currentTowerViewed;
    }
}
