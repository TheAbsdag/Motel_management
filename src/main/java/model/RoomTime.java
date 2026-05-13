package model;

/**
 * 
 * Representation of custom time pricing for a room to be used for each amount of time
 *
 * @author SECC
 */
public class RoomTime {
    
    private final long price;
    private final long timeSeconds;
    
    private char[] timeConventions = {'s','m','h'};
    
    public  RoomTime(long price, long timeSeconds){
        this.price = price;
        this.timeSeconds = timeSeconds;
    }

    /**
     * @return the price
     */
    public long getPrice() {
        return price;
    }

    /**
     * @return the timeSeconds
     */
    public long getTimeSeconds() {
        return timeSeconds;
    }

    /**
     * @return the timeConventions
     */
    public char[] getTimeConventions() {
        return timeConventions;
    }
    
    
}
