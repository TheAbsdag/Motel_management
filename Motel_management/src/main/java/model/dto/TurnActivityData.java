package model.dto;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Typed data object for a single entry in a turn's activity log.
 * Represents one of three activity types: room booking, item sale, or room swap.
 * <p>
 * This is a view-facing DTO; the Turn class internally stores the same data as JSON.
 */
public class TurnActivityData {

    private final String changeType;       // "room", "sale", or "roomSwap"
    private final ZonedDateTime changeDate;
    private final String roomString;
    private final int roomStatus;
    private final long price;
    private final int service;
    private final int servicedExtension;
    private final String itemName;
    private final long itemID;
    private final long quantity;
    private final String roomSoldTo;
    private final String originalRoom;
    private final String swappedRoom;

    // --- Factory methods ---

    /**
     * Creates a TurnActivityData for a room booking event.
     */
    public static TurnActivityData forRoomBooking(ZonedDateTime changeDate, String roomString,
                                                  int roomStatus, long price, int service,
                                                  int servicedExtension) {
        return new TurnActivityData("room", changeDate, roomString, roomStatus,
                price, service, servicedExtension, null, 0, 0, null, null, null);
    }

    /**
     * Creates a TurnActivityData for a sale event line item.
     */
    public static TurnActivityData forSale(ZonedDateTime changeDate, String roomSoldTo,
                                           String itemName, long itemID, long quantity, long price) {
        return new TurnActivityData("sale", changeDate, null, 0,
                price, 0, 0, itemName, itemID, quantity, roomSoldTo, null, null);
    }

    /**
     * Creates a TurnActivityData for a room swap event.
     */
    public static TurnActivityData forRoomSwap(ZonedDateTime changeDate, String originalRoom,
                                               String swappedRoom) {
        return new TurnActivityData("roomSwap", changeDate, null, 0,
                0, 0, 0, null, 0, 0, null, originalRoom, swappedRoom);
    }

    private TurnActivityData(String changeType, ZonedDateTime changeDate, String roomString,
                             int roomStatus, long price, int service, int servicedExtension,
                             String itemName, long itemID, long quantity, String roomSoldTo,
                             String originalRoom, String swappedRoom) {
        this.changeType = changeType;
        this.changeDate = changeDate;
        this.roomString = roomString;
        this.roomStatus = roomStatus;
        this.price = price;
        this.service = service;
        this.servicedExtension = servicedExtension;
        this.itemName = itemName;
        this.itemID = itemID;
        this.quantity = quantity;
        this.roomSoldTo = roomSoldTo;
        this.originalRoom = originalRoom;
        this.swappedRoom = swappedRoom;
    }

    // --- Getters ---

    public String getChangeType() { return changeType; }
    public ZonedDateTime getChangeDate() { return changeDate; }
    public String getRoomString() { return roomString; }
    public int getRoomStatus() { return roomStatus; }
    public long getPrice() { return price; }
    public int getService() { return service; }
    public int getServicedExtension() { return servicedExtension; }
    public String getItemName() { return itemName; }
    public long getItemID() { return itemID; }
    public long getQuantity() { return quantity; }
    public String getRoomSoldTo() { return roomSoldTo; }
    public String getOriginalRoom() { return originalRoom; }
    public String getSwappedRoom() { return swappedRoom; }

    /**
     * Returns the effective service duration (uses extension if present).
     */
    public int getEffectiveService() {
        return servicedExtension != 0 ? servicedExtension : service;
    }
}
