package model;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
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

    public MotelManagement() {
        files = new FileManager();
        zoneID = ZoneId.of("America/Bogota");
        rooms = new ArrayList<>();
        currentTime = Instant.now();
        localizedTime = currentTime.atZone(zoneID);
        turn = new Turn(currentTime, zoneID);
        register = new Register();
        printer = new Printer();
        reception = new Room("Reception", -1, -1);
    }

    public void prepareProgramData() {
        JSONObject programData = files.getJsonData("applicationProperties");
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
                    case 1 ->
                        rooms.get(floor).get(roomNum).setRoomStatus(status);
                    case 2 ->
                        rooms.get(floor).get(roomNum).setRoomStatus(status, ZonedDateTime.parse(room.getString("startStatus")).toInstant());
                    case 3 ->
                        rooms.get(floor).get(roomNum).setRoomStatus(status, ZonedDateTime.parse(room.getString("startStatus")).toInstant(), room.getInt("service"));
                }
            }
        }

    }
    
    public void setNewTurn(int turn){
        this.turn.setNewTurn(turn, currentTime);
    }
    
    public void registerRoomTimeAdded(int floor, int room, int service, long price){
        int currentStatus = rooms.get(floor).get(room).getStatus();
        //A time increase was done if the room is currently booked
        if(currentStatus ==3){
            rooms.get(floor).get(room).extendRoomTime(service);
            turn.registerRoomChange(rooms.get(floor).get(room), currentTime, price);
        }
        else{
            rooms.get(floor).get(room).setRoomStatus(3, currentTime, service);
            turn.registerRoomChange(rooms.get(floor).get(room), currentTime, price);
        }
    }
    
    public void registerRoomTimeEnd(int floor, int room){
        int currentStatus = rooms.get(floor).get(room).getStatus();
        //If it was being cleaned it will set it up as free
        if(currentStatus ==2){
            rooms.get(floor).get(room).setRoomStatus(1);
        }else{
            //Otherwise it will be setup as cleaning
            rooms.get(floor).get(room).setRoomStatus(2, currentTime);
        }
    }
    

    public boolean prepareTurnRegisterData() {
        JSONObject turnData = files.getJsonData("turn");
        JSONObject inventoryData = files.getJsonData("inventory");
        boolean validPreviousTurn = false;
        //Validation if the file is valid or not, if it'snot, a previous turn was not found.
        if (!(turnData == null || turnData.isEmpty())) {
            this.turn.setPreviousTurnJSON(turnData);
            validPreviousTurn = true;
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

    public void timeInformationUpdate() {
        currentTime = Instant.now();
        localizedTime = currentTime.atZone(zoneID);
    }

    public String getCurrentLocalizedTime() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("hh:mm:ss").appendLiteral(' ')
                .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "\tAM", 1L, "\tPM"))
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
    
    public Room getRoom(int floor, int room){
        if(floor <0 || room <0){
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

    public void setCurrentFloorRoom(int floor, int room){
        this.currentFloorViewed = floor;
        this.currentRoomViewed = room;
    }

    public String getRemainingTimeRoom(int floor, int room) {
        String output = new String();
        Duration duration = Duration.between(currentTime, getRoom(floor, room).getEndStatus());
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        output = String.valueOf(hours+":"+minutes);
        return output;
    }
    
    public String getStartTimeRoom(int floor, int room) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("hh:mm:ss").appendLiteral(' ')
                .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "\tAM", 1L, "\tPM"))
                .toFormatter();
        String output = new String();
        ZonedDateTime start = getRoom(floor, room).getStartStatus().atZone(zoneID);
        output = start.format(formatter);
        return output;
    }
    
    public String getStartDateRoom(int floor, int room){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", new Locale("es", "ES"));
        String output = new String();
        ZonedDateTime start = getRoom(floor, room).getStartStatus().atZone(zoneID);
        output = start.format(formatter);
        return output;
    }

    public void startSaleManager() {
        register.newSellingList();
    }

    public void stopSaleManager() {
        register.newSellingList();
    }

    public JSONObject getInventoryData(){
        return register.getInventoryData();
    }
    public void  saveItemInformation(JSONObject itemInformation){
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
}
