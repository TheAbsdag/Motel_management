package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import model.RoomStatus;

/**
 *
 * @author Santiago
 */
public class Turn {

    /**
     * @return the turnNumber
     */
    public long getTurnNumber() {
        return turnNumber;
    }

    private Instant start;
    private Instant end;
    private long turnNumber;
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
    public Turn(Instant start, Instant end, int turnID, ZoneId zoneID, JSONArray turnData) {
        this.start = start;
        this.end = end;
        this.turnHistory = new JSONArray();
        this.turnNumber = 1;
        this.turnNumber = turnID;
        turnDetails = new JSONObject();
        this.zoneID = zoneID;
        isTurnActive = false;
        this.turnHistory = turnData;

        String dateLocalized = this.start.atZone(zoneID).toString();
        turnDetails.put("turnNumber", this.turnNumber);
        turnDetails.put("turnStart", dateLocalized);
        dateLocalized = this.end.atZone(zoneID).toString();
        turnDetails.put("turnEnd", dateLocalized);
        turnDetails.put("turnActivity", turnData);
    }

    public JSONObject registerRoomChange(Room room, Instant time, long price, int extended, int consecutiveTransaction) {
        //Setting temporary information for the change.
        String dateLocalized = time.atZone(zoneID).toString();
        JSONObject change = new JSONObject();
        RoomStatus roomStatus = room.getStatus();

        change.put("changeDate", dateLocalized);
        change.put("changeType", "room");
        change.put("roomString", room.getRoomString());
        change.put("roomNumber", room.getRoomNumber());
        change.put("floorNumber", room.getFloorNumber());
        change.put("towerNumber", room.getTowerNumber());
        change.put("roomStatus", roomStatus.getCode());
        //Validation of the time
        dateLocalized = room.getStartStatus().atZone(zoneID).toString();
        change.put("startStatus", dateLocalized);
        if (roomStatus == RoomStatus.OCCUPIED) {
            dateLocalized = room.getEndStatus().atZone(zoneID).toString();
            change.put("endStatus", dateLocalized);
            change.put("price", price);
            change.put("service", room.getService());
            change.put("extension", room.getExtension());

            //Current extension for this change (separate from total extension).
            change.put("servicedExtension", extended);
            change.put("consecutiveTrans", consecutiveTransaction);
            change.put("refunded", false);
        }
        if (consecutiveTransaction > 0) {
            change.put("consecutiveTrans", consecutiveTransaction);
        }
        turnHistory.put(change);
        saveCurrentTurnHistory();
        return change;
    }

    public void registerRoomSwap(Room originalRoom, Room swapedRoom, Instant time) {
        JSONObject change = new JSONObject();
        String dateLocalized = time.atZone(zoneID).toString();
        change.put("changeDate", dateLocalized);
        change.put("changeType", "roomSwap");
        change.put("originalRoom", originalRoom.getRoomString());
        change.put("swapedRoom", swapedRoom.getRoomString());
        change.put("originalRoomNumber", originalRoom.getRoomNumber());
        change.put("originalFloorNumber", originalRoom.getFloorNumber());
        change.put("originalTowerNumber", originalRoom.getTowerNumber());

        change.put("swapedRoomNumber", swapedRoom.getRoomNumber());
        change.put("swapedFloorNumber", swapedRoom.getFloorNumber());
        change.put("swapedTowerNumber", swapedRoom.getTowerNumber());

        turnHistory.put(change);
        saveCurrentTurnHistory();
    }

    public JSONObject saveTransactionInformation(JSONArray registerJson, Room roomSoldTo, Instant time, int transactionNumber) {
        JSONObject change = new JSONObject();
        String dateLocalized = time.atZone(zoneID).toString();
        change.put("changeDate", dateLocalized);
        change.put("changeType", "sale");
        change.put("roomSoldTo", roomSoldTo.getRoomString());
        change.put("register", registerJson);
        change.put("consecutiveTrans", transactionNumber);
        turnHistory.put(change);
        saveCurrentTurnHistory();
        return change;
    }

    private void saveCurrentTurnHistory() {
        turnDetails.remove("turnActivity");
        turnDetails.put("turnActivity", turnHistory);
    }

