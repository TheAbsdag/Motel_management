package model;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Turn} — shift management, transaction logging,
 * summary/detail generation, and sale reversal.
 */
class TurnTest {

    private Turn turn;
    private ZoneId zoneID;
    private Instant startTime;

    @BeforeEach
    void setUp() {
        zoneID = ZoneId.of("America/Bogota");
        startTime = Instant.now();
        turn = new Turn(startTime, zoneID);
    }

    // ========== Turn Initialization ==========

    @Test
    void shouldStartInactive() {
        JSONObject details = turn.getDetailedTurnInformation();
        assertThat(details.optBoolean("isTurnActive", false)).isFalse();
    }

    @Test
    void shouldSetNewTurn() {
        turn.setNewTurn(2, startTime);

        JSONObject details = turn.getDetailedTurnInformation();
        assertThat(details.getLong("turnNumber")).isEqualTo(2);
        assertThat(details.getBoolean("isTurnActive")).isTrue();
        assertThat(details.has("turnStart")).isTrue();
    }

    // ========== Room Booking Logging ==========

    @Test
    void shouldRegisterRoomChangeForNewBooking() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 12);

        JSONObject change = turn.registerRoomChange(room, startTime, 50000, 0, 0);

        assertThat(change.getString("changeType")).isEqualTo("room");
        assertThat(change.getString("roomString")).isEqualTo("1-101");
        assertThat(change.getInt("roomStatus")).isEqualTo(RoomStatus.OCCUPIED.getCode());
        assertThat(change.getLong("price")).isEqualTo(50000);
        assertThat(change.getInt("service")).isEqualTo(12);
        assertThat(change.getInt("servicedExtension")).isZero();
    }

    @Test
    void shouldRegisterRoomChangeForExtension() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        room.extendRoomTime(3);

        JSONObject change = turn.registerRoomChange(room, startTime, 30000, 3, 0);

        assertThat(change.getInt("servicedExtension")).isEqualTo(3);
        assertThat(change.getInt("extension")).isEqualTo(3);
    }

    // ========== Sale Logging ==========

    @Test
    void shouldSaveTransactionInformation() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);

        JSONArray register = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("itemName", "Coca-Cola");
        item.put("itemID", 1);
        item.put("quantity", 3);
        item.put("price", 7500);
        register.put(item);

        JSONObject change = turn.saveTransactionInformation(register, room, startTime, 42);

        assertThat(change.getString("changeType")).isEqualTo("sale");
        assertThat(change.getString("roomSoldTo")).isEqualTo("1-101");
        assertThat(change.getInt("consecutiveTrans")).isEqualTo(42);
        assertThat(change.getJSONArray("register")).hasSize(1);
    }

    // ========== Summary Generation ==========

    @Test
    void shouldGenerateBasicTurnSummaryForBookings() {
        turn.setNewTurn(1, startTime);
        Room room1 = createRoom("1-101", 0, 1, 1);
        room1.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room1, startTime, 30000, 0, 0);

        JSONObject summary = turn.getBasicTurnInformation();

        assertThat(summary.getLong("totalRooms")).isEqualTo(30000);
        assertThat(summary.getLong("totalSales")).isEqualTo(30000);

        JSONArray summaryArray = summary.getJSONArray("turnSummary");
        assertThat(summaryArray).hasSize(1);
        JSONObject entry = summaryArray.getJSONObject(0);
        assertThat(entry.getString("summaryType")).isEqualTo("room");
        assertThat(entry.getInt("service")).isEqualTo(3);
        assertThat(entry.getInt("quantity")).isEqualTo(1);
    }

    @Test
    void shouldGroupSameServiceBookingsInSummary() {
        turn.setNewTurn(1, startTime);

        Room room1 = createRoom("1-101", 0, 1, 1);
        room1.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room1, startTime, 30000, 0, 0);

        Room room2 = createRoom("1-102", 0, 2, 1);
        room2.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room2, startTime, 30000, 0, 0);

        JSONObject summary = turn.getBasicTurnInformation();
        JSONArray summaryArray = summary.getJSONArray("turnSummary");

        assertThat(summaryArray).hasSize(1);
        JSONObject entry = summaryArray.getJSONObject(0);
        assertThat(entry.getInt("quantity")).isEqualTo(2);
        assertThat(entry.getLong("price")).isEqualTo(60000);
    }

    @Test
    void shouldIncludeItemSalesInSummary() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);

        JSONArray register = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("itemName", "Coca-Cola");
        item.put("itemID", 1);
        item.put("quantity", 5);
        item.put("price", 12500);
        register.put(item);

        turn.saveTransactionInformation(register, room, startTime, 1);

        JSONObject summary = turn.getBasicTurnInformation();
        assertThat(summary.getLong("totalItems")).isEqualTo(12500);
        assertThat(summary.getLong("totalSales")).isEqualTo(12500);

        JSONArray summaryArray = summary.getJSONArray("turnSummary");
        assertThat(summaryArray).hasSize(1);
        JSONObject entry = summaryArray.getJSONObject(0);
        assertThat(entry.getString("summaryType")).isEqualTo("item");
        assertThat(entry.getString("itemName")).isEqualTo("Coca-Cola");
    }

    // ========== Detailed Turn Info ==========

    @Test
    void shouldGenerateDetailedTotals() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room, startTime, 30000, 0, 0);

        JSONObject detailed = turn.getDetailedTurnInformation();
        assertThat(detailed.getLong("totalRooms")).isEqualTo(30000);
        assertThat(detailed.getLong("totalSales")).isEqualTo(30000);
    }

    // ========== Turn End ==========

    @Test
    void shouldEndTurnAndSetInactive() {
        turn.setNewTurn(1, startTime);
        Instant endTime = startTime.plus(Duration.ofHours(8));

        turn.turnEnd(endTime);

        JSONObject details = turn.getDetailedTurnInformation();
        assertThat(details.getBoolean("isTurnActive")).isFalse();
        assertThat(details.has("turnEnd")).isTrue();
    }

    @Test
    void shouldHandleSummaryBeforeTurnEnd() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room, startTime, 30000, 0, 0);

        // Summary before end should handle missing turnEnd gracefully
        JSONObject summary = turn.getBasicTurnInformation();
        assertThat(summary.optString("turnEnd", "not finished")).isEqualTo("not finished");
    }

    // ========== Activity Data DTOs ==========

    @Test
    void shouldConvertActivityToTypedDtoList() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 12);
        turn.registerRoomChange(room, startTime, 50000, 0, 0);

        List<TurnActivityData> activities = turn.getActivityDataList();

        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("room");
        assertThat(activities.get(0).getPrice()).isEqualTo(50000);
        assertThat(activities.get(0).getService()).isEqualTo(12);
        assertThat(activities.get(0).getEffectiveService()).isEqualTo(12);
    }

    @Test
    void shouldConvertSaleActivityToTypedDto() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);

        JSONArray register = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("itemName", "Coca-Cola");
        item.put("itemID", 1);
        item.put("quantity", 3);
        item.put("price", 7500);
        register.put(item);

        turn.saveTransactionInformation(register, room, startTime, 42);

        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("sale");
        assertThat(activities.get(0).getItemName()).isEqualTo("Coca-Cola");
        assertThat(activities.get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void shouldConvertSummaryToTypedDtoList() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room, startTime, 30000, 0, 0);

        List<TurnSummaryItemData> summaries = turn.getSummaryDataList();

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).summaryType()).isEqualTo("room");
        assertThat(summaries.get(0).service()).isEqualTo(3);
        assertThat(summaries.get(0).quantity()).isEqualTo(1);
    }

    // ========== Sale Reversal ==========

    @Test
    void shouldReverseItemSaleFromTurn() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);

        JSONArray register = new JSONArray();
        JSONObject item1 = new JSONObject();
        item1.put("itemName", "Coca-Cola");
        item1.put("itemID", 1);
        item1.put("quantity", 3);
        item1.put("price", 7500);
        register.put(item1);

        JSONObject transaction = turn.saveTransactionInformation(register, room, startTime, 42);

        // Build reversal JSON
        JSONObject reversal = new JSONObject();
        reversal.put("roomSoldTo", transaction.getString("roomSoldTo"));
        reversal.put("changeDate", transaction.getString("changeDate"));
        reversal.put("itemID", 1L);
        reversal.put("quantity", 3L);

        turn.reverseItemSaleFromTurn(reversal);

        // After reversal, the sale entry should have no register items
        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).isEmpty();
    }

    // ========== Room Swap ==========

    @Test
    void shouldRegisterRoomSwap() {
        turn.setNewTurn(1, startTime);
        Room original = createRoom("1-101", 0, 1, 1);
        Room swapped = createRoom("1-102", 0, 2, 1);

        turn.registerRoomSwap(original, swapped, startTime);

        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("roomSwap");
        assertThat(activities.get(0).getOriginalRoom()).isEqualTo("1-101");
        assertThat(activities.get(0).getSwappedRoom()).isEqualTo("1-102");
    }

    // ========== Previous Turn Restoration ==========

    @Test
    void shouldRestoreActiveTurnFromJson() {
        JSONObject previousTurn = new JSONObject();
        previousTurn.put("turnNumber", 3);
        previousTurn.put("turnStart", startTime.atZone(zoneID).toString());
        previousTurn.put("isTurnActive", true);
        previousTurn.put("turnActivity", new JSONArray());

        boolean active = turn.setPreviousTurnJSON(previousTurn);

        assertThat(active).isTrue();
        assertThat(turn.getTurnNumber()).isEqualTo(3);
    }

    @Test
    void shouldRestoreInactiveTurnFromJson() {
        JSONObject previousTurn = new JSONObject();
        previousTurn.put("turnNumber", 2);
        previousTurn.put("turnStart", startTime.atZone(zoneID).toString());
        previousTurn.put("isTurnActive", false);
        previousTurn.put("turnActivity", new JSONArray());

        boolean active = turn.setPreviousTurnJSON(previousTurn);

        assertThat(active).isFalse();
    }

    // ========== Spending Transactions ==========

    @Test
    void shouldRegisterSpendingTransaction() {
        turn.setNewTurn(1, startTime);

        // MotelManagement negates the value before passing to Turn
        turn.registerSpendingTransaction("Compra de insumos", -50000, 1, startTime);

        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("spending");
        assertThat(activities.get(0).getDescription()).isEqualTo("Compra de insumos");
        assertThat(activities.get(0).getPrice()).isEqualTo(-50000);
    }

    @Test
    void shouldIncludeSpendingInFinancialTotals() {
        turn.setNewTurn(1, startTime);
        turn.registerSpendingTransaction("Gasto varios", -20000, 1, startTime);

        JSONObject basic = turn.getBasicTurnInformation();
        assertThat(basic.getLong("totalSpending")).isEqualTo(-20000);
        assertThat(basic.getLong("totalTurn")).isEqualTo(-20000);
        assertThat(basic.getLong("totalNet")).isEqualTo(-20000);

        JSONObject detailed = turn.getDetailedTurnInformation();
        assertThat(detailed.getLong("totalSpending")).isEqualTo(-20000);
        assertThat(detailed.getLong("totalTurn")).isEqualTo(-20000);
        assertThat(detailed.getLong("totalNet")).isEqualTo(-20000);
    }

    // ========== Extra Change Transactions ==========

    @Test
    void shouldRegisterExtraChangeBankTransfer() {
        turn.setNewTurn(1, startTime);

        // MotelManagement negates the value before passing to Turn
        turn.registerExtraChangeTransaction("Transferencia recibida", -100000, "bankTransfer", 1, startTime);

        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("extraChange");
        assertThat(activities.get(0).getExtraType()).isEqualTo("bankTransfer");
        assertThat(activities.get(0).getDescription()).isEqualTo("Transferencia recibida");
        assertThat(activities.get(0).getPrice()).isEqualTo(-100000);
    }

    @Test
    void shouldRegisterExtraChangeSafeDeposit() {
        turn.setNewTurn(1, startTime);

        // MotelManagement negates the value before passing to Turn
        turn.registerExtraChangeTransaction("Abono efectivo", -50000, "safeDeposit", 1, startTime);

        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("extraChange");
        assertThat(activities.get(0).getExtraType()).isEqualTo("safeDeposit");
        assertThat(activities.get(0).getPrice()).isEqualTo(-50000);
    }

    @Test
    void shouldIncludeExtraChangesInFinancialTotals() {
        turn.setNewTurn(1, startTime);
        // MotelManagement negates values before passing to Turn
        turn.registerExtraChangeTransaction("Transf Bancaria", -100000, "bankTransfer", 1, startTime);
        turn.registerExtraChangeTransaction("Abono caja", -50000, "safeDeposit", 2, startTime);

        JSONObject basic = turn.getBasicTurnInformation();
        // totalBankTransfers = value * -1L: -100000 * -1 = 100000
        assertThat(basic.getLong("totalBankTransfers")).isEqualTo(100000);
        // totalDeposits = value * -1L: -50000 * -1 = 50000
        assertThat(basic.getLong("totalDeposits")).isEqualTo(50000);
        // totalNet: -100000 + (-50000) = -150000
        assertThat(basic.getLong("totalNet")).isEqualTo(-150000);

        JSONObject detailed = turn.getDetailedTurnInformation();
        assertThat(detailed.getLong("totalBankTransfers")).isEqualTo(100000);
        assertThat(detailed.getLong("totalDeposits")).isEqualTo(50000);
        assertThat(detailed.getLong("totalNet")).isEqualTo(-150000);
    }

    @Test
    void shouldGroupExtraChangesInSummary() {
        turn.setNewTurn(1, startTime);
        // MotelManagement negates values before passing to Turn
        turn.registerExtraChangeTransaction("Transf 1", -50000, "bankTransfer", 1, startTime);
        turn.registerExtraChangeTransaction("Transf 2", -30000, "bankTransfer", 2, startTime);

        List<TurnSummaryItemData> summaries = turn.getSummaryDataList();
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).summaryType()).isEqualTo("extraChange");
        assertThat(summaries.get(0).quantity()).isEqualTo(2);
        assertThat(summaries.get(0).price()).isEqualTo(-80000);
    }

    // ========== Refund Transactions ==========

    @Test
    void shouldRefundRoomTransactionFromTurn() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        JSONObject change = turn.registerRoomChange(room, startTime, 30000, 0, 42);

        turn.refundTransactionFromTurn(change, 43, startTime);

        List<TurnActivityData> activities = turn.getActivityDataList();
        // Should have: room booking + room refund
        assertThat(activities).hasSize(2);
        TurnActivityData refund = activities.get(1);
        assertThat(refund.getChangeType()).isEqualTo("refund");
        assertThat(refund.getRefundType()).isEqualTo("roomRefund");
        assertThat(refund.getPrice()).isEqualTo(-30000);
    }

    @Test
    void shouldRefundSaleTransactionFromTurn() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);

        JSONArray register = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("itemName", "Coca-Cola");
        item.put("itemID", 1);
        item.put("quantity", 3);
        item.put("price", 7500);
        register.put(item);

        JSONObject transaction = turn.saveTransactionInformation(register, room, startTime, 42);

        // Build the filtered-transaction-style JSON for refund
        JSONObject filteredTransaction = new JSONObject();
        filteredTransaction.put("changeType", "sale");
        filteredTransaction.put("consecutiveTrans", 42);
        filteredTransaction.put("roomSoldTo", transaction.getString("roomSoldTo"));
        filteredTransaction.put("itemID", 1L);
        filteredTransaction.put("quantity", 3L);
        filteredTransaction.put("itemName", "Coca-Cola");
        filteredTransaction.put("price", 7500);

        turn.refundTransactionFromTurn(filteredTransaction, 43, startTime);

        List<TurnActivityData> activities = turn.getActivityDataList();
        // Should have: sale + sale refund
        assertThat(activities).hasSize(2);
        TurnActivityData refund = activities.get(1);
        assertThat(refund.getChangeType()).isEqualTo("refund");
        assertThat(refund.getRefundType()).isEqualTo("saleRefund");
        assertThat(refund.getPrice()).isEqualTo(-7500);
    }

    @Test
    void shouldIncludeRefundsInFinancialTotals() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        JSONObject roomChange = turn.registerRoomChange(room, startTime, 30000, 0, 42);

        turn.refundTransactionFromTurn(roomChange, 43, startTime);

        JSONObject basic = turn.getBasicTurnInformation();
        assertThat(basic.getLong("totalRooms")).isEqualTo(30000);
        assertThat(basic.getLong("totalRefunds")).isEqualTo(-30000);
        assertThat(basic.getLong("totalNet")).isEqualTo(0);  // 30000 + (-30000)
    }

    // ========== Net Total Calculation ==========

    @Test
    void shouldCalculateNetTotalAcrossAllTransactionTypes() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        // Room booking: +30000
        turn.registerRoomChange(room, startTime, 30000, 0, 1);
        // Spending: -5000 (MotelManagement negates user-entered value)
        turn.registerSpendingTransaction("Gasto", -5000, 2, startTime);
        // Bank transfer: -20000 (MotelManagement negates user-entered value)
        turn.registerExtraChangeTransaction("Transf", -20000, "bankTransfer", 3, startTime);

        JSONObject basic = turn.getBasicTurnInformation();
        assertThat(basic.getLong("totalNet")).isEqualTo(5000); // 30000 + (-5000) + (-20000)
    }

    // ========== Activity DTO: New Types ==========

    @Test
    void shouldConvertSpendingActivityToDto() {
        turn.setNewTurn(1, startTime);
        turn.registerSpendingTransaction("Gasto insumos", -30000, 1, startTime);

        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("spending");
        assertThat(activities.get(0).getDescription()).isEqualTo("Gasto insumos");
        assertThat(activities.get(0).getPrice()).isEqualTo(-30000);
    }

    @Test
    void shouldConvertExtraChangeActivityToDto() {
        turn.setNewTurn(1, startTime);
        // MotelManagement negates the value before passing to Turn
        turn.registerExtraChangeTransaction("Abono", -50000, "safeDeposit", 1, startTime);

        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("extraChange");
        assertThat(activities.get(0).getExtraType()).isEqualTo("safeDeposit");
        assertThat(activities.get(0).getDescription()).isEqualTo("Abono");
        assertThat(activities.get(0).getPrice()).isEqualTo(-50000);
    }

    // ========== Helper ==========

    private static Room createRoom(String roomString, int floor, int roomNumber, int tower) {
        return new Room(roomString, floor, roomNumber, tower);
    }
}
