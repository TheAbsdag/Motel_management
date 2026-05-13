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
import model.turn.RoomBookingActivity;
import model.turn.SaleActivity;
import model.turn.TurnActivity;
import model.turn.TurnDetails;

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
        TurnDetails details = turn.getDetailedTurnInformation();
        assertThat(details.isTurnActive()).isFalse();
    }

    @Test
    void shouldSetNewTurn() {
        turn.setNewTurn(2, startTime);

        TurnDetails details = turn.getDetailedTurnInformation();
        assertThat(details.getTurnNumber()).isEqualTo(2);
        assertThat(details.isTurnActive()).isTrue();
        assertThat(details.getTurnStart()).isNotNull();
    }

    // ========== Room Booking Logging ==========

    @Test
    void shouldRegisterRoomChangeForNewBooking() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 12);

        RoomBookingActivity change = turn.registerRoomChange(room, startTime, 50000, 0, 0);

        assertThat(change.roomString()).isEqualTo("1-101");
        assertThat(change.roomStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(change.price()).isEqualTo(50000);
        assertThat(change.service()).isEqualTo(12);
        assertThat(change.servicedExtension()).isZero();
    }

    @Test
    void shouldRegisterRoomChangeForExtension() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        room.extendRoomTime(3);

        RoomBookingActivity change = turn.registerRoomChange(room, startTime, 30000, 3, 0);

        assertThat(change.servicedExtension()).isEqualTo(3);
        assertThat(change.extension()).isEqualTo(3);
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

        SaleActivity change = turn.saveTransactionInformation(register, room, startTime, 42);

        assertThat(change.roomSoldTo()).isEqualTo("1-101");
        assertThat(change.consecutiveTrans()).isEqualTo(42);
        assertThat(change.items()).hasSize(1);
    }

    // ========== Summary Generation ==========

    @Test
    void shouldGenerateBasicTurnSummaryForBookings() {
        turn.setNewTurn(1, startTime);
        Room room1 = createRoom("1-101", 0, 1, 1);
        room1.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room1, startTime, 30000, 0, 0);

        TurnDetails summary = turn.getBasicTurnInformation();

        assertThat(summary.getTotalRooms()).isEqualTo(30000);
        assertThat(summary.getTotalSales()).isEqualTo(30000);

        List<TurnSummaryItemData> summaryItems = summary.getSummaryItems();
        assertThat(summaryItems).hasSize(1);
        assertThat(summaryItems.get(0).summaryType()).isEqualTo("room");
        assertThat(summaryItems.get(0).service()).isEqualTo(3);
        assertThat(summaryItems.get(0).quantity()).isEqualTo(1);
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

        TurnDetails summary = turn.getBasicTurnInformation();
        List<TurnSummaryItemData> summaryItems = summary.getSummaryItems();

        assertThat(summaryItems).hasSize(1);
        assertThat(summaryItems.get(0).quantity()).isEqualTo(2);
        assertThat(summaryItems.get(0).price()).isEqualTo(60000);
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

        TurnDetails summary = turn.getBasicTurnInformation();
        assertThat(summary.getTotalItems()).isEqualTo(12500);
        assertThat(summary.getTotalSales()).isEqualTo(12500);

        List<TurnSummaryItemData> summaryItems = summary.getSummaryItems();
        assertThat(summaryItems).hasSize(1);
        assertThat(summaryItems.get(0).summaryType()).isEqualTo("item");
        assertThat(summaryItems.get(0).name()).isEqualTo("Coca-Cola");
    }

    // ========== Detailed Turn Info ==========

    @Test
    void shouldGenerateDetailedTotals() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room, startTime, 30000, 0, 0);

        TurnDetails detailed = turn.getDetailedTurnInformation();
        assertThat(detailed.getTotalRooms()).isEqualTo(30000);
        assertThat(detailed.getTotalSales()).isEqualTo(30000);
    }

    // ========== Turn End ==========

    @Test
    void shouldEndTurnAndSetInactive() {
        turn.setNewTurn(1, startTime);
        Instant endTime = startTime.plus(Duration.ofHours(8));

        turn.turnEnd(endTime);

        TurnDetails details = turn.getDetailedTurnInformation();
        assertThat(details.isTurnActive()).isFalse();
        assertThat(details.getTurnEnd()).isNotNull();
    }

    @Test
    void shouldHandleSummaryBeforeTurnEnd() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room, startTime, 30000, 0, 0);

        TurnDetails summary = turn.getBasicTurnInformation();
        assertThat(summary.getTurnEnd()).isNull();
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

        turn.saveTransactionInformation(register, room, startTime, 42);

        TurnActivity activity = turn.findActivity(42, "sale");
        assertThat(activity).isNotNull();

        turn.reverseItemSaleFromTurn(activity, 1L, 3L);

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

        TurnDetails basic = turn.getBasicTurnInformation();
        assertThat(basic.getTotalSpending()).isEqualTo(-20000);
        assertThat(basic.getTotalTurn()).isEqualTo(-20000);
        assertThat(basic.getTotalNet()).isEqualTo(-20000);

        TurnDetails detailed = turn.getDetailedTurnInformation();
        assertThat(detailed.getTotalSpending()).isEqualTo(-20000);
        assertThat(detailed.getTotalTurn()).isEqualTo(-20000);
        assertThat(detailed.getTotalNet()).isEqualTo(-20000);
    }

    // ========== Extra Change Transactions ==========

    @Test
    void shouldRegisterExtraChangeBankTransfer() {
        turn.setNewTurn(1, startTime);

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
        turn.registerExtraChangeTransaction("Transf Bancaria", -100000, "bankTransfer", 1, startTime);
        turn.registerExtraChangeTransaction("Abono caja", -50000, "safeDeposit", 2, startTime);

        TurnDetails basic = turn.getBasicTurnInformation();
        assertThat(basic.getTotalBankTransfers()).isEqualTo(100000);
        assertThat(basic.getTotalDeposits()).isEqualTo(50000);
        assertThat(basic.getTotalNet()).isEqualTo(-150000);

        TurnDetails detailed = turn.getDetailedTurnInformation();
        assertThat(detailed.getTotalBankTransfers()).isEqualTo(100000);
        assertThat(detailed.getTotalDeposits()).isEqualTo(50000);
        assertThat(detailed.getTotalNet()).isEqualTo(-150000);
    }

    @Test
    void shouldGroupExtraChangesInSummary() {
        turn.setNewTurn(1, startTime);
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
        turn.registerRoomChange(room, startTime, 30000, 0, 42);

        TurnActivity activity = turn.findActivity(42, "room");
        assertThat(activity).isNotNull();

        turn.refundTransactionFromTurn(activity, 43, startTime, 0, 0);

        List<TurnActivityData> activities = turn.getActivityDataList();
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

        turn.saveTransactionInformation(register, room, startTime, 42);

        TurnActivity activity = turn.findActivity(42, "sale");
        assertThat(activity).isNotNull();

        turn.refundTransactionFromTurn(activity, 43, startTime, 1L, 3L);

        List<TurnActivityData> activities = turn.getActivityDataList();
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
        turn.registerRoomChange(room, startTime, 30000, 0, 42);

        TurnActivity activity = turn.findActivity(42, "room");
        turn.refundTransactionFromTurn(activity, 43, startTime, 0, 0);

        TurnDetails basic = turn.getBasicTurnInformation();
        assertThat(basic.getTotalRooms()).isEqualTo(30000);
        assertThat(basic.getTotalRefunds()).isEqualTo(-30000);
        assertThat(basic.getTotalNet()).isEqualTo(0);
    }

    // ========== Net Total Calculation ==========

    @Test
    void shouldCalculateNetTotalAcrossAllTransactionTypes() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 3);
        turn.registerRoomChange(room, startTime, 30000, 0, 1);
        turn.registerSpendingTransaction("Gasto", -5000, 2, startTime);
        turn.registerExtraChangeTransaction("Transf", -20000, "bankTransfer", 3, startTime);

        TurnDetails basic = turn.getBasicTurnInformation();
        assertThat(basic.getTotalNet()).isEqualTo(5000);
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
