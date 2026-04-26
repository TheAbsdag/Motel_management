package model;

/**
 * Represents the possible states of a room in the motel.
 *
 * <ul>
 *   <li>{@link #FREE} — Room is available and ready for booking (status 1)</li>
 *   <li>{@link #CLEANING} — Room is being cleaned after check-out (status 2)</li>
 *   <li>{@link #OCCUPIED} — Room is currently booked by a guest (status 3)</li>
 * </ul>
 */
public enum RoomStatus {
    FREE(1),
    CLEANING(2),
    OCCUPIED(3);

    private final int code;

    RoomStatus(int code) {
        this.code = code;
    }

    /**
     * Returns the legacy integer code for JSON persistence.
     */
    public int getCode() {
        return code;
    }

    /**
     * Converts a legacy integer code to the corresponding RoomStatus enum.
     *
     * @param code the integer code (1, 2, or 3)
     * @return the matching RoomStatus
     * @throws IllegalArgumentException if the code is not 1, 2, or 3
     */
    public static RoomStatus fromCode(int code) {
        return switch (code) {
            case 1 -> FREE;
            case 2 -> CLEANING;
            case 3 -> OCCUPIED;
            default -> throw new IllegalArgumentException("Invalid room status code: " + code);
        };
    }
}
