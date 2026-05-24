package model;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a single room in the motel.
 *
 * <p>Each room belongs to a specific tower, floor, and has a unique room number.
 * Rooms track their current status ({@link RoomStatus}), service bookings,
 * and time extensions.
 *
 * <p>Status values:
 * <ul>
 *   <li>{@link RoomStatus#FREE} — available for booking</li>
 *   <li>{@link RoomStatus#CLEANING} — being cleaned after check-out</li>
 *   <li>{@link RoomStatus#OCCUPIED} — currently booked by a guest</li>
 * </ul>
 *
 * @author Santiago
 */
public class Room {
    private String roomString;
    private final int roomNumber;
    private final int floorNumber;
    private RoomStatus status;
    private final int towerNumber;
    
    private RoomTime[] customRoomTimeData;

    private long serviceDuration;
    private long extensionDuration;
    private Instant startStatus;
    private Instant endStatus;
    /**
     * Creates a new room with default FREE status.
     *
     * @param roomName   display string (e.g. "1-105")
     * @param floor      floor index
     * @param roomNumber room number within the floor
     * @param towerNumber tower index
     */
    public Room(String roomName, int floor, int roomNumber, int towerNumber) {
        this.roomString = roomName;
        this.roomNumber = roomNumber;
        this.floorNumber = floor;
        this.towerNumber = towerNumber;
        this.status = RoomStatus.FREE;
    }

    /**
     * Books the room (sets status to OCCUPIED) with a start time and service duration.
     * The end time is calculated as start + serviceDuration seconds.
     */
    public void setRoomStatus(RoomStatus status, Instant start, long serviceDuration) {
        this.status = status;
        this.startStatus = start;
        this.serviceDuration = serviceDuration;
        this.endStatus = start.plus(Duration.ofSeconds(serviceDuration));
        extensionDuration = 0;
    }

    /**
     * Marks the room for cleaning or changes status with a start time.
     */
    public void setRoomStatus(RoomStatus status, Instant start) {
        this.status = status;
        this.startStatus = start;
        serviceDuration = 0;
        extensionDuration = 0;
    }

    /**
     * Sets the room status and resets the start time to now, serviceDuration to 0,
     * and extensionDuration to 0.
     */
    public void setRoomStatus(RoomStatus status) {
        this.status = status;
        startStatus = Instant.now();
        serviceDuration = 0;
        extensionDuration = 0;
    }

    /**
     * Extends the current booking by the given duration in seconds.
     * Only meaningful when status is OCCUPIED.
     */
    public void extendRoomTime(long roomTimeDuration) {
        extensionDuration += roomTimeDuration;
        endStatus = endStatus.plus(Duration.ofSeconds(roomTimeDuration));
    }

    // --- Getters / Setters ---

    /**
     * Returns the display string for this room (e.g. "1-105").
     *
     * @return room display string
     */
    public String getRoomString() {
        return roomString;
    }

    /**
     * Updates the display name of this room.
     * @param roomString the new room identifier string
     */
    public void setRoomString(String roomString) {
        this.roomString = roomString;
    }

    /**
     * Returns the room number within its floor.
     *
     * @return room number index
     */
    public int getRoomNumber() {
        return roomNumber;
    }

    /**
     * Returns the floor index this room belongs to.
     *
     * @return floor index
     */
    public int getFloorNumber() {
        return floorNumber;
    }

    /**
     * Returns the current room status as an enum.
     */
    public RoomStatus getStatus() {
        return status;
    }

    /**
     * Returns the base service duration in seconds.
     *
     * @return service duration in seconds
     */
    public long getServiceDuration() {
        return serviceDuration;
    }

    /**
     * Returns the start time of the current status.
     *
     * @return start instant
     */
    public Instant getStartStatus() {
        return startStatus;
    }

    /**
     * Returns the expected end time of the current booking.
     * Only meaningful when status is OCCUPIED; returns null otherwise.
     */
    public Instant getEndStatus() {
        if (status != RoomStatus.OCCUPIED) {
            return null;
        }
        return endStatus;
    }

    /**
     * Returns the total extension duration in seconds.
     *
     * @return extension duration in seconds
     */
    public long getExtensionDuration() {
        return extensionDuration;
    }

    /**
     * Sets the extension duration in seconds for this room.
     *
     * @param extensionDuration extension duration in seconds
     */
    public void setExtensionDuration(long extensionDuration) {
        this.extensionDuration = extensionDuration;
    }

    /**
     * Returns the tower index this room belongs to.
     *
     * @return tower index
     */
    public int getTowerNumber() {
        return towerNumber;
    }

    /**
     * Returns the custom time pricing slots for this room.
     * Falls back to {@link RoomTime#getDefaultTimeSlots()} if no custom data has been set.
     * @return array of 3 RoomTime instances
     */
    public RoomTime[] getCustomRoomTimeData() {
        if (customRoomTimeData == null) {
            return RoomTime.getDefaultTimeSlots();
        }
        return customRoomTimeData;
    }

    /**
     * Sets custom time pricing for this room.
     * @param customRoomTimeData array of 3 RoomTime instances
     */
    public void setCustomRoomTimeData(RoomTime[] customRoomTimeData) {
        this.customRoomTimeData = customRoomTimeData;
    }

    /**
     * @return true if custom time data has been explicitly set (non-null)
     */
    public boolean hasCustomTimeData() {
        return customRoomTimeData != null;
    }
}
