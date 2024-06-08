package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Santiago
 */
public class Turn {

    private Instant start;
    private Instant end;
    private int turnNumber;
    private JSONArray turnHistory;
    private JSONObject turnDetails;
    private ZoneId zoneID;
    private boolean isTurnActive;

    public Turn(Instant start, ZoneId zoneID) {
        this.start = start;
        this.turnHistory = new JSONArray();
        this.turnNumber = 1;
        turnDetails = new JSONObject();
        this.zoneID = zoneID;
        isTurnActive = false;
    }

    //Specific turn information for history purposes.
    public Turn(Instant start, Instant end, int turnID, ZoneId zoneID) {
        this.start = start;
        this.end = end;
        this.turnHistory = new JSONArray();
        this.turnNumber = 1;
        this.turnNumber = turnID;
        turnDetails = new JSONObject();
        this.zoneID = zoneID;
        isTurnActive = false;
        
        String dateLocalized = this.start.atZone(zoneID).toString();
        turnDetails.put("turnNumber", this.turnNumber);
        turnDetails.put("turnStart", dateLocalized);
        dateLocalized = this.end.atZone(zoneID).toString();
        turnDetails.put("turnEnd", dateLocalized);
    }

    public void registerRoomChange(Room room, Instant time, int price) {
        //Setting temporary information for the change.
        String dateLocalized = time.atZone(zoneID).toString();
        JSONObject change = new JSONObject();
        int roomStatus = room.getStatus();

        change.put("changeDate", dateLocalized);
        change.put("changeType", "room");
        change.put("roomString", room.getRoomString());
        change.put("roomNumber", room.getRoomNumber());
        change.put("floorNumber", room.getFloorNumber());
        change.put("roomStatus", room.getStatus());
        //Validation of the time
        dateLocalized = room.getStartStatus().atZone(zoneID).toString();
        change.put("startStatus", dateLocalized);
        switch (roomStatus) {
            case 3:
                change.put("changeDate", dateLocalized);
                change.put("changeType", "room");
                change.put("roomString", room.getRoomString());
                change.put("roomNumber", room.getRoomNumber());
                change.put("floorNumber", room.getFloorNumber());
                change.put("roomStatus", room.getStatus());

                dateLocalized = room.getStartStatus().atZone(zoneID).toString();
                change.put("startStatus", dateLocalized);
                dateLocalized = room.getEndStatus().atZone(zoneID).toString();
                //Validation of the time
                change.put("endStatus", dateLocalized);
                change.put("price", price);
                change.put("service", room.getService());
                change.put("extension", room.getExtension());
                break;
        }
        turnHistory.put(change);
    }
    
    public void saveTransactionInformation (JSONArray registerJson, Room roomSoldTo, Instant time){
        JSONObject change = new JSONObject();
        String dateLocalized = time.atZone(zoneID).toString();
        change.put("changeDate", dateLocalized);
        change.put("changeType", "sale");
        change.put("roomSoldTo", roomSoldTo.getRoomString());
        change.put("register",registerJson);
        turnHistory.put(change);
    }
    
    public void setNewTurn(int turnID, Instant start){
        this.turnNumber = turnID;
        this.start = start;
        String dateLocalized = start.atZone(zoneID).toString();
        turnHistory.clear();
        turnDetails.clear();
        turnDetails.put("turnNumber", turnNumber);
        turnDetails.put("turnStart", dateLocalized);
        isTurnActive = true;
        turnDetails.put("isTurnActive", isTurnActive);
    }
    
    //TurnEnd must be called before any turn information is requested
    public void turnEnd(Instant end){
        this.end = end;
        String dateLocalized = this.end.atZone(zoneID).toString();
        turnDetails.put("turnEnd", dateLocalized);
        turnDetails.put("turnActivity", turnHistory);
        isTurnActive = false;
                turnDetails.put("isTurnActive", isTurnActive);

    }
    
    public JSONObject getBasicTurnInformation(){
        JSONObject basicTurn = new JSONObject();
        basicTurn.put("turnStart", turnDetails.getString("turnStart"));
        basicTurn.put("turnNumber", turnDetails.getInt("turnNumber"));
        basicTurn.put("turnEnd", turnDetails.getString("turnEnd"));
        
        JSONArray turnHistorySummary = new JSONArray();
        
        //Going through each transaction made within it.
        for(int i =0; i< turnHistory.length();i++){
            JSONObject change = turnHistory.getJSONObject(i);
            String changeType = change.getString("changeType");
            //Validation for the room summary
            if(changeType.equals("room")){
                int status = change.getInt("status");
                //A room was booked on the turn, so we capture the data for the summary
                if(status ==3){
                    int price = change.getInt("price");
                    int service = change.getInt("service");
                    boolean roomServiceExisting = false;
                    //Going through the curren turnHistorySummary to find if the key exists
                    
                    for(int j = 0;j<turnHistorySummary.length();j++){
                        JSONObject summaryItem = turnHistorySummary.getJSONObject(j);
                        if(summaryItem.getInt("roomService") == service){
                            //It exists, we update values, and we exit the loop
                            roomServiceExisting = true;
                            summaryItem.put("quantity", summaryItem.getInt("quantity") + 1);
                            summaryItem.put("price", summaryItem.getInt("price") + price);
                            turnHistorySummary.remove(j);
                            turnHistorySummary.put(summaryItem);
                            break;
                        }
                    }
                    
                    //If the roomService was never found, we just create a new one
                    if(!roomServiceExisting){
                        JSONObject newSummaryItem = new JSONObject();
                        newSummaryItem.put("service", service);
                        newSummaryItem.put("quantity", 1);
                        newSummaryItem.put("price", price);
                        turnHistorySummary.put(newSummaryItem);
                    }
                }
            }
            
            //Validation for the items summary
        }
        return basicTurn;
    }
    
    public JSONObject getDetailedTurnInformation(){
        return turnDetails;
    }
    
    public void setPreviousTurnJSON(JSONObject previousTurn){
        turnHistory.clear();
        turnDetails.clear();
        start = ZonedDateTime.parse(previousTurn.getString("turnStart")).toInstant();
        turnNumber = previousTurn.getInt("turnNumber");
        String dateLocalized = start.atZone(zoneID).toString();
        turnDetails.put("turnNumber", turnNumber);
        turnDetails.put("turnStart", dateLocalized);
        
        isTurnActive = previousTurn.getBoolean("isTurnActive");
        turnDetails.put("isTurnActive", isTurnActive);

        turnHistory = previousTurn.getJSONArray("turnActivity");
    }
}
