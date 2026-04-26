package model.dto;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Typed data object for a turn history record displayed in the history view.
 * Replaces raw JSONObject usage for turn history data at the model→view boundary.
 */
public class TurnHistoryData {

    private final int turnNumber;
    private final ZonedDateTime turnStart;
    private final ZonedDateTime turnEnd;
    private final long totalSales;
    private final long totalItems;
    private final long totalRooms;
    private final String startDate;
    private final String duration;
    private final String startString;
    private final String endString;
    private final List<TurnActivityData> activities;

    /**
     * Full constructor for TurnHistoryData.
     *
     * @param turnNumber  the turn identifier
     * @param turnStart   turn start timestamp
     * @param turnEnd     turn end timestamp
     * @param totalSales  total sales amount in COP
     * @param totalItems  total item sales amount
     * @param totalRooms  total room booking amount
     * @param startDate   formatted start date string (Spanish locale)
     * @param duration    human-readable duration string
     * @param startString formatted start datetime string
     * @param endString   formatted end datetime string
     * @param activities  list of turn activity entries
     */
    public TurnHistoryData(int turnNumber, ZonedDateTime turnStart, ZonedDateTime turnEnd,
                           long totalSales, long totalItems, long totalRooms,
                           String startDate, String duration, String startString,
                           String endString, List<TurnActivityData> activities) {
        this.turnNumber = turnNumber;
        this.turnStart = turnStart;
        this.turnEnd = turnEnd;
        this.totalSales = totalSales;
        this.totalItems = totalItems;
        this.totalRooms = totalRooms;
        this.startDate = startDate;
        this.duration = duration;
        this.startString = startString;
        this.endString = endString;
        this.activities = activities;
    }

    // --- Getters ---

    public int getTurnNumber() { return turnNumber; }
    public ZonedDateTime getTurnStart() { return turnStart; }
    public ZonedDateTime getTurnEnd() { return turnEnd; }
    public long getTotalSales() { return totalSales; }
    public long getTotalItems() { return totalItems; }
    public long getTotalRooms() { return totalRooms; }
    public String getStartDate() { return startDate; }
    public String getDuration() { return duration; }
    public String getStartString() { return startString; }
    public String getEndString() { return endString; }
    public List<TurnActivityData> getActivities() { return activities; }
}