    public void setNewTurn(long turnID, Instant start) {
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
    public void turnEnd(Instant end) {
        this.end = end;
        String dateLocalized = this.end.atZone(zoneID).toString();
        turnDetails.put("turnEnd", dateLocalized);
        turnDetails.put("turnActivity", turnHistory);
        isTurnActive = false;
        turnDetails.put("isTurnActive", isTurnActive);

    }

    public JSONObject getBasicTurnInformation() {
        JSONObject basicTurn = new JSONObject();
        basicTurn.put("turnStart", turnDetails.getString("turnStart"));
        basicTurn.put("turnNumber", turnDetails.getInt("turnNumber"));
        try{
            basicTurn.put("turnEnd", turnDetails.getString("turnEnd"));
        }
        catch(JSONException e){
            //This should only trigger if turnEnd does not exists
            basicTurn.put("turnEnd", "not finished");
        }

        JSONArray turnHistorySummary = new JSONArray();

        //Going through each transaction made within it.
        long totalSales = 0;
        long totalItems = 0;
        long totalRooms = 0;
        long totalRefunds = 0;
        long totalSpending = 0;
        long totalTurn = 0;
        long totalBankTransfer = 0;
        long totalSafeDeposit = 0;
        long totalNet = 0;
        for (int i = 0; i < turnHistory.length(); i++) {
            JSONObject change = turnHistory.getJSONObject(i);
            String changeType = change.getString("changeType");

            // Validation for the room summary
            if (changeType.equals("room")) {
                int statusCode = change.getInt("roomStatus");

                // A room was booked on the turn, so we capture the data for the summary
                if (statusCode == RoomStatus.OCCUPIED.getCode()) {
                    long price = change.getLong("price");
                    int service = change.getInt("service");
                    int servicedExtension = change.getInt("servicedExtension");
                    int serviceKey = servicedExtension == 0 ? service : servicedExtension;
                    totalSales += price;
                    totalRooms += price;
                    totalTurn += price;
                    totalNet += price;

                    boolean roomServiceExisting = false;

                    // Going through the current turnHistorySummary to find if the key exists
                    for (int j = 0; j < turnHistorySummary.length(); j++) {
                        JSONObject summaryItem = turnHistorySummary.getJSONObject(j);
                        if ("room".equals(summaryItem.getString("summaryType")) && summaryItem.getInt("service") == serviceKey) {
                            // It exists, we update values, and we exit the loop
                            roomServiceExisting = true;
                            summaryItem.put("quantity", summaryItem.getInt("quantity") + 1);
                            summaryItem.put("price", summaryItem.getLong("price") + price);
                            break;
                        }
                    }

                    // If the roomService was never found, we just create a new one
                    if (!roomServiceExisting) {
                        JSONObject newSummaryItem = new JSONObject();
                        newSummaryItem.put("summaryType", "room");
                        newSummaryItem.put("service", serviceKey);
                        newSummaryItem.put("quantity", 1);
                        newSummaryItem.put("price", price);
                        turnHistorySummary.put(newSummaryItem);
                    }
                }
            }

            //Validation for the items summary
            if (changeType.equals("sale")) {
                JSONArray itemList = change.getJSONArray("register");
                for (int itemSold = 0; itemSold < itemList.length(); itemSold++) {
                    JSONObject item = itemList.getJSONObject(itemSold);
                    long itemID = item.getLong("itemID");
                    long price = item.getLong("price");
                    long quantity = item.getLong("quantity");
                    totalSales += price;
                    totalItems += price;
                    totalTurn += price;
                    totalNet += price;
                    //Going through the current list to see if the item is found
                    boolean itemExists = false;
                    for (int j = 0; j < turnHistorySummary.length(); j++) {
                        JSONObject summaryItem = turnHistorySummary.getJSONObject(j);
                        if ("item".equals(summaryItem.getString("summaryType"))) {
                            //We found the item, updating quantity and price
                            if (itemID == summaryItem.getLong("itemID")) {
                                summaryItem.put("quantity", summaryItem.getLong("quantity") + quantity);
                                summaryItem.put("price", summaryItem.getLong("price") + price);

                                itemExists = true;
                                break;
                            }
                        }
                    }

                    //The item summary does not exists, creation required
                    if (!itemExists) {
                        JSONObject newSummaryItem = new JSONObject();
                        newSummaryItem.put("summaryType", "item");
                        newSummaryItem.put("itemName", item.getString("itemName"));
                        newSummaryItem.put("itemID", itemID);
                        newSummaryItem.put("quantity", quantity);
                        newSummaryItem.put("price", price);
                        turnHistorySummary.put(newSummaryItem);
                    }
                }
            }

            // Validation for refunds
            if (changeType.equals("refund")) {
                if (change.getString("refundType").equals("saleRefund")) {
                    long itemID = change.getLong("itemID");
                    long price = change.getLong("price");
                    long quantity = change.getLong("quantity");
                    totalRefunds += price;
                    totalTurn += price;
                    totalNet += price;
                    boolean itemRefundExists = false;
                    for (int j = 0; j < turnHistorySummary.length(); j++) {
                        JSONObject summaryItem = turnHistorySummary.getJSONObject(j);
                        if ("itemRefund".equals(summaryItem.getString("summaryType")) && itemID == summaryItem.getLong("itemID")) {
                            summaryItem.put("quantity", summaryItem.getLong("quantity") + quantity);
                            summaryItem.put("price", summaryItem.getLong("price") + price);
                            itemRefundExists = true;
                            break;
                        }
                    }
                    if (!itemRefundExists) {
                        JSONObject newSummaryItem = new JSONObject();
                        newSummaryItem.put("summaryType", "itemRefund");
                        newSummaryItem.put("itemName", change.getString("itemName"));
                        newSummaryItem.put("itemID", itemID);
                        newSummaryItem.put("quantity", quantity);
                        newSummaryItem.put("price", price);
                        turnHistorySummary.put(newSummaryItem);
                    }
                } else if (change.getString("refundType").equals("roomRefund")) {
                    long price = change.getLong("price");
                    int service = change.getInt("refundService");
                    totalRefunds += price;
                    totalTurn += price;
                    totalNet += price;
                    boolean roomRefundExisting = false;
                    for (int j = 0; j < turnHistorySummary.length(); j++) {
                        JSONObject summaryItem = turnHistorySummary.getJSONObject(j);
                        if ("roomRefund".equals(summaryItem.getString("summaryType")) && summaryItem.getInt("service") == service) {
                            roomRefundExisting = true;
                            summaryItem.put("quantity", summaryItem.getInt("quantity") + 1);
                            summaryItem.put("price", summaryItem.getLong("price") + price);
                            break;
                        }
                    }
                    if (!roomRefundExisting) {
                        JSONObject newSummaryItem = new JSONObject();
                        newSummaryItem.put("summaryType", "roomRefund");
                        newSummaryItem.put("service", service);
                        newSummaryItem.put("quantity", 1);
                        newSummaryItem.put("price", price);
                        turnHistorySummary.put(newSummaryItem);
                    }
                }
            }

            // Validation for spending
            if (changeType.equals("spending")) {
                long value = change.getLong("value");
                totalSpending += value;
                totalTurn += value;
                totalNet += value;
            }

            // Validation for extra changes (bank transfer / safe deposit)
            if (changeType.equals("extraChange")) {
                String extraType = change.getString("extraType");
                long value = change.getLong("value");
                if (extraType.equals("bankTransfer")) {
                    totalBankTransfer += value * -1L;
                } else if (extraType.equals("safeDeposit")) {
                    totalSafeDeposit += value * -1L;
                }
                totalNet += value;
                boolean extraChangeExisting = false;
                for (int j = 0; j < turnHistorySummary.length(); j++) {
                    JSONObject summaryItem = turnHistorySummary.getJSONObject(j);
                    if ("extraChange".equals(summaryItem.getString("summaryType")) && extraType.equals(summaryItem.getString("extraType"))) {
                        extraChangeExisting = true;
                        summaryItem.put("quantity", summaryItem.getInt("quantity") + 1);
                        summaryItem.put("price", summaryItem.getLong("price") + value);
                        break;
                    }
                }
                if (!extraChangeExisting) {
                    JSONObject newSummaryItem = new JSONObject();
                    newSummaryItem.put("summaryType", "extraChange");
                    newSummaryItem.put("extraType", extraType);
                    newSummaryItem.put("quantity", 1);
                    newSummaryItem.put("price", value);
                    turnHistorySummary.put(newSummaryItem);
                }
            }
        }
        basicTurn.put("totalItems", totalItems);
        basicTurn.put("totalSales", totalSales);
        basicTurn.put("totalRooms", totalRooms);
        basicTurn.put("totalRefunds", totalRefunds);
        basicTurn.put("totalSpending", totalSpending);
        basicTurn.put("totalTurn", totalTurn);
        basicTurn.put("totalBankTransfers", totalBankTransfer);
        basicTurn.put("totalDeposits", totalSafeDeposit);
        basicTurn.put("totalNet", totalNet);
        basicTurn.put("turnSummary", turnHistorySummary);
        return basicTurn;
    }

    public JSONObject getDetailedTurnInformation() {
        long totalSales = 0;
        long totalItems = 0;
        long totalRooms = 0;
        long totalSpending = 0;
        long totalRefunds = 0;
        long totalTurn = 0;
        long totalBankTransfers = 0;
        long totalDeposits = 0;
        long totalNet = 0;

        for (int i = 0; i < turnHistory.length(); i++) {
            JSONObject change = turnHistory.getJSONObject(i);
            String changeType = change.getString("changeType");
            if (changeType.equals("sale")) {
                JSONArray register = change.getJSONArray("register");
                for (int j = 0; j < register.length(); j++) {
                    JSONObject currentItem = register.getJSONObject(j);
                    long value = currentItem.getLong("price");
                    totalSales += value;
                    totalItems += value;
                    totalTurn += value;
                    totalNet += value;
                }
            } else if (changeType.equals("room") && change.getInt("roomStatus") == RoomStatus.OCCUPIED.getCode()) {
                long value = change.getLong("price");
                totalSales += value;
                totalRooms += value;
                totalTurn += value;
                totalNet += value;
            } else if (changeType.equals("spending")) {
                long value = change.getLong("value");
                totalSpending += value;
                totalTurn += value;
                totalNet += value;
            } else if (changeType.equals("refund")) {
                String type = change.getString("refundType");
                long value = change.getLong("price");
                totalTurn += value;
                totalNet += value;
                if (type.equals("roomRefund")) {
                    totalRefunds += value;
                } else if (type.equals("saleRefund")) {
                    totalRefunds += value;
                }
            } else if (changeType.equals("extraChange")) {
                String type = change.getString("extraType");
                long value = change.getLong("value");
                totalNet += value;
                if (type.equals("bankTransfer")) {
                    totalBankTransfers += value * -1L;
                } else if (type.equals("safeDeposit")) {
                    totalDeposits += value * -1L;
                }
            }
        }
        turnDetails.put("totalItems", totalItems);
        turnDetails.put("totalSales", totalSales);
        turnDetails.put("totalRooms", totalRooms);
        turnDetails.put("totalSpending", totalSpending);
        turnDetails.put("totalRefunds", totalRefunds);
        turnDetails.put("totalTurn", totalTurn);
        turnDetails.put("totalBankTransfers", totalBankTransfers);
        turnDetails.put("totalDeposits", totalDeposits);
        turnDetails.put("totalNet", totalNet);
        return turnDetails;
    }

    public boolean setPreviousTurnJSON(JSONObject previousTurn) {
        turnHistory.clear();
        turnDetails.clear();
        start = ZonedDateTime.parse(previousTurn.getString("turnStart")).toInstant();
        turnNumber = previousTurn.getLong("turnNumber");
        String dateLocalized = start.atZone(zoneID).toString();
        turnDetails.put("turnNumber", turnNumber);
        turnDetails.put("turnStart", dateLocalized);

        isTurnActive = previousTurn.getBoolean("isTurnActive");
        turnDetails.put("isTurnActive", isTurnActive);
        try {
            turnHistory = previousTurn.getJSONArray("turnActivity");
            saveCurrentTurnHistory();
        } catch (JSONException ex) {
            System.out.println("Previous turn found, no previous activity found");
        }
        return isTurnActive;
    }

    /**
     * Returns the turn activity log as a list of typed DTOs for view consumption.
     * Each JSONObject in the internal turnHistory is converted to a TurnActivityData.
     */
    public List<TurnActivityData> getActivityDataList() {
        List<TurnActivityData> result = new ArrayList<>();
        for (int i = 0; i < turnHistory.length(); i++) {
            JSONObject item = turnHistory.getJSONObject(i);
            String changeType = item.getString("changeType");
            ZonedDateTime changeDate = ZonedDateTime.parse(item.getString("changeDate"));
            int consecutiveTrans = item.optInt("consecutiveTrans", 0);

            try {
                if ("sale".equals(changeType)) {
                    JSONArray registerArray = item.getJSONArray("register");
                    String roomSoldTo = item.getString("roomSoldTo");
                    for (int j = 0; j < registerArray.length(); j++) {
                        JSONObject reg = registerArray.getJSONObject(j);
                        boolean refunded = reg.optBoolean("refunded", false);
                        result.add(TurnActivityData.forSale(
                                changeDate, roomSoldTo,
                                reg.getString("itemName"),
                                reg.getLong("itemID"),
                                reg.getLong("quantity"),
                                reg.getLong("price"),
                                consecutiveTrans,
                                refunded
                        ));
                    }
                } else if ("room".equals(changeType) && item.getInt("roomStatus") == RoomStatus.OCCUPIED.getCode()) {
                    boolean refunded = item.optBoolean("refunded", false);
                    result.add(TurnActivityData.forRoomBooking(
                            changeDate, item.getString("roomString"),
                            item.getInt("roomStatus"),
                            item.getLong("price"),
                            item.getInt("service"),
                            item.optInt("servicedExtension", 0),
                            consecutiveTrans,
                            refunded
                    ));
                } else if ("roomSwap".equals(changeType)) {
                    result.add(TurnActivityData.forRoomSwap(
                            changeDate,
                            item.getString("originalRoom"),
                            item.getString("swapedRoom")
                    ));
                } else if ("refund".equals(changeType)) {
                    String refundType = item.getString("refundType");
                    long price = item.getLong("price");
                    if ("saleRefund".equals(refundType)) {
                        result.add(TurnActivityData.forRefund(
                                changeDate, refundType,
                                item.getString("refundRoom"), price,
                                item.getLong("itemID"), item.getLong("quantity"),
                                item.getString("itemName"), 0
                        ));
                    } else if ("roomRefund".equals(refundType)) {
                        result.add(TurnActivityData.forRefund(
                                changeDate, refundType,
                                item.getString("refundRoom"), price,
                                0, 0, null,
                                item.getInt("refundService")
                        ));
                    }
                } else if ("spending".equals(changeType)) {
                    result.add(TurnActivityData.forSpending(
                            changeDate,
                            item.getString("spendingDescription"),
                            item.getLong("value")
                    ));
                } else if ("extraChange".equals(changeType)) {
                    result.add(TurnActivityData.forExtraChange(
                            changeDate,
                            item.getString("extraType"),
                            item.getString("extraChangeDescription"),
                            item.getLong("value")
                    ));
                }
            } catch (JSONException e) {
                System.out.println("Skipping malformed turn activity entry at index " + i);
            }
        }
        return result;
    }

    /**
     * Returns the turn summary as a list of typed DTOs.
     * Summarizes room bookings, item sales, refunds, and extra changes grouped by type.
     */
    public List<TurnSummaryItemData> getSummaryDataList() {
        List<TurnSummaryItemData> result = new ArrayList<>();
        JSONArray summaryArray = getBasicTurnInformation().optJSONArray("turnSummary");
        if (summaryArray == null) return result;
        for (int i = 0; i < summaryArray.length(); i++) {
            JSONObject obj = summaryArray.getJSONObject(i);
            String summaryType = obj.getString("summaryType");
            int quantity = obj.getInt("quantity");
            long price = obj.getLong("price");
            switch (summaryType) {
                case "room" ->
                    result.add(new TurnSummaryItemData(summaryType, quantity, price, null, obj.getInt("service")));
                case "item" ->
                    result.add(new TurnSummaryItemData(summaryType, quantity, price, obj.getString("itemName"), 0));
                case "itemRefund" ->
                    result.add(new TurnSummaryItemData(summaryType, quantity, price, obj.getString("itemName"), 0));
                case "roomRefund" ->
                    result.add(new TurnSummaryItemData(summaryType, quantity, price, null, obj.getInt("service")));
                case "extraChange" ->
                    result.add(new TurnSummaryItemData(summaryType, quantity, price, obj.getString("extraType"), 0));
            }
        }
        return result;
    }

    public void registerSpendingTransaction(String description, long value, int consecutiveTransaction, Instant time) {
        String dateLocalized = time.atZone(zoneID).toString();
        JSONObject spending = new JSONObject();
        spending.put("changeDate", dateLocalized);
        spending.put("changeType", "spending");
        spending.put("value", value);
        spending.put("consecutiveTrans", consecutiveTransaction);
        spending.put("spendingDescription", description);
        turnHistory.put(spending);
        saveCurrentTurnHistory();
    }

    public void registerExtraChangeTransaction(String description, long value, String type, int consecutiveTransaction, Instant time) {
        String dateLocalized = time.atZone(zoneID).toString();
        JSONObject extraChange = new JSONObject();
        extraChange.put("changeDate", dateLocalized);
        extraChange.put("changeType", "extraChange");
        extraChange.put("extraType", type);
        extraChange.put("value", value);
        extraChange.put("consecutiveTrans", consecutiveTransaction);
        extraChange.put("extraChangeDescription", description);
        turnHistory.put(extraChange);
        saveCurrentTurnHistory();
    }

    /**
     * Creates a refund entry in the turn history and marks the original transaction as refunded.
     * Handles both room refunds and sale refunds.
     */
    public void refundTransactionFromTurn(JSONObject selectedFilteredTransaction, int transactionNumber, Instant time) {
        String dateLocalized = time.atZone(zoneID).toString();
        String originalChange = selectedFilteredTransaction.getString("changeType");
        JSONObject refundObject = new JSONObject();
        refundObject.put("changeType", "refund");
        refundObject.put("changeDate", dateLocalized);
        refundObject.put("consecutiveTrans", transactionNumber);

        try {
            refundObject.put("refundConsecutiveTrans", selectedFilteredTransaction.get("consecutiveTrans"));
        } catch (Exception ex) {
            System.out.println("Previous format - no consecutiveTrans");
        }

        if (originalChange.equals("room")) {
            refundObject.put("refundType", "roomRefund");
            refundObject.put("refundRoom", selectedFilteredTransaction.getString("roomString"));
            refundObject.put("price", selectedFilteredTransaction.getLong("price") * -1L);
            refundObject.put("refundService", selectedFilteredTransaction.getLong("service"));

            for (int i = 0; i < turnHistory.length(); i++) {
                JSONObject transaction = turnHistory.getJSONObject(i);
                if (transaction.has("consecutiveTrans")
                        && transaction.getInt("consecutiveTrans") == selectedFilteredTransaction.getInt("consecutiveTrans")
                        && transaction.getString("changeType").equals("room")) {
                    transaction.put("refunded", true);
                    break;
                }
            }
        } else if (originalChange.equals("sale")) {
            refundObject.put("refundType", "saleRefund");
            refundObject.put("refundRoom", selectedFilteredTransaction.getString("roomSoldTo"));

            for (int i = 0; i < turnHistory.length(); i++) {
                JSONObject transaction = turnHistory.getJSONObject(i);
                if (transaction.has("consecutiveTrans")
                        && transaction.getInt("consecutiveTrans") == selectedFilteredTransaction.getInt("consecutiveTrans")
                        && transaction.getString("changeType").equals("sale")) {
                    JSONArray register = transaction.getJSONArray("register");
                    for (int j = 0; j < register.length(); j++) {
                        JSONObject item = register.getJSONObject(j);
                        if (item.getInt("itemID") == selectedFilteredTransaction.getInt("itemID")) {
                            item.put("refunded", true);
                        }
                    }
                    break;
                }
            }

            refundObject.put("itemID", selectedFilteredTransaction.getLong("itemID"));
            refundObject.put("quantity", selectedFilteredTransaction.getLong("quantity"));
            refundObject.put("itemName", selectedFilteredTransaction.getString("itemName"));
            refundObject.put("price", selectedFilteredTransaction.getLong("price") * -1L);
        }

        turnHistory.put(refundObject);
        saveCurrentTurnHistory();
    }

    public void reverseItemSaleFromTurn(JSONObject selectedFilteredItem) {
        String roomSoldTo = selectedFilteredItem.getString("roomSoldTo");
        String changeDate = selectedFilteredItem.getString("changeDate");
        long itemID = selectedFilteredItem.getLong("itemID");
        long quantity = selectedFilteredItem.getLong("quantity");
        for(int i = 0;i<turnHistory.length();i++){
            JSONObject currentRegisterChange = turnHistory.getJSONObject(i);
            String changeType = currentRegisterChange.getString("changeType");
            if(changeType.equals("sale")
                    &&changeDate.equals(currentRegisterChange.getString("changeDate"))
                    && roomSoldTo.equals(currentRegisterChange.getString("roomSoldTo"))){
                JSONArray currentArray = currentRegisterChange.getJSONArray("register");
                for(int j = 0;j<currentArray.length();j++){
                    JSONObject currentObject = currentArray.getJSONObject(j);
                    if(currentObject.getLong("itemID") == itemID && currentObject.getLong("quantity") == quantity){
                        currentArray.remove(j);
                    }
                }
            }
        }
    }
}
