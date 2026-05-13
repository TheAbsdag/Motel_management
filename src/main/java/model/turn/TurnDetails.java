package model.turn;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import model.RoomStatus;
import model.dto.TurnSummaryItemData;

public class TurnDetails {

    private long turnNumber;
    private ZonedDateTime turnStart;
    private ZonedDateTime turnEnd;
    private boolean isTurnActive;
    private List<TurnActivity> activities;

    private long totalItems;
    private long totalSales;
    private long totalRooms;
    private long totalRefunds;
    private long totalItemRefunds;
    private long totalRoomRefunds;
    private long totalSpending;
    private long totalTurn;
    private long totalBankTransfers;
    private long totalDeposits;
    private long totalNet;

    private List<TurnSummaryItemData> summaryItems;
    private boolean totalsComputed;

    public TurnDetails() {
        this.activities = new ArrayList<>();
        this.summaryItems = new ArrayList<>();
        this.totalsComputed = false;
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
                        int serviceKey = r.servicedExtension() != 0 ? r.servicedExtension() : r.service();
                        totalSales += price;
                        totalRooms += price;
                        totalTurn += price;
                        totalNet += price;
                        boolean found = false;
                        for (int j = 0; j < newSummary.size(); j++) {
                            TurnSummaryItemData si = newSummary.get(j);
                            if ("room".equals(si.summaryType()) && si.service() == serviceKey) {
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
                        int service = r.refundService();
                        boolean found = false;
                        for (int j = 0; j < newSummary.size(); j++) {
                            TurnSummaryItemData si = newSummary.get(j);
                            if ("roomRefund".equals(si.summaryType()) && si.service() == service) {
                                newSummary.set(j, new TurnSummaryItemData("roomRefund", si.quantity() + 1, si.price() + price, null, service));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newSummary.add(new TurnSummaryItemData("roomRefund", 1, price, null, service));
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

    public JSONObject toJson() {
        computeTotalsAndSummary();
        JSONObject json = new JSONObject();
        json.put("turnNumber", turnNumber);
        json.put("turnStart", turnStart.toString());
        if (turnEnd != null) {
            json.put("turnEnd", turnEnd.toString());
        }
        json.put("isTurnActive", isTurnActive);
        JSONArray arr = new JSONArray();
        for (TurnActivity a : activities) {
            arr.put(a.toJson());
        }
        json.put("turnActivity", arr);
        json.put("totalItems", totalItems);
        json.put("totalSales", totalSales);
        json.put("totalRooms", totalRooms);
        json.put("totalRefunds", totalRefunds);
        json.put("totalItemRefunds", totalItemRefunds);
        json.put("totalRoomRefunds", totalRoomRefunds);
        json.put("totalSpending", totalSpending);
        json.put("totalTurn", totalTurn);
        json.put("totalBankTransfers", totalBankTransfers);
        json.put("totalDeposits", totalDeposits);
        json.put("totalNet", totalNet);
        return json;
    }

    public static TurnDetails fromJson(JSONObject json) {
        TurnDetails details = new TurnDetails();
        details.turnNumber = json.getLong("turnNumber");
        details.turnStart = ZonedDateTime.parse(json.getString("turnStart"));
        if (json.has("turnEnd")) {
            details.turnEnd = ZonedDateTime.parse(json.getString("turnEnd"));
        }
        details.isTurnActive = json.getBoolean("isTurnActive");
        if (json.has("turnActivity")) {
            JSONArray arr = json.getJSONArray("turnActivity");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String type = obj.getString("changeType");
                TurnActivity activity = switch (type) {
                    case "room" -> RoomBookingActivity.fromJson(obj);
                    case "sale" -> SaleActivity.fromJson(obj);
                    case "roomSwap" -> RoomSwapActivity.fromJson(obj);
                    case "refund" -> RefundActivity.fromJson(obj);
                    case "spending" -> SpendingActivity.fromJson(obj);
                    case "extraChange" -> ExtraChangeActivity.fromJson(obj);
                    default -> throw new IllegalArgumentException("Unknown changeType: " + type);
                };
                details.activities.add(activity);
            }
        }
        details.computeTotalsAndSummary();
        return details;
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
