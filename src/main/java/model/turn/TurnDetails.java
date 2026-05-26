package model.turn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RoomStatus;
import model.dto.TurnSummaryItemData;
import model.json.ObjectMapperFactory;

/**
 * Aggregates turn-level data: basic information, activity list, and computed
 * financial totals.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TurnDetails {

    private static final Logger logger = Logger.getLogger(TurnDetails.class.getName());

    @JsonProperty("turnNumber")
    private long turnNumber;
    @JsonProperty("turnStart")
    private ZonedDateTime turnStart;
    @JsonProperty("turnEnd")
    private ZonedDateTime turnEnd;
    @JsonProperty("isTurnActive")
    private boolean isTurnActive;
    @JsonProperty("turnActivity")
    private List<TurnActivity> activities;

    @JsonProperty("totalItems")
    private long totalItems;
    @JsonProperty("totalSales")
    private long totalSales;
    @JsonProperty("totalRooms")
    private long totalRooms;
    @JsonProperty("totalRefunds")
    private long totalRefunds;
    @JsonProperty("totalItemRefunds")
    private long totalItemRefunds;
    @JsonProperty("totalRoomRefunds")
    private long totalRoomRefunds;
    @JsonProperty("totalSpending")
    private long totalSpending;
    @JsonProperty("totalTurn")
    private long totalTurn;
    @JsonProperty("totalBankTransfers")
    private long totalBankTransfers;
    @JsonProperty("totalDeposits")
    private long totalDeposits;
    @JsonProperty("totalNet")
    private long totalNet;
    @JsonProperty("version")
    private int version;

    @JsonIgnore
    private List<TurnSummaryItemData> summaryItems;
    @JsonIgnore
    private boolean totalsComputed;

    public TurnDetails() {
        this.activities = new ArrayList<>();
        this.summaryItems = new ArrayList<>();
        this.totalsComputed = false;
        this.version = 2;
    }

    public TurnDetails(long turnNumber, ZonedDateTime turnStart, boolean isTurnActive) {
        this();
        this.turnNumber = turnNumber;
        this.turnStart = turnStart;
        this.isTurnActive = isTurnActive;
    }

    public void addActivity(TurnActivity activity) {
        activities.add(activity);
        totalsComputed = false;
    }

    public void clear() {
        activities.clear();
        summaryItems.clear();
        turnNumber = 0;
        turnStart = null;
        turnEnd = null;
        isTurnActive = false;
        resetTotals();
        totalsComputed = false;
    }

    private void resetTotals() {
        totalItems = 0;
        totalSales = 0;
        totalRooms = 0;
        totalRefunds = 0;
        totalItemRefunds = 0;
        totalRoomRefunds = 0;
        totalSpending = 0;
        totalTurn = 0;
        totalBankTransfers = 0;
        totalDeposits = 0;
        totalNet = 0;
        summaryItems.clear();
    }

    public void computeTotalsAndSummary() {
        if (totalsComputed) return;
        resetTotals();
        List<TurnSummaryItemData> newSummary = new ArrayList<>();

        for (TurnActivity activity : activities) {
            switch (activity) {
                case RoomBookingActivity r -> {
                    if (r.isOccupied()) {
                        long price = r.price();
                        long serviceKey = r.servicedExtensionDuration() != 0 ? r.servicedExtensionDuration() : r.serviceDuration();
                        totalSales += price;
                        totalRooms += price;
                        totalTurn += price;
                        totalNet += price;
                        boolean found = false;
                        for (int j = 0; j < newSummary.size(); j++) {
                            TurnSummaryItemData si = newSummary.get(j);
                            if ("room".equals(si.summaryType()) && si.serviceDuration() == serviceKey) {
                                newSummary.set(j, new TurnSummaryItemData("room", si.quantity() + 1, si.price() + price, null, serviceKey));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newSummary.add(new TurnSummaryItemData("room", 1, price, null, serviceKey));
                        }
                    }
                }
                case SaleActivity s -> {
                    for (SaleItem item : s.items()) {
                        long price = item.price();
                        long qty = item.quantity();
                        totalSales += price;
                        totalItems += price;
                        totalTurn += price;
                        totalNet += price;
                        boolean found = false;
                        for (int j = 0; j < newSummary.size(); j++) {
                            TurnSummaryItemData si = newSummary.get(j);
                            if ("item".equals(si.summaryType()) && item.itemName().equals(si.name())) {
                                newSummary.set(j, new TurnSummaryItemData("item", (int)(si.quantity() + qty), si.price() + price, item.itemName(), 0));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newSummary.add(new TurnSummaryItemData("item", (int) qty, price, item.itemName(), 0));
                        }
                    }
                }
                case RefundActivity r -> {
                    long price = r.price();
                    totalRefunds += price;
                    totalTurn += price;
                    totalNet += price;
                    if (r.refundType() == RefundType.SALE_REFUND) {
                        totalItemRefunds += price;
                        boolean found = false;
                        for (int j = 0; j < newSummary.size(); j++) {
                            TurnSummaryItemData si = newSummary.get(j);
                            if ("itemRefund".equals(si.summaryType()) && r.itemName().equals(si.name())) {
                                newSummary.set(j, new TurnSummaryItemData("itemRefund", (int)(si.quantity() + r.quantity()), si.price() + price, r.itemName(), 0));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newSummary.add(new TurnSummaryItemData("itemRefund", (int) r.quantity(), price, r.itemName(), 0));
                        }
                    } else {
                        totalRoomRefunds += price;
                        long serviceKey = r.refundServiceDuration();
                        boolean found = false;
                        for (int j = 0; j < newSummary.size(); j++) {
                            TurnSummaryItemData si = newSummary.get(j);
                            if ("roomRefund".equals(si.summaryType()) && si.serviceDuration() == serviceKey) {
                                newSummary.set(j, new TurnSummaryItemData("roomRefund", si.quantity() + 1, si.price() + price, null, serviceKey));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newSummary.add(new TurnSummaryItemData("roomRefund", 1, price, null, serviceKey));
                        }
                    }
                }
                case SpendingActivity s -> {
                    long value = s.value();
                    totalSpending += value;
                    totalTurn += value;
                    totalNet += value;
                }
                case ExtraChangeActivity e -> {
                    long value = e.value();
                    if (e.extraType() == ExtraChangeType.BANK_TRANSFER) {
                        totalBankTransfers += value * -1L;
                    } else {
                        totalDeposits += value * -1L;
                    }
                    totalNet += value;
                    boolean found = false;
                    for (int j = 0; j < newSummary.size(); j++) {
                        TurnSummaryItemData si = newSummary.get(j);
                        if ("extraChange".equals(si.summaryType()) && e.extraType().getValue().equals(si.name())) {
                            newSummary.set(j, new TurnSummaryItemData("extraChange", si.quantity() + 1, si.price() + value, e.extraType().getValue(), 0));
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        newSummary.add(new TurnSummaryItemData("extraChange", 1, value, e.extraType().getValue(), 0));
                    }
                }
                default -> { /* roomSwap has no financial impact */ }
            }
        }
        summaryItems = newSummary;
        totalsComputed = true;
    }

    /**
     * Serializes this turn details to a JSON string.
     *
     * @return JSON representation suitable for persistence
     */
    public String toJson() {
        computeTotalsAndSummary();
        try {
            return ObjectMapperFactory.get().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to serialize TurnDetails", e);
            return "{}";
        }
    }

    /**
     * Deserializes a {@code TurnDetails} from its JSON representation.
     *
     * @param json JSON string previously produced by {@link #toJson()}
     * @return the reconstructed turn details
     */
    public static TurnDetails fromJson(String json) {
        try {
            TurnDetails details = ObjectMapperFactory.get().readValue(json, TurnDetails.class);
            if (details.activities == null) {
                details.activities = new ArrayList<>();
            }
            details.summaryItems = new ArrayList<>();
            details.computeTotalsAndSummary();
            return details;
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to deserialize TurnDetails", e);
            return new TurnDetails();
        }
    }

    // --- Getters / Setters ---

    public long getTurnNumber() { return turnNumber; }
    public void setTurnNumber(long turnNumber) { this.turnNumber = turnNumber; }

    public ZonedDateTime getTurnStart() { return turnStart; }
    public void setTurnStart(ZonedDateTime turnStart) { this.turnStart = turnStart; }

    public ZonedDateTime getTurnEnd() { return turnEnd; }
    public void setTurnEnd(ZonedDateTime turnEnd) { this.turnEnd = turnEnd; }

    public boolean isTurnActive() { return isTurnActive; }
    public void setTurnActive(boolean turnActive) { isTurnActive = turnActive; }

    public List<TurnActivity> getActivities() { return activities; }

    public void setActivities(List<TurnActivity> activities) {
        this.activities = activities;
        totalsComputed = false;
    }

    public List<TurnSummaryItemData> getSummaryItems() {
        computeTotalsAndSummary();
        return summaryItems;
    }

    public long getTotalItems() { computeTotalsAndSummary(); return totalItems; }
    public long getTotalSales() { computeTotalsAndSummary(); return totalSales; }
    public long getTotalRooms() { computeTotalsAndSummary(); return totalRooms; }
    public long getTotalRefunds() { computeTotalsAndSummary(); return totalRefunds; }
    public long getTotalItemRefunds() { computeTotalsAndSummary(); return totalItemRefunds; }
    public long getTotalRoomRefunds() { computeTotalsAndSummary(); return totalRoomRefunds; }
    public long getTotalSpending() { computeTotalsAndSummary(); return totalSpending; }
    public long getTotalTurn() { computeTotalsAndSummary(); return totalTurn; }
    public long getTotalBankTransfers() { computeTotalsAndSummary(); return totalBankTransfers; }
    public long getTotalDeposits() { computeTotalsAndSummary(); return totalDeposits; }
    public long getTotalNet() { computeTotalsAndSummary(); return totalNet; }
}
