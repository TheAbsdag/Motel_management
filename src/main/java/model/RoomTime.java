package model;

/**
 * Representation of custom time pricing for a room to be used for each amount of time.
 *
 * <p>Each room has 3 configurable time slots with a duration (in seconds) and a price.
 * When no custom configuration exists, {@link #getDefaultTimeSlots()} provides the
 * hardcoded defaults: 3h/$40k, 12h/$45k, 24h/$88k.
 *
 * @author SECC
 */
public class RoomTime {

    private final long price;
    private final long timeSeconds;

    private static final long DEFAULT_SLOT_1_SECONDS = 10800L;
    private static final long DEFAULT_SLOT_2_SECONDS = 43200L;
    private static final long DEFAULT_SLOT_3_SECONDS = 86400L;

    private static final long DEFAULT_SLOT_1_PRICE = 40000L;
    private static final long DEFAULT_SLOT_2_PRICE = 45000L;
    private static final long DEFAULT_SLOT_3_PRICE = 88000L;

    /**
     * Creates a time-price pair for a room pricing slot.
     *
     * @param price       price in pesos
     * @param timeSeconds duration in seconds
     */
    public RoomTime(long price, long timeSeconds) {
        this.price = price;
        this.timeSeconds = timeSeconds;
    }

    /**
     * Returns the price for this time slot.
     *
     * @return price in pesos
     */
    public long getPrice() {
        return price;
    }

    /**
     * Returns the duration of this time slot.
     *
     * @return duration in seconds
     */
    public long getTimeSeconds() {
        return timeSeconds;
    }

    /**
     * Creates a copy of this RoomTime with a different price.
     * @param newPrice the new price in pesos
     * @return a new RoomTime instance with the updated price
     */
    public RoomTime withPrice(long newPrice) {
        return new RoomTime(newPrice, this.timeSeconds);
    }

    /**
     * Creates a copy of this RoomTime with a different duration.
     * @param newTimeSeconds the new duration in seconds
     * @return a new RoomTime instance with the updated duration
     */
    public RoomTime withTimeSeconds(long newTimeSeconds) {
        return new RoomTime(this.price, newTimeSeconds);
    }

    /**
     * Returns the three default time slots used when a room has no custom pricing.
     * <ul>
     *   <li>Slot 0: 3 hours / $40,000</li>
     *   <li>Slot 1: 12 hours / $45,000</li>
     *   <li>Slot 2: 24 hours / $88,000</li>
     * </ul>
     * @return an array of 3 RoomTime instances with hardcoded defaults
     */
    public static RoomTime[] getDefaultTimeSlots() {
        return new RoomTime[] {
            new RoomTime(DEFAULT_SLOT_1_PRICE, DEFAULT_SLOT_1_SECONDS),
            new RoomTime(DEFAULT_SLOT_2_PRICE, DEFAULT_SLOT_2_SECONDS),
            new RoomTime(DEFAULT_SLOT_3_PRICE, DEFAULT_SLOT_3_SECONDS)
        };
    }

    /**
     * Returns the supported time unit conventions (seconds, minutes, hours).
     * @return character array {@code {'s', 'm', 'h'}}
     */
    public static char[] getTimeConventions() {
        return new char[] {'s', 'm', 'h'};
    }
}
