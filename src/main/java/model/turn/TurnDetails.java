package model.turn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RoomStatus;
import model.dto.TurnInventoryRowData;
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
    @JsonProperty("inventoryMovements")
    private List<InventoryMovement> inventoryMovements;
    @JsonProperty("openingInventory")
    private List<InventorySnapshotItem> openingInventory;
    @JsonProperty("closingInventory")
    private List<InventorySnapshotItem> closingInventory;

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
        this.inventoryMovements = new ArrayList<>();
        this.openingInventory = new ArrayList<>();
        this.closingInventory = new ArrayList<>();
        this.summaryItems = new ArrayList<>();
        this.totalsComputed = false;
        // version stays 0 until the file declares it: 0 means the file predates
        // versioning, so migration must run. toJson() stamps the current version.
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
        inventoryMovements.clear();
        openingInventory.clear();
        closingInventory.clear();
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
        version = CURRENT_VERSION;
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
            if (details.inventoryMovements == null) details.inventoryMovements = new ArrayList<>();
            if (details.openingInventory == null) details.openingInventory = new ArrayList<>();
            if (details.closingInventory == null) details.closingInventory = new ArrayList<>();
            details.summaryItems = new ArrayList<>();
            details.migrateActivitiesIfNeeded();
            details.computeTotalsAndSummary();
            return details;
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to deserialize TurnDetails", e);
            return new TurnDetails();
        }
    }

    private static final int CURRENT_VERSION = 4;

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

    /**
     * Records a manual inventory movement.
     *
     * @param movement movement to append; null is ignored
     */
    public void addInventoryMovement(InventoryMovement movement) {
        if (movement != null) {
            inventoryMovements.add(movement);
        }
    }

    public List<InventoryMovement> getInventoryMovements() { return inventoryMovements; }

    public void setInventoryMovements(List<InventoryMovement> movements) {
        this.inventoryMovements = movements != null ? new ArrayList<>(movements) : new ArrayList<>();
    }

    public List<InventorySnapshotItem> getOpeningInventory() { return openingInventory; }

    public void setOpeningInventory(List<InventorySnapshotItem> opening) {
        this.openingInventory = opening != null ? new ArrayList<>(opening) : new ArrayList<>();
    }

    public List<InventorySnapshotItem> getClosingInventory() { return closingInventory; }

    public void setClosingInventory(List<InventorySnapshotItem> closing) {
        this.closingInventory = closing != null ? new ArrayList<>(closing) : new ArrayList<>();
    }

    /**
     * Builds the per-product inventory movement summary of this turn.
     *
     * <p>One row exists for every product that appears in the opening snapshot, in a
     * manual movement, in a sale, in a sale refund or in the current stock, so deleted
     * products stay visible with a current stock of zero. Sales and courtesy sales are
     * split by price, exactly as the printed report does. Refunds, consumptions and
     * deletions have no column of their own, so the current stock is the real stock
     * after all of them.
     *
     * <p>When the turn has no opening snapshot (data written before inventory
     * traceability existed), {@code received} is rebuilt from the tracked movements
     * instead of showing a misleading zero.
     *
     * @param currentStock current stock of every product
     * @return one row per product, ordered by product name
     */
    public List<TurnInventoryRowData> getInventoryRows(List<InventorySnapshotItem> currentStock) {
        Map<Long, InventorySnapshotItem> opening = indexByItemID(openingInventory);
        Map<Long, InventorySnapshotItem> current = indexByItemID(currentStock);

        Map<Long, Long> increases = new LinkedHashMap<>();
        Map<Long, Long> consumptions = new LinkedHashMap<>();
        Map<Long, String> names = new LinkedHashMap<>();
        for (InventoryMovement movement : inventoryMovements) {
            if (movement.movementType() == MovementType.INCREASE) {
                increases.merge(movement.itemID(), movement.quantity(), Long::sum);
            } else {
                consumptions.merge(movement.itemID(), movement.quantity(), Long::sum);
            }
            names.putIfAbsent(movement.itemID(), movement.itemName());
        }

        Map<Long, Long> sold = new LinkedHashMap<>();
        Map<Long, Long> courtesy = new LinkedHashMap<>();
        Map<Long, Long> refunds = new LinkedHashMap<>();
        for (TurnActivity activity : activities) {
            if (activity instanceof SaleActivity sale) {
                for (SaleItem item : sale.items()) {
                    if (item.price() == 0) {
                        courtesy.merge(item.itemID(), item.quantity(), Long::sum);
                    } else {
                        sold.merge(item.itemID(), item.quantity(), Long::sum);
                    }
                    names.putIfAbsent(item.itemID(), item.itemName());
                }
            } else if (activity instanceof RefundActivity refund
                    && refund.refundType() == RefundType.SALE_REFUND) {
                refunds.merge(refund.itemID(), refund.quantity(), Long::sum);
                names.putIfAbsent(refund.itemID(), refund.itemName());
            }
        }

        Set<Long> itemIDs = new LinkedHashSet<>();
        itemIDs.addAll(opening.keySet());
        itemIDs.addAll(increases.keySet());
        itemIDs.addAll(consumptions.keySet());
        itemIDs.addAll(sold.keySet());
        itemIDs.addAll(courtesy.keySet());
        itemIDs.addAll(refunds.keySet());
        itemIDs.addAll(current.keySet());

        boolean hasOpening = !opening.isEmpty();
        List<TurnInventoryRowData> rows = new ArrayList<>();
        for (long itemID : itemIDs) {
            InventorySnapshotItem openingItem = opening.get(itemID);
            InventorySnapshotItem currentItem = current.get(itemID);
            String itemName = currentItem != null ? currentItem.itemName()
                    : openingItem != null ? openingItem.itemName()
                    : names.get(itemID);
            long increaseQty = increases.getOrDefault(itemID, 0L);
            long soldQty = sold.getOrDefault(itemID, 0L);
            long courtesyQty = courtesy.getOrDefault(itemID, 0L);
            long currentQty = currentItem != null ? currentItem.quantity() : 0L;
            long receivedQty;
            if (openingItem != null) {
                receivedQty = openingItem.quantity();
            } else if (hasOpening) {
                receivedQty = 0L;
            } else {
                receivedQty = currentQty - increaseQty - refunds.getOrDefault(itemID, 0L)
                        + soldQty + courtesyQty + consumptions.getOrDefault(itemID, 0L);
            }
            rows.add(new TurnInventoryRowData(itemID, itemName, receivedQty, increaseQty,
                    soldQty, courtesyQty, currentQty));
        }
        rows.sort(Comparator
                .comparing((TurnInventoryRowData row) -> row.itemName() == null
                        ? "" : row.itemName().toLowerCase(Locale.ROOT))
                .thenComparingLong(TurnInventoryRowData::itemID));
        return rows;
    }

    private static Map<Long, InventorySnapshotItem> indexByItemID(List<InventorySnapshotItem> items) {
        Map<Long, InventorySnapshotItem> indexed = new LinkedHashMap<>();
        if (items != null) {
            for (InventorySnapshotItem item : items) {
                indexed.put(item.itemID(), item);
            }
        }
        return indexed;
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
