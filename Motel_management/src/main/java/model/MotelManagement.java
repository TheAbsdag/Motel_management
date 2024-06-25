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
    private final ArrayList<ArrayList<Room>> rooms;
    private final Printer printer;
    private final Register register;
    private final Turn turn;
    private int currentFloorViewed;
    private int currentRoomViewed;
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

    private int consecutiveTransaction;

    public MotelManagement() {
        files = new FileManager();
        zoneID = ZoneId.of("America/Bogota");
        rooms = new ArrayList<>();
        currentTime = Instant.now();
        localizedTime = currentTime.atZone(zoneID);
        turn = new Turn(currentTime, zoneID);
        register = new Register();
        printer = new Printer();
        reception = new Room("Recepcion", -1, -1);
        consecutiveTransaction = 0;
        programData = new JSONObject();
        turnHistory = new ArrayList<>();
    }

    public void prepareProgramData() {
        programData = files.getJsonData("applicationProperties");
        int floors = programData.getInt("floors");
        //creation for the rooms of each floor
        for (int i = 0; i < floors; i++) {
            rooms.add(new ArrayList<>());
            int roomsInFloor = programData.getInt("roomsFloor" + i);
            for (int j = 0; j < roomsInFloor; j++) {
                String formatedRoomString = (i + 1) + String.format("%02d", (j + 1));
                Room currentRoom = new Room(formatedRoomString, i, j);
                rooms.get(i).add(currentRoom);
            }
        }
        consecutiveTransaction = programData.getInt("consecutiveTransaction");
        /*
        For the format of the room data, it would be as follows:
        {
        rooms:[
                {
        "roomString": "101",
        "floorNumber": 0,
        "roomNumber": 0
        "status": 3
        "startStatus":ZonedDateTime String,
        "service": 12,
        "endStatus": ZonedDateTime String (with the duration of the service amount"
        
                }
            ]
        }
         */
        JSONObject roomData = files.getJsonData("roomsInformation");
        if (!(roomData == null || roomData.isEmpty())) {
            JSONArray roomsArray = roomData.getJSONArray("rooms");
            for (int i = 0; i < roomsArray.length(); i++) {
                JSONObject room = roomsArray.getJSONObject(i);
                int floor = room.getInt("floorNumber");
                int roomNum = room.getInt("roomNumber");
                int status = room.getInt("status");
                switch (status) {
                    case 1:
                        rooms.get(floor).get(roomNum).setRoomStatus(status);
                        break;
                    case 2:
                        rooms.get(floor).get(roomNum).setRoomStatus(status, ZonedDateTime.parse(room.getString("startStatus")).toInstant());
                        break;
                    case 3:
                        rooms.get(floor).get(roomNum).setRoomStatus(status, ZonedDateTime.parse(room.getString("startStatus")).toInstant(), room.getInt("service"));
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid status: " + status);
                }
                int extension = room.getInt("extension");
                if (extension != 0) {
                    rooms.get(floor).get(roomNum).extendRoomTime(extension);
                }
            }
        }

    }

    public void setNewTurn(int turn) {
        this.turn.setNewTurn(turn, currentTime);
    }

    //TODO add boolean for printing logic
    public void registerRoomTimeAdded(int floor, int room, int service, long price, boolean print) {
        addConsecutiveTransaction();
        int currentStatus = rooms.get(floor).get(room).getStatus();
        int currentExtension = 0;
        //A time increase was done if the room is currently booked
        if (currentStatus == 3) {
            rooms.get(floor).get(room).extendRoomTime(service);
            currentExtension = service;
        } else {
            rooms.get(floor).get(room).setRoomStatus(3, currentTime, service);
        }
        JSONObject roomChange = turn.registerRoomChange(rooms.get(floor).get(room), currentTime, price, currentExtension);
        if (print) {
            printer.printRoomTimeSell(roomChange, consecutiveTransaction, false);
        } else {
            printer.printRoomTimeSell(roomChange, consecutiveTransaction, true);
        }
    }

    public void registerRoomTimeEnd(int floor, int room) {
        int currentStatus = rooms.get(floor).get(room).getStatus();
        //If it was being cleaned it will set it up as free
        if (currentStatus == 2) {
            rooms.get(floor).get(room).setRoomStatus(1);
        } else {
            //Otherwise it will be setup as cleaning
            rooms.get(floor).get(room).setRoomStatus(2, currentTime);
        }
        turn.registerRoomChange(rooms.get(floor).get(room), currentTime, 0, 0);
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

        //Creation for room data
        JSONObject roomData = new JSONObject();
        JSONArray roomDataArray = new JSONArray();
        for (int floor = 0; floor < rooms.size(); floor++) {
            for (int room = 0; room < rooms.get(floor).size(); room++) {
                JSONObject currentRoom = new JSONObject();
                currentRoom.put("roomString", rooms.get(floor).get(room).getRoomString());
                currentRoom.put("floorNumber", rooms.get(floor).get(room).getFloorNumber());
                currentRoom.put("roomNumber", rooms.get(floor).get(room).getRoomNumber());
                currentRoom.put("status", rooms.get(floor).get(room).getStatus());
                currentRoom.put("service", rooms.get(floor).get(room).getService());
                Instant startStatus = rooms.get(floor).get(room).getStartStatus();

                if (startStatus == null) {
                    currentRoom.put("startStatus", "");
                } else {
                    currentRoom.put("startStatus", startStatus.atZone(zoneID).toString());
                }

                Instant endStatus = rooms.get(floor).get(room).getEndStatus();

                if (endStatus == null) {
                    currentRoom.put("endStatus", "");
                } else {
                    currentRoom.put("endStatus", endStatus.atZone(zoneID).toString());
                }
                currentRoom.put("extension", rooms.get(floor).get(room).getExtension());

                roomDataArray.put(currentRoom);
            }
        }
        roomData.put("rooms", roomDataArray);
        files.saveJsonMainDataPath(roomData, "roomsInformation");
    }

    public void saveFilesForBackup() {
        JSONObject turnData = turn.getDetailedTurnInformation();
        files.saveJsonBackupDataPath(turnData, "turn");
        JSONObject inventoryData = register.getInventoryData();
        files.saveJsonBackupDataPath(inventoryData, "inventory");

        //Creation for room data
        JSONObject roomData = new JSONObject();
        JSONArray roomDataArray = new JSONArray();
        for (int floor = 0; floor < rooms.size(); floor++) {
            for (int room = 0; room < rooms.get(floor).size(); room++) {
                JSONObject currentRoom = new JSONObject();
                currentRoom.put("roomString", rooms.get(floor).get(room).getRoomString());
                currentRoom.put("floorNumber", rooms.get(floor).get(room).getFloorNumber());
                currentRoom.put("roomNumber", rooms.get(floor).get(room).getRoomNumber());
                currentRoom.put("status", rooms.get(floor).get(room).getStatus());
                currentRoom.put("service", rooms.get(floor).get(room).getService());
                Instant startStatus = rooms.get(floor).get(room).getStartStatus();

                if (startStatus == null) {
                    currentRoom.put("startStatus", "");
                } else {
                    currentRoom.put("startStatus", startStatus.atZone(zoneID).toString());
                }

                Instant endStatus = rooms.get(floor).get(room).getEndStatus();

                if (endStatus == null) {
                    currentRoom.put("endStatus", "");
                } else {
                    currentRoom.put("endStatus", endStatus.atZone(zoneID).toString());
                }
                currentRoom.put("extension", rooms.get(floor).get(room).getExtension());

                roomDataArray.put(currentRoom);
            }
        }
        roomData.put("rooms", roomDataArray);
        files.saveJsonBackupDataPath(roomData, "roomsInformation");
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

    public int[] getRoomsArray() {
        int arr[] = new int[rooms.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = rooms.get(i).size();
        }
        return arr;
    }

    public Room getRoom(int floor, int room) {
        if (floor < 0 || room < 0) {
            return reception;
        }
        return rooms.get(floor).get(room);
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

    public void setCurrentFloorRoom(int floor, int room) {
        this.currentFloorViewed = floor;
        this.currentRoomViewed = room;
    }

    public String getRemainingTimeRoom(int floor, int room) {
        String output = new String();
        Duration duration = Duration.between(currentTime, getRoom(floor, room).getEndStatus());
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        output = String.valueOf(hours + ":" + minutes);
        return output;
    }

    public String getStartTimeRoom(int floor, int room) {
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
        ZonedDateTime start = getRoom(floor, room).getStartStatus().atZone(zoneID);
        output = start.format(formatter);
        return output;
    }

    public String getStartDateRoom(int floor, int room) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", new Locale("es", "ES"));
        String output = new String();
        ZonedDateTime start = getRoom(floor, room).getStartStatus().atZone(zoneID);
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
            roomSoldTo = rooms.get(currentFloorViewed).get(currentRoomViewed);
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
    }

    public void setDesiredRoomChange(int currentFloor, int currentRoom) {
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

    public boolean changeRoomTimeToAnother() {
        boolean validReturn = false;
        Room currentRoom = rooms.get(currentFloorViewed).get(currentRoomViewed);
        Room desiredChangeRoom = rooms.get(selectedRoomChangeFloor).get(selectedRoomChangeRoom);
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
    
    public JSONObject getDetailedTurnHistoryData(int selectedRow){
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
}
