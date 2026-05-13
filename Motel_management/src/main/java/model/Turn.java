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
import model.turn.ExtraChangeActivity;
import model.turn.ExtraChangeType;
import model.turn.RefundActivity;
import model.turn.RefundType;
import model.turn.RoomBookingActivity;
import model.turn.RoomSwapActivity;
import model.turn.SaleActivity;
import model.turn.SaleItem;
import model.turn.SpendingActivity;
import model.turn.TurnActivity;
import model.turn.TurnDetails;

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
    private TurnDetails turnDetails;
    private ZoneId zoneID;
    private boolean isTurnActive;

    public Turn(Instant start, ZoneId zoneID) {
        this.start = start;
        this.turnNumber = 1;
        this.turnDetails = new TurnDetails();
        this.zoneID = zoneID;
        isTurnActive = false;
    }

    public Turn(Instant start, Instant end, int turnID, ZoneId zoneID, JSONArray turnData) {
        this.start = start;
        this.end = end;
        this.turnNumber = turnID;
        this.zoneID = zoneID;
        isTurnActive = false;
        this.turnDetails = TurnDetails.fromJson(buildHistoryJson(start, end, turnID, turnData));
    }

    private JSONObject buildHistoryJson(Instant start, Instant end, int turnID, JSONArray turnData) {
        JSONObject json = new JSONObject();
        json.put("turnNumber", turnID);
        json.put("turnStart", start.atZone(zoneID).toString());
        json.put("turnEnd", end.atZone(zoneID).toString());
        json.put("isTurnActive", false);
        json.put("turnActivity", turnData);
        return json;
    }

    public RoomBookingActivity registerRoomChange(Room room, Instant time, long price, int extended, int consecutiveTransaction) {
        ZonedDateTime changeDate = time.atZone(zoneID);
        RoomStatus roomStatus = room.getStatus();

        RoomBookingActivity activity = new RoomBookingActivity(
                changeDate,
                room.getRoomString(),
                room.getRoomNumber(),
                room.getFloorNumber(),
                room.getTowerNumber(),
                roomStatus,
                room.getStartStatus().atZone(zoneID),
                roomStatus == RoomStatus.OCCUPIED ? room.getEndStatus().atZone(zoneID) : null,
                price,
                room.getService(),
                room.getExtension(),
                extended,
                consecutiveTransaction,
                false
        );
        turnDetails.addActivity(activity);
        return activity;
    }

    public void registerRoomSwap(Room originalRoom, Room swapedRoom, Instant time) {
        ZonedDateTime changeDate = time.atZone(zoneID);
        RoomSwapActivity activity = new RoomSwapActivity(
                changeDate,
                originalRoom.getRoomString(),
                originalRoom.getRoomNumber(),
                originalRoom.getFloorNumber(),
                originalRoom.getTowerNumber(),
                swapedRoom.getRoomString(),
                swapedRoom.getRoomNumber(),
                swapedRoom.getFloorNumber(),
                swapedRoom.getTowerNumber()
        );
        turnDetails.addActivity(activity);
    }

    public SaleActivity saveTransactionInformation(JSONArray registerJson, Room roomSoldTo, Instant time, int transactionNumber) {
        ZonedDateTime changeDate = time.atZone(zoneID);
        List<SaleItem> items = new ArrayList<>();
        for (int i = 0; i < registerJson.length(); i++) {
            items.add(SaleItem.fromJson(registerJson.getJSONObject(i)));
        }
        SaleActivity activity = new SaleActivity(
                changeDate,
                roomSoldTo.getRoomString(),
                items,
                transactionNumber
        );
        turnDetails.addActivity(activity);
        return activity;
    }

    public void setNewTurn(long turnID, Instant start) {
        this.turnNumber = turnID;
        this.start = start;
        ZonedDateTime localizedStart = start.atZone(zoneID);
        turnDetails.clear();
        turnDetails.setTurnNumber(turnNumber);
        turnDetails.setTurnStart(localizedStart);
        isTurnActive = true;
        turnDetails.setTurnActive(true);
    }

    public void turnEnd(Instant end) {
        this.end = end;
        turnDetails.setTurnEnd(end.atZone(zoneID));
        isTurnActive = false;
        turnDetails.setTurnActive(false);
    }

    public TurnDetails getBasicTurnInformation() {
        turnDetails.computeTotalsAndSummary();
        return turnDetails;
    }

    public TurnDetails getDetailedTurnInformation() {
        turnDetails.computeTotalsAndSummary();
        return turnDetails;
    }

    public JSONObject getDetailedTurnInformationAsJson() {
        return turnDetails.toJson();
    }

    public boolean setPreviousTurnJSON(JSONObject previousTurn) {
        turnDetails.clear();
        ZonedDateTime turnStartZ = ZonedDateTime.parse(previousTurn.getString("turnStart"));
        start = turnStartZ.toInstant();
        turnNumber = previousTurn.getLong("turnNumber");
        turnDetails.setTurnNumber(turnNumber);
        turnDetails.setTurnStart(turnStartZ);

        isTurnActive = previousTurn.getBoolean("isTurnActive");
        turnDetails.setTurnActive(isTurnActive);
        try {
            JSONArray activityArray = previousTurn.getJSONArray("turnActivity");
            for (int i = 0; i < activityArray.length(); i++) {
                JSONObject obj = activityArray.getJSONObject(i);
                String type = obj.getString("changeType");
                TurnActivity activity = parseActivityFromJson(obj, type);
                if (activity != null) {
                    turnDetails.addActivity(activity);
                }
            }
        } catch (JSONException ex) {
            System.out.println("Previous turn found, no previous activity found");
        }
        return isTurnActive;
    }

    private static TurnActivity parseActivityFromJson(JSONObject obj, String type) {
        return switch (type) {
            case "room" -> RoomBookingActivity.fromJson(obj);
            case "sale" -> SaleActivity.fromJson(obj);
            case "roomSwap" -> RoomSwapActivity.fromJson(obj);
            case "refund" -> RefundActivity.fromJson(obj);
            case "spending" -> SpendingActivity.fromJson(obj);
            case "extraChange" -> ExtraChangeActivity.fromJson(obj);
            default -> {
                System.out.println("Unknown changeType in activity: " + type);
                yield null;
            }
        };
    }

    public List<TurnActivityData> getActivityDataList() {
        List<TurnActivityData> result = new ArrayList<>();
        for (TurnActivity activity : turnDetails.getActivities()) {
            try {
                switch (activity) {
                    case SaleActivity s -> {
                        for (SaleItem item : s.items()) {
                            result.add(TurnActivityData.forSale(
                                    s.changeDate(), s.roomSoldTo(),
                                    item.itemName(), item.itemID(),
                                    item.quantity(), item.price(),
                                    s.consecutiveTrans(),
                                    item.refunded()
                            ));
                        }
                    }
                    case RoomBookingActivity r -> {
                        if (r.isOccupied()) {
                            result.add(TurnActivityData.forRoomBooking(
                                    r.changeDate(), r.roomString(),
                                    r.roomStatus().getCode(),
                                    r.price(), r.service(),
                                    r.servicedExtension(),
                                    r.consecutiveTrans(),
                                    r.refunded()
                            ));
                        }
                    }
                    case RoomSwapActivity s -> {
                        result.add(TurnActivityData.forRoomSwap(
                                s.changeDate(),
                                s.originalRoom(),
                                s.swappedRoom()
                        ));
                    }
                    case RefundActivity r -> {
                        if (r.refundType() == RefundType.SALE_REFUND) {
                            result.add(TurnActivityData.forRefund(
                                    r.changeDate(), r.refundType().getValue(),
                                    r.refundRoom(), r.price(),
                                    r.itemID(), r.quantity(),
                                    r.itemName(), 0
                            ));
                        } else {
                            result.add(TurnActivityData.forRefund(
                                    r.changeDate(), r.refundType().getValue(),
                                    r.refundRoom(), r.price(),
                                    0, 0, null,
                                    r.refundService()
                            ));
                        }
                    }
                    case SpendingActivity s -> {
                        result.add(TurnActivityData.forSpending(
                                s.changeDate(),
                                s.description(),
                                s.value()
                        ));
                    }
                    case ExtraChangeActivity e -> {
                        result.add(TurnActivityData.forExtraChange(
                                e.changeDate(),
                                e.extraType().getValue(),
                                e.description(),
                                e.value()
                        ));
                    }
                }
            } catch (Exception e) {
                System.out.println("Skipping malformed turn activity entry");
            }
        }
        return result;
    }

    public List<TurnSummaryItemData> getSummaryDataList() {
        return new ArrayList<>(turnDetails.getSummaryItems());
    }

    public void registerSpendingTransaction(String description, long value, int consecutiveTransaction, Instant time) {
        ZonedDateTime changeDate = time.atZone(zoneID);
        SpendingActivity activity = new SpendingActivity(changeDate, description, value, consecutiveTransaction);
        turnDetails.addActivity(activity);
    }

    public void registerExtraChangeTransaction(String description, long value, String type, int consecutiveTransaction, Instant time) {
        ZonedDateTime changeDate = time.atZone(zoneID);
        ExtraChangeActivity activity = new ExtraChangeActivity(
                changeDate,
                ExtraChangeType.fromString(type),
                description,
                value,
                consecutiveTransaction
        );
        turnDetails.addActivity(activity);
    }

    public void refundTransactionFromTurn(TurnActivity selectedActivity, int transactionNumber, Instant time, long specificItemID, long specificQuantity) {
        ZonedDateTime changeDate = time.atZone(zoneID);

        if (selectedActivity instanceof RoomBookingActivity r) {
            RefundActivity refundActivity = new RefundActivity(
                    changeDate,
                    RefundType.ROOM_REFUND,
                    transactionNumber,
                    r.consecutiveTrans(),
                    r.roomString(),
                    r.price() * -1L,
                    r.service(),
                    0, 0, null
            );
            markActivityRefunded(r, 0);
            turnDetails.addActivity(refundActivity);
        } else if (selectedActivity instanceof SaleActivity s) {
            String roomString = s.roomSoldTo();
            if (specificItemID > 0) {
                SaleItem targetItem = null;
                for (SaleItem item : s.items()) {
                    if (item.itemID() == specificItemID && item.quantity() == specificQuantity) {
                        targetItem = item;
                        break;
                    }
                }
                if (targetItem != null) {
                    RefundActivity refundActivity = new RefundActivity(
                            changeDate,
                            RefundType.SALE_REFUND,
                            transactionNumber,
                            s.consecutiveTrans(),
                            roomString,
                            targetItem.price() * -1L,
                            0,
                            targetItem.itemID(),
                            targetItem.quantity(),
                            targetItem.itemName()
                    );
                    markActivityRefunded(s, specificItemID);
                    turnDetails.addActivity(refundActivity);
                }
            } else {
                for (SaleItem item : s.items()) {
                    RefundActivity refundActivity = new RefundActivity(
                            changeDate,
                            RefundType.SALE_REFUND,
                            transactionNumber,
                            s.consecutiveTrans(),
                            roomString,
                            item.price() * -1L,
                            0,
                            item.itemID(),
                            item.quantity(),
                            item.itemName()
                    );
                    turnDetails.addActivity(refundActivity);
                }
                markActivityRefunded(s, 0);
            }
        }
    }

    private void markActivityRefunded(TurnActivity activity, long specificItemID) {
        int idx = turnDetails.getActivities().indexOf(activity);
        if (idx < 0) return;

        if (activity instanceof RoomBookingActivity r) {
            turnDetails.getActivities().set(idx, new RoomBookingActivity(
                    r.changeDate(), r.roomString(), r.roomNumber(), r.floorNumber(), r.towerNumber(),
                    r.roomStatus(), r.startStatus(), r.endStatus(),
                    r.price(), r.service(), r.extension(), r.servicedExtension(),
                    r.consecutiveTrans(), true
            ));
        } else if (activity instanceof SaleActivity s) {
            List<SaleItem> updatedItems = new ArrayList<>();
            for (SaleItem item : s.items()) {
                if (specificItemID == 0 || item.itemID() == specificItemID) {
                    updatedItems.add(new SaleItem(item.itemName(), item.itemID(), item.quantity(), item.price(), true));
                } else {
                    updatedItems.add(item);
                }
            }
            turnDetails.getActivities().set(idx, new SaleActivity(
                    s.changeDate(), s.roomSoldTo(), updatedItems, s.consecutiveTrans()
            ));
        }
    }

    public void reverseItemSaleFromTurn(TurnActivity activity, long itemID, long quantity) {
        if (!(activity instanceof SaleActivity s)) return;

        int idx = turnDetails.getActivities().indexOf(activity);
        if (idx < 0) return;

        List<SaleItem> updatedItems = new ArrayList<>();
        for (SaleItem item : s.items()) {
            if (item.itemID() != itemID || item.quantity() != quantity) {
                updatedItems.add(item);
            }
        }
        if (updatedItems.isEmpty()) {
            turnDetails.getActivities().remove(idx);
        } else {
            turnDetails.getActivities().set(idx, new SaleActivity(
                    s.changeDate(), s.roomSoldTo(), updatedItems, s.consecutiveTrans()
            ));
        }
    }

    public TurnActivity findActivity(int consecutiveTrans, String changeType) {
        for (TurnActivity a : turnDetails.getActivities()) {
            if (a.consecutiveTrans() == consecutiveTrans && consecutiveTrans > 0) {
                if (a instanceof RoomBookingActivity && "room".equals(changeType)) return a;
                if (a instanceof SaleActivity && "sale".equals(changeType)) return a;
            }
        }
        return null;
    }

    public List<TurnActivity> getActivities() {
        return turnDetails.getActivities();
    }

    public TurnActivity getActivity(int index) {
        return turnDetails.getActivities().get(index);
    }
}
