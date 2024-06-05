
package model;

import java.time.Duration;
import java.time.Instant;

/**
 *
 * @author Santiago
 */
public class Room {
    private String roomString;
    private int roomNumber;
    private int floorNumber;
    private int status;
    /*
    For status management, 1 means free, 2 is cleaning, 3 is occupied.
    */
    private int service;
    private Instant startStatus;
    private Instant endStatus;
    
    public Room(String roomName, int roomNumber, int floor){
        this.roomString = roomName;
        this.roomNumber = roomNumber;
        this.floorNumber = floor;
    }
    
    //Method to book a room
    public void setRoomStatus( int status, Instant start, int service){
        this.status = status;
        this.startStatus = start;
        this.service = service;
        
        //Assigning the end time for it.
        this.endStatus = start.plus(Duration.ofHours(service));
    }
    
    //Method for cleaning a room
    public void setRoomStatus ( int status, Instant start){
        this.status = status;
        this.startStatus = start;
        service = 0;
    }
    
    //Method for a room that's just been freed
    public void setRoomStatus ( int status){
        this.status = status;
        startStatus = Instant.now();
        service = 0;
    }

    /**
     * @return the roomString
     */
    public String getRoomString() {
        return roomString;
    }

    /**
     * @return the roomNumber
     */
    public int getRoomNumber() {
        return roomNumber;
    }

    /**
     * @return the floorNumber
     */
    public int getFloorNumber() {
        return floorNumber;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the service
     */
    public int getService() {
        return service;
    }

    /**
     * @return the startStatus
     */
    public Instant getStartStatus() {
        return startStatus;
    }

    /**
     * @return the endStatus
     */
    public Instant getEndStatus() {
        //If there is no valid end status, such as the room being free or in cleaning, it will return a null
        if(status != 3){
            return null;
        }
        else{
            return endStatus;
        }
    }
    
}
