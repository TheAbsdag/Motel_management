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
    private final String roomString;
    private final int roomNumber;
    private final int floorNumber;
    private RoomStatus status;
    private final int towerNumber;

    private int service;
    private int extension;
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
     * The end time is calculated as start + service hours.
     */
    public void setRoomStatus(RoomStatus status, Instant start, int service) {
        this.status = status;
        this.startStatus = start;
        this.service = service;
        this.endStatus = start.plus(Duration.ofHours(service));
        extension = 0;
    }

    /**
     * Marks the room for cleaning or changes status with a start time.
     */
    public void setRoomStatus(RoomStatus status, Instant start) {
        this.status = status;
        this.startStatus = start;
        service = 0;
        extension = 0;
    }

    /**
     * Sets the room status without modifying time tracking (for initialization/freeing).
     */
    public void setRoomStatus(RoomStatus status) {
        this.status = status;
        startStatus = Instant.now();
        service = 0;
        extension = 0;
    }

    /**
     * Extends the current booking by the given number of hours.
     * Only meaningful when status is OCCUPIED.
     */
    public void extendRoomTime(int roomTime) {
        extension += roomTime;
        endStatus = endStatus.plus(Duration.ofHours(roomTime));
    }

    // --- Getters / Setters ---

    public String getRoomString() {
        return roomString;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    /**
     * Returns the current room status as an enum.
     */
    public RoomStatus getStatus() {
        return status;
    }

    public int getService() {
        return service;
    }

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

    public int getExtension() {
        return extension;
    }

    public void setExtension(int extension) {
        if (endStatus != null) {
            endStatus = endStatus.plus(Duration.ofHours(extension - this.extension));
        }
        this.extension = extension;
    }

    public int getTowerNumber() {
        return towerNumber;
    }
}
