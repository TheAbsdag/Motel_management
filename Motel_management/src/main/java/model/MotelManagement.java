package model;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Santiago
 */
public class MotelManagement {

    private FileManager files;
    private ArrayList<ArrayList<Room>> rooms;
    private Printer printer;
    private Register register;
    private Turn turn;
    private int currentFloor;
    private Instant currentTime;
    private ZonedDateTime localizedTime;
    private ZoneId zoneID;
    private Room reception;

    public MotelManagement() {
        files = new FileManager();
        zoneID = ZoneId.of("America/Bogota");
        rooms = new ArrayList<ArrayList<Room>>();
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
        for (int i = 0; i < floors; i++) {
            rooms.add(new ArrayList<Room>());
            int roomsInFloor = programData.getInt("roomsFloor" + i);
            for (int j = 0; j < roomsInFloor; j++) {
                String formatedRoomString = (i + 1) + String.format("%02d", (j + 1));
                Room currentRoom = new Room(formatedRoomString, i, j);
                rooms.get(i).add(currentRoom);
            }
        }
    }

    public void prepareTurnRegisterData() {
        JSONObject turnData = files.getJsonData("turn");
        JSONObject inventoryData = files.getJsonData("inventory");
        //Validation if the file is valid or not
        if (!(turnData == null || turnData.isEmpty())) {
            this.turn.setPreviousTurnJSON(turnData);
        }
        if (!(inventoryData== null|| inventoryData.isEmpty())) {
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

    }

    public void timeInformationUpdate() {
        currentTime = Instant.now();
        localizedTime = currentTime.atZone(zoneID);
    }

    public String getCurrentLocalizedTime() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("hh:mm:ss a")
            .toFormatter();

        // Format the ZonedDateTime to the desired format
        return localizedTime.format(formatter);
    }

    public int[] getRoomsArray() {
        int arr[] = new int[rooms.size()];
        for(int i=0;i<arr.length;i++){
            arr[i] = rooms.get(i).size();
        }
        return arr;
    }

}
