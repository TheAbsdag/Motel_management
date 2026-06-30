package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import model.turn.ActivityType;
import model.turn.ExtraChangeActivity;
import model.turn.ExtraChangeType;
import model.turn.RefundActivity;
import model.turn.RefundType;
import model.turn.RoomBookingActivity;
import model.turn.RoomSwapActivity;
import model.turn.SaleActivity;
import model.turn.SaleItem;
import model.turn.SpendingActivity;
import model.turn.RoomData;
import model.turn.TurnActivity;
import model.turn.TurnDetails;

/**
 * Represents a work shift (turn) in the motel, tracking all transactions and
 * activities that occur during that shift.
 *
 * @author Santiago
 */
public class Turn {

    public long getTurnNumber() {
        return turnNumber;
    }

    private Instant start;
    private Instant end;
    private long turnNumber;
    private TurnDetails turnDetails;
    private ZoneId zoneID;
    private boolean isTurnActive;

    private static final Logger logger = Logger.getLogger(Turn.class.getName());

    /**
     * Creates a new turn starting at the given instant.
     */
    public Turn(Instant start, ZoneId zoneID) {
        this.start = start;
        this.turnNumber = 1;
        this.turnDetails = new TurnDetails();
        this.zoneID = zoneID;
        isTurnActive = false;
    }

    /**
     * Creates a turn from historical data (previously saved turn).
     */
    public Turn(ZoneId zoneID, String historyJson) {
        this.zoneID = zoneID;
        isTurnActive = false;
        this.turnDetails = TurnDetails.fromJson(historyJson);
        this.start = turnDetails.getTurnStart().toInstant();
        this.turnNumber = turnDetails.getTurnNumber();
    }

