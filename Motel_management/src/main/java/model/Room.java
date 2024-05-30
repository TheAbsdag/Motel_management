
package model;

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
    private int service;
    private Instant startStatus;
    private Instant endStatus;
    
    public Room(String roomName, int roomNumber, int floor){
        this.roomString = roomName;
        this.roomNumber = roomNumber;
        this.floorNumber = floor;
    }
    
    
}
