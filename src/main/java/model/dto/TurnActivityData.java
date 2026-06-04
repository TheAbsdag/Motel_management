package model.dto;

import java.time.ZonedDateTime;

/**
 * Typed data object for a single entry in a turn's activity log.
 * Represents one of several activity types: room booking, item sale, room swap,
 * refund, spending, or extra change (bank transfer / safe deposit).
 * <p>
 * This is a view-facing DTO; the Turn class internally stores the same data as JSON.
 */
public class TurnActivityData {

    private final String changeType;       // "room", "sale", "roomSwap", "refund", "spending", "extraChange"
    private final ZonedDateTime changeDate;
    private final String roomString;
    private final int roomStatus;
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
    private final String refundType;        // "roomRefund" or "saleRefund"
    private final String extraType;         // "bankTransfer" or "safeDeposit"
    private final String description;       // spending/extraChange description
    private final boolean refunded;         // whether the original transaction was already refunded

    // --- Factory methods ---

    /**
     * Creates a TurnActivityData for a room booking event.
     */
    public static TurnActivityData forRoomBooking(ZonedDateTime changeDate, String roomString,
                                                   int roomStatus, long price, long serviceDuration,
                                                   long servicedExtensionDuration, int consecutiveTrans,
                                                   boolean refunded) {
        return new TurnActivityData("room", changeDate, roomString, roomStatus,
                price, serviceDuration, servicedExtensionDuration, null, 0, 0, null, null, null,
                consecutiveTrans, null, null, null, refunded);
    }

    /**
     * Creates a TurnActivityData for a sale event line item.
     */
    public static TurnActivityData forSale(ZonedDateTime changeDate, String roomSoldTo,
                                            String itemName, long itemID, long quantity, long price,
                                            int consecutiveTrans, boolean refunded) {
        return new TurnActivityData("sale", changeDate, null, 0,
                price, 0, 0, itemName, itemID, quantity, roomSoldTo, null, null,
                consecutiveTrans, null, null, null, refunded);
    }

    /**
     * Creates a TurnActivityData for a room swap event.
     */
    public static TurnActivityData forRoomSwap(ZonedDateTime changeDate, String originalRoom,
                                                String swappedRoom) {
        return new TurnActivityData("roomSwap", changeDate, null, 0,
                0, 0, 0, null, 0, 0, null, originalRoom, swappedRoom,
                0, null, null, null, false);
    }

    /**
     * Creates a TurnActivityData for a refund event.
     */
    public static TurnActivityData forRefund(ZonedDateTime changeDate, String refundType,
                                              String roomRef, long price, long itemID,
                                              long quantity, String itemName, long serviceDuration) {
        return new TurnActivityData("refund", changeDate, roomRef, 0,
                price, serviceDuration, 0, itemName, itemID, quantity, null, null, null,
                0, refundType, null, null, false);
    }

    /**
     * Creates a TurnActivityData for a spending (expense) event.
     */
    public static TurnActivityData forSpending(ZonedDateTime changeDate, String description, long value) {
        return new TurnActivityData("spending", changeDate, null, 0,
                value, 0, 0, null, 0, 0, null, null, null,
                0, null, null, description, false);
    }

    /**
     * Creates a TurnActivityData for an extra change (bank transfer / safe deposit) event.
     */
    public static TurnActivityData forExtraChange(ZonedDateTime changeDate, String extraType,
                                                   String description, long value) {
        return new TurnActivityData("extraChange", changeDate, null, 0,
                value, 0, 0, null, 0, 0, null, null, null,
                0, null, extraType, description, false);
    }

    private TurnActivityData(String changeType, ZonedDateTime changeDate, String roomString,
                              int roomStatus, long price, long serviceDuration, long servicedExtensionDuration,
                              String itemName, long itemID, long quantity, String roomSoldTo,
                              String originalRoom, String swappedRoom, int consecutiveTrans,
                              String refundType, String extraType, String description, boolean refunded) {
        this.changeType = changeType;
        this.changeDate = changeDate;
        this.roomString = roomString;
        this.roomStatus = roomStatus;
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

    // --- Getters ---

    public String getChangeType() { return changeType; }
    public ZonedDateTime getChangeDate() { return changeDate; }
    public String getRoomString() { return roomString; }
    /**
     * Gets the current activity room status int enum
     * Currently not used, could be used with history reimplementation
     * @return the enum of roomstatus
     */
    public int getRoomStatus() { return roomStatus; }
    public long getPrice() { return price; }
    public long getServiceDuration() { return serviceDuration; }
    /**
     * Gets the current activity room extension
     * Currently not used, could be used with history reimplementation
     * Method could be deprecated in aloration of functionality of extension and usage
     * @return the enum of roomstatus
     */
    public long getServicedExtensionDuration() { return servicedExtensionDuration; }
    public String getItemName() { return itemName; }
    public long getItemID() { return itemID; }
    public long getQuantity() { return quantity; }
    public String getRoomSoldTo() { return roomSoldTo; }
    public String getOriginalRoom() { return originalRoom; }
    public String getSwappedRoom() { return swappedRoom; }
    public int getConsecutiveTrans() { return consecutiveTrans; }
    public String getRefundType() { return refundType; }
    public String getExtraType() { return extraType; }
    public String getDescription() { return description; }
    public boolean isRefunded() { return refunded; }

    /**
     * Returns the effective service duration in seconds (uses extension if present).
     */
    public long getEffectiveServiceDuration() {
        return servicedExtensionDuration != 0 ? servicedExtensionDuration : serviceDuration;
    }
}
