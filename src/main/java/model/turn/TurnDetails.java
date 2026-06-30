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
        this.version = 3;
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
                case RoomBookingActivity r -> accumulateRoomBooking(r, newSummary);
                case SaleActivity s -> accumulateSale(s, newSummary);
                case RefundActivity r -> accumulateRefund(r, newSummary);
                case SpendingActivity s -> accumulateSpending(s);
                case ExtraChangeActivity e -> accumulateExtraChange(e, newSummary);
                default -> { /* roomSwap has no financial impact */ }
            }
        }
        summaryItems = newSummary;
        totalsComputed = true;
    }

    private void accumulateRoomBooking(RoomBookingActivity r, List<TurnSummaryItemData> summary) {
        if (!r.isOccupied()) return;
        long price = r.price();
        long serviceKey = r.servicedExtensionDuration() != 0 ? r.servicedExtensionDuration() : r.serviceDuration();
        totalSales += price;
        totalRooms += price;
        totalTurn += price;
        totalNet += price;
        findOrUpdateSummary(summary,
                si -> "room".equals(si.summaryType()) && si.serviceDuration() == serviceKey,
                () -> new TurnSummaryItemData("room", 1, price, null, serviceKey),
                si -> new TurnSummaryItemData("room", si.quantity() + 1, si.price() + price, null, serviceKey));
    }

    private void accumulateSale(SaleActivity s, List<TurnSummaryItemData> summary) {
        for (SaleItem item : s.items()) {
            long price = item.price();
            long qty = item.quantity();
            totalSales += price;
            totalItems += price;
            totalTurn += price;
            totalNet += price;
            findOrUpdateSummary(summary,
                    si -> "item".equals(si.summaryType()) && item.itemName().equals(si.name()),
                    () -> new TurnSummaryItemData("item", (int) qty, price, item.itemName(), 0),
                    si -> new TurnSummaryItemData("item", (int)(si.quantity() + qty), si.price() + price, item.itemName(), 0));
        }
    }

    private void accumulateRefund(RefundActivity r, List<TurnSummaryItemData> summary) {
        long price = r.price();
        totalRefunds += price;
        totalTurn += price;
        totalNet += price;
        if (r.refundType() == RefundType.SALE_REFUND) {
            totalItemRefunds += price;
            findOrUpdateSummary(summary,
                    si -> "itemRefund".equals(si.summaryType()) && r.itemName().equals(si.name()),
                    () -> new TurnSummaryItemData("itemRefund", (int) r.quantity(), price, r.itemName(), 0),
                    si -> new TurnSummaryItemData("itemRefund", (int)(si.quantity() + r.quantity()), si.price() + price, r.itemName(), 0));
        } else {
            totalRoomRefunds += price;
            long serviceKey = r.refundServiceDuration();
            findOrUpdateSummary(summary,
                    si -> "roomRefund".equals(si.summaryType()) && si.serviceDuration() == serviceKey,
                    () -> new TurnSummaryItemData("roomRefund", 1, price, null, serviceKey),
                    si -> new TurnSummaryItemData("roomRefund", si.quantity() + 1, si.price() + price, null, serviceKey));
        }
    }

    private void accumulateSpending(SpendingActivity s) {
        long value = s.value();
        totalSpending += value;
        totalTurn += value;
        totalNet += value;
    }

    private void accumulateExtraChange(ExtraChangeActivity e, List<TurnSummaryItemData> summary) {
        long value = e.value();
        if (e.extraType() == ExtraChangeType.BANK_TRANSFER) {
            totalBankTransfers += value * -1L;
        } else {
            totalDeposits += value * -1L;
        }
        totalNet += value;
        findOrUpdateSummary(summary,
                si -> "extraChange".equals(si.summaryType()) && e.extraType().getValue().equals(si.name()),
                () -> new TurnSummaryItemData("extraChange", 1, value, e.extraType().getValue(), 0),
                si -> new TurnSummaryItemData("extraChange", si.quantity() + 1, si.price() + value, e.extraType().getValue(), 0));
    }

    private void findOrUpdateSummary(List<TurnSummaryItemData> summary,
                                     java.util.function.Predicate<TurnSummaryItemData> matcher,
                                     java.util.function.Supplier<TurnSummaryItemData> newItemSupplier,
                                     java.util.function.Function<TurnSummaryItemData, TurnSummaryItemData> updateMapper) {
        for (int j = 0; j < summary.size(); j++) {
            if (matcher.test(summary.get(j))) {
                summary.set(j, updateMapper.apply(summary.get(j)));
                return;
            }
        }
        summary.add(newItemSupplier.get());
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
            details.migrateActivitiesIfNeeded();
            details.computeTotalsAndSummary();
            return details;
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to deserialize TurnDetails", e);
            return new TurnDetails();
        }
    }

    private static final int CURRENT_VERSION = 3;

    /**
     * Migrates activities from older schema versions.
     * <p>
     * v2 → v3: Tower numbers changed from 1-based to 0-based.
     * RoomBookingActivity.towerNumber and RoomSwapActivity tower fields are decremented by 1.
     */
    private void migrateActivitiesIfNeeded() {
        if (version >= CURRENT_VERSION) return;
        if (version < 3 && activities != null) {
            List<TurnActivity> migrated = new ArrayList<>();
            for (TurnActivity a : activities) {
                switch (a) {
                    case RoomBookingActivity r -> {
                        int newTower = r.towerNumber() - 1;
                        RoomData rd = r.roomData() != null
                                ? new RoomData(newTower, r.roomData().floorNumber(),
                                        r.roomData().roomNumber(), r.roomData().roomString())
                                : null;
                        migrated.add(new RoomBookingActivity(
                                r.changeDate(), r.roomString(), r.roomNumber(), r.floorNumber(), newTower,
                                r.roomStatus(), r.startStatus(), r.endStatus(),
                                r.price(), r.serviceDuration(), r.extensionDuration(), r.servicedExtensionDuration(),
                                r.consecutiveTrans(), r.refunded(), rd));
                    }
                    case RoomSwapActivity s -> {
                        int newOrigTower = s.originalTowerNumber() - 1;
                        int newSwapTower = s.swappedTowerNumber() - 1;
                        RoomData origRd = s.originalRoomData() != null
                                ? new RoomData(newOrigTower, s.originalRoomData().floorNumber(),
                                        s.originalRoomData().roomNumber(), s.originalRoomData().roomString())
                                : null;
                        RoomData swapRd = s.swapRoomData() != null
                                ? new RoomData(newSwapTower, s.swapRoomData().floorNumber(),
                                        s.swapRoomData().roomNumber(), s.swapRoomData().roomString())
                                : null;
                        migrated.add(new RoomSwapActivity(
                                s.changeDate(),
                                s.originalRoom(), s.originalRoomNumber(), s.originalFloorNumber(), newOrigTower,
                                s.swappedRoom(), s.swappedRoomNumber(), s.swappedFloorNumber(), newSwapTower,
                                origRd, swapRd));
                    }
                    default -> migrated.add(a);
                }
            }
            activities = migrated;
        }
        version = CURRENT_VERSION;
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

    /**
     * @deprecated Currently not in use due to new history management creation
     * @param activities 
     */
    @Deprecated
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
