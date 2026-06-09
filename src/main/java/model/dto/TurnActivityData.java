package model.dto;

import java.time.ZonedDateTime;
import model.turn.ActivityType;
import model.turn.ExtraChangeType;
import model.turn.RefundType;

/**
 * Typed data object for a single entry in a turn's activity log.
 * Represents one of several activity types: room booking, item sale, room swap,
 * refund, spending, or extra change (bank transfer / safe deposit).
 */
public class TurnActivityData {

    private final ActivityType changeType;
    private final ZonedDateTime changeDate;
    private final String roomString;
    private final long price;
    private final long serviceDuration;
    private final long servicedExtensionDuration;
    private final String itemName;
    private final long itemID;
    private final long quantity;
    private final String roomSoldTo;
    private final String originalRoom;
    private final String swappedRoom;
    private final int consecutiveTrans;
    private final RefundType refundType;
    private final ExtraChangeType extraType;
    private final String description;
    private final boolean refunded;

    public static TurnActivityData forRoomBooking(ZonedDateTime changeDate, String roomString,
                                                    long price, long serviceDuration,
                                                    long servicedExtensionDuration, int consecutiveTrans,
                                                    boolean refunded) {
        return new TurnActivityData(ActivityType.ROOM, changeDate, roomString,
                price, serviceDuration, servicedExtensionDuration, null, 0, 0, null, null, null,
                consecutiveTrans, null, null, null, refunded);
    }

    public static TurnActivityData forSale(ZonedDateTime changeDate, String roomSoldTo,
                                            String itemName, long itemID, long quantity, long price,
                                            int consecutiveTrans, boolean refunded) {
        return new TurnActivityData(ActivityType.SALE, changeDate, null,
                price, 0, 0, itemName, itemID, quantity, roomSoldTo, null, null,
                consecutiveTrans, null, null, null, refunded);
    }

    public static TurnActivityData forRoomSwap(ZonedDateTime changeDate, String originalRoom,
                                                String swappedRoom) {
        return new TurnActivityData(ActivityType.ROOM_SWAP, changeDate, null,
                0, 0, 0, null, 0, 0, null, originalRoom, swappedRoom,
                0, null, null, null, false);
    }

    public static TurnActivityData forRefund(ZonedDateTime changeDate, RefundType refundType,
                                              String roomRef, long price, long itemID,
                                              long quantity, String itemName, long serviceDuration) {
        return new TurnActivityData(ActivityType.REFUND, changeDate, roomRef,
                price, serviceDuration, 0, itemName, itemID, quantity, null, null, null,
                0, refundType, null, null, false);
    }

    public static TurnActivityData forSpending(ZonedDateTime changeDate, String description, long value) {
        return new TurnActivityData(ActivityType.SPENDING, changeDate, null,
                value, 0, 0, null, 0, 0, null, null, null,
                0, null, null, description, false);
    }

    public static TurnActivityData forExtraChange(ZonedDateTime changeDate, ExtraChangeType extraType,
                                                   String description, long value) {
        return new TurnActivityData(ActivityType.EXTRA_CHANGE, changeDate, null,
                value, 0, 0, null, 0, 0, null, null, null,
                0, null, extraType, description, false);
    }

    private TurnActivityData(ActivityType changeType, ZonedDateTime changeDate, String roomString,
                              long price, long serviceDuration, long servicedExtensionDuration,
                              String itemName, long itemID, long quantity, String roomSoldTo,
                              String originalRoom, String swappedRoom, int consecutiveTrans,
                              RefundType refundType, ExtraChangeType extraType, String description, boolean refunded) {
        this.changeType = changeType;
        this.changeDate = changeDate;
        this.roomString = roomString;
        this.price = price;
        this.serviceDuration = serviceDuration;
        this.servicedExtensionDuration = servicedExtensionDuration;
        this.itemName = itemName;
        this.itemID = itemID;
        this.quantity = quantity;
        this.roomSoldTo = roomSoldTo;
        this.originalRoom = originalRoom;
        this.swappedRoom = swappedRoom;
        this.consecutiveTrans = consecutiveTrans;
        this.refundType = refundType;
        this.extraType = extraType;
        this.description = description;
        this.refunded = refunded;
    }

    public ActivityType getChangeType() { return changeType; }
    public ZonedDateTime getChangeDate() { return changeDate; }
    public String getRoomString() { return roomString; }
    public long getPrice() { return price; }
    public long getServiceDuration() { return serviceDuration; }
    public long getServicedExtensionDuration() { return servicedExtensionDuration; }
    public String getItemName() { return itemName; }
    public long getItemID() { return itemID; }
    public long getQuantity() { return quantity; }
    public String getRoomSoldTo() { return roomSoldTo; }
    public String getOriginalRoom() { return originalRoom; }
    public String getSwappedRoom() { return swappedRoom; }
    public int getConsecutiveTrans() { return consecutiveTrans; }
    public RefundType getRefundType() { return refundType; }
    public ExtraChangeType getExtraType() { return extraType; }
    public String getDescription() { return description; }
    public boolean isRefunded() { return refunded; }

    public long getEffectiveServiceDuration() {
        return servicedExtensionDuration != 0 ? servicedExtensionDuration : serviceDuration;
    }
}