    /**
     * Records a room status change (booking, check-out, cleaning) as a turn activity.
     */
    public RoomBookingActivity registerRoomChange(Room room, Instant time, long price, long extended, int consecutiveTransaction) {
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
                room.getServiceDuration(),
                room.getExtensionDuration(),
                extended,
                consecutiveTransaction,
                false
        );
        turnDetails.addActivity(activity);
        return activity;
    }

    /**
     * Records a room-to-room swap as a turn activity.
     */
    public void registerRoomSwap(Room originalRoom, Room swappedRoom, Instant time) {
        ZonedDateTime changeDate = time.atZone(zoneID);
        RoomSwapActivity activity = new RoomSwapActivity(
                changeDate,
                originalRoom.getRoomString(),
                originalRoom.getRoomNumber(),
                originalRoom.getFloorNumber(),
                originalRoom.getTowerNumber(),
                swappedRoom.getRoomString(),
                swappedRoom.getRoomNumber(),
                swappedRoom.getFloorNumber(),
                swappedRoom.getTowerNumber()
        );
        turnDetails.addActivity(activity);
    }

    /**
     * Records a sale transaction as a turn activity.
     */
    public SaleActivity saveTransactionInformation(List<CartItem> items, Room roomSoldTo, Instant time, int transactionNumber) {
        ZonedDateTime changeDate = time.atZone(zoneID);
        List<SaleItem> saleItems = new ArrayList<>();
        for (CartItem ci : items) {
            saleItems.add(new SaleItem(ci.itemName(), ci.itemID(), ci.quantity(), ci.price(), false));
        }
        RoomData roomData = new RoomData(
                roomSoldTo.getTowerNumber(),
                roomSoldTo.getFloorNumber(),
                roomSoldTo.getRoomNumber(),
                roomSoldTo.getRoomString()
        );
        SaleActivity activity = new SaleActivity(
                changeDate,
                roomSoldTo.getRoomString(),
                saleItems,
                transactionNumber,
                roomData
        );
        turnDetails.addActivity(activity);
        return activity;
    }

    /**
     * Initialises a new turn with the given ID and start time.
     */
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

    /**
     * Marks the turn as ended and stores the end time.
     */
    public void turnEnd(Instant end) {
        this.end = end;
        turnDetails.setTurnEnd(end.atZone(zoneID));
        isTurnActive = false;
        turnDetails.setTurnActive(false);
    }

    /**
     * Returns the aggregated turn data with computed totals and summary.
     */
    public TurnDetails getDetailedTurnInformation() {
        turnDetails.computeTotalsAndSummary();
        return turnDetails;
    }

    /**
     * Returns the full turn data serialised as a JSON string.
     */
    public String getDetailedTurnInformationAsJson() {
        return turnDetails.toJson();
    }

    /**
     * Restores a previous turn from its JSON representation.
     */
    public boolean setPreviousTurnJSON(String previousTurn) {
        TurnDetails loaded = TurnDetails.fromJson(previousTurn);
        this.turnDetails = loaded;
        this.start = loaded.getTurnStart().toInstant();
        this.turnNumber = loaded.getTurnNumber();
        this.isTurnActive = loaded.isTurnActive();
        return isTurnActive;
    }

    /**
     * Returns the turn activities converted to a flat list of DTOs suitable
     * for display in the UI.
     */
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
                                    r.price(), r.serviceDuration(),
                                    r.servicedExtensionDuration(),
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
                                    r.changeDate(), r.refundType(),
                                    r.refundRoom(), r.price(),
                                    r.itemID(), r.quantity(),
                                    r.itemName(), 0
                            ));
                        } else {
                            result.add(TurnActivityData.forRefund(
                                    r.changeDate(), r.refundType(),
                                    r.refundRoom(), r.price(),
                                    0, 0, null,
                                    r.refundServiceDuration()
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
                                e.extraType(),
                                e.description(),
                                e.value()
                        ));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Skipping malformed turn activity entry", e);
            }
        }
        return result;
    }

    /**
     * Returns the per-concept summary items (rooms, products, refunds, extra changes).
     */
    public List<TurnSummaryItemData> getSummaryDataList() {
        return new ArrayList<>(turnDetails.getSummaryItems());
    }

    /**
     * Records an operational expense (spending) as a turn activity.
     */
    public void registerSpendingTransaction(String description, long value, int consecutiveTransaction, Instant time) {
        ZonedDateTime changeDate = time.atZone(zoneID);
        SpendingActivity activity = new SpendingActivity(changeDate, description, value, consecutiveTransaction);
        turnDetails.addActivity(activity);
    }

    /**
     * Records a bank transfer or safe deposit as a turn activity.
     */
    public void registerExtraChangeTransaction(String description, long value, ExtraChangeType changeType, int consecutiveTransaction, Instant time) {
        ZonedDateTime changeDate = time.atZone(zoneID);
        ExtraChangeActivity activity = new ExtraChangeActivity(
                changeDate,
                changeType,
                description,
                value,
                consecutiveTransaction
        );
        turnDetails.addActivity(activity);
    }

    /**
     * Processes a refund for a room booking or sale activity and records it
     * as a new turn activity.
     */
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
                    r.serviceDuration(),
                    0, 0, null,
                    r.roomData()
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
                            targetItem.itemName(),
                            s.roomSoldToData()
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
                            item.itemName(),
                            s.roomSoldToData()
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
                    r.price(), r.serviceDuration(), r.extensionDuration(), r.servicedExtensionDuration(),
                    r.consecutiveTrans(), true, r.roomData()
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
                    s.changeDate(), s.roomSoldTo(), updatedItems, s.consecutiveTrans(), s.roomSoldToData()
            ));
        }
    }

    /**
     * Removes a specific item from a sale activity without creating a refund
     * record.
     */
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
                    s.changeDate(), s.roomSoldTo(), updatedItems, s.consecutiveTrans(), s.roomSoldToData()
            ));
        }
    }

    /**
     * Finds a room booking or sale activity by its consecutive transaction number
     * and activity type.
     * Test focused method
     */
    public TurnActivity findActivity(int consecutiveTrans, ActivityType activityType) {
        for (TurnActivity a : turnDetails.getActivities()) {
            if (a.consecutiveTrans() == consecutiveTrans && consecutiveTrans > 0) {
                if (a instanceof RoomBookingActivity && activityType == ActivityType.ROOM) return a;
                if (a instanceof SaleActivity && activityType == ActivityType.SALE) return a;
            }
        }
        return null;
    }

    /**
     * Returns the full list of turn activities.
     * Not used, replaced by internal management of turn details
     * @deprecated 
     */
    @Deprecated
    public List<TurnActivity> getActivities() {
        return turnDetails.getActivities();
    }

    /**
     * Returns the activity at the given index.
     * Not used, replaced by internal management of turn details
     * @deprecated 
     */
    @Deprecated
    public TurnActivity getActivity(int index) {
        return turnDetails.getActivities().get(index);
    }
}
