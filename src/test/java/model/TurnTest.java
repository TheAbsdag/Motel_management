package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.json.ObjectMapperFactory;
import model.turn.TurnDetails;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import model.turn.ActivityType;
import model.turn.ExtraChangeType;
import model.turn.RoomBookingActivity;
import model.turn.SaleActivity;
import model.turn.TurnActivity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void shouldRegisterRoomChangeForNewBooking() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 43200L);
        RoomBookingActivity change = turn.registerRoomChange(room, startTime, 50000, 0, 0);
        assertThat(change.roomString()).isEqualTo("1-101");
        assertThat(change.roomStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(change.price()).isEqualTo(50000);
        assertThat(change.serviceDuration()).isEqualTo(43200L);
        assertThat(change.servicedExtensionDuration()).isZero();
    }

    @Test
    void shouldRegisterRoomChangeForExtension() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        room.extendRoomTime(10800L);
        RoomBookingActivity change = turn.registerRoomChange(room, startTime, 30000, 10800L, 0);
        assertThat(change.servicedExtensionDuration()).isEqualTo(10800L);
        assertThat(change.extensionDuration()).isEqualTo(10800L);
    }

    @Test
    void shouldSaveTransactionInformation() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        List<CartItem> register = new ArrayList<>();
        register.add(new CartItem(1L, "Coca-Cola", 3L, 7500L));
        SaleActivity change = turn.saveTransactionInformation(register, room, startTime, 42);
        assertThat(change.roomSoldTo()).isEqualTo("1-101");
        assertThat(change.consecutiveTrans()).isEqualTo(42);
        assertThat(change.items()).hasSize(1);
    }

    @Test
    void shouldGenerateBasicTurnSummaryForBookings() {
        turn.setNewTurn(1, startTime);
        Room room1 = createRoom("1-101", 0, 1, 1);
        room1.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        turn.registerRoomChange(room1, startTime, 30000, 0, 0);
        TurnDetails summary = turn.getDetailedTurnInformation();
        assertThat(summary.getTotalRooms()).isEqualTo(30000);
        assertThat(summary.getTotalSales()).isEqualTo(30000);
        List<TurnSummaryItemData> summaryItems = summary.getSummaryItems();
        assertThat(summaryItems).hasSize(1);
        assertThat(summaryItems.get(0).summaryType()).isEqualTo("room");
        assertThat(summaryItems.get(0).serviceDuration()).isEqualTo(10800L);
        assertThat(summaryItems.get(0).quantity()).isEqualTo(1);
    }

    @Test
    void shouldGroupSameServiceBookingsInSummary() {
        turn.setNewTurn(1, startTime);
        Room room1 = createRoom("1-101", 0, 1, 1);
        room1.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        turn.registerRoomChange(room1, startTime, 30000, 0, 0);
        Room room2 = createRoom("1-102", 0, 2, 1);
        room2.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        turn.registerRoomChange(room2, startTime, 30000, 0, 0);
        TurnDetails summary = turn.getDetailedTurnInformation();
        List<TurnSummaryItemData> summaryItems = summary.getSummaryItems();
        assertThat(summaryItems).hasSize(1);
        assertThat(summaryItems.get(0).quantity()).isEqualTo(2);
        assertThat(summaryItems.get(0).price()).isEqualTo(60000);
    }

    @Test
    void shouldIncludeItemSalesInSummary() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        List<CartItem> register2 = new ArrayList<>();
        register2.add(new CartItem(1L, "Coca-Cola", 5L, 12500L));
        turn.saveTransactionInformation(register2, room, startTime, 1);
        TurnDetails summary = turn.getDetailedTurnInformation();
        assertThat(summary.getTotalItems()).isEqualTo(12500);
        assertThat(summary.getTotalSales()).isEqualTo(12500);
        List<TurnSummaryItemData> summaryItems = summary.getSummaryItems();
        assertThat(summaryItems).hasSize(1);
        assertThat(summaryItems.get(0).summaryType()).isEqualTo("item");
        assertThat(summaryItems.get(0).name()).isEqualTo("Coca-Cola");
    }

    @Test
    void shouldGenerateDetailedTotals() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        turn.registerRoomChange(room, startTime, 30000, 0, 0);
        TurnDetails detailed = turn.getDetailedTurnInformation();
        assertThat(detailed.getTotalRooms()).isEqualTo(30000);
        assertThat(detailed.getTotalSales()).isEqualTo(30000);
    }

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
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        turn.registerRoomChange(room, startTime, 30000, 0, 0);
        TurnDetails summary = turn.getDetailedTurnInformation();
        assertThat(summary.getTurnEnd()).isNull();
    }

    @Test
    void shouldConvertActivityToTypedDtoList() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 43200L);
        turn.registerRoomChange(room, startTime, 50000, 0, 0);
        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("room");
        assertThat(activities.get(0).getPrice()).isEqualTo(50000);
        assertThat(activities.get(0).getServiceDuration()).isEqualTo(43200L);
        assertThat(activities.get(0).getEffectiveServiceDuration()).isEqualTo(43200L);
    }

    @Test
    void shouldConvertSaleActivityToTypedDto() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        List<CartItem> register = new ArrayList<>();
        register.add(new CartItem(1L, "Coca-Cola", 3L, 7500L));
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
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        turn.registerRoomChange(room, startTime, 30000, 0, 0);
        List<TurnSummaryItemData> summaries = turn.getSummaryDataList();
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).summaryType()).isEqualTo("room");
        assertThat(summaries.get(0).serviceDuration()).isEqualTo(10800L);
        assertThat(summaries.get(0).quantity()).isEqualTo(1);
    }

    @Test
    void shouldReverseItemSaleFromTurn() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        List<CartItem> register = new ArrayList<>();
        register.add(new CartItem(1L, "Coca-Cola", 3L, 7500L));
        turn.saveTransactionInformation(register, room, startTime, 42);
        TurnActivity activity = turn.findActivity(42, ActivityType.SALE);
        assertThat(activity).isNotNull();
        turn.reverseItemSaleFromTurn(activity, 1L, 3L);
        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).isEmpty();
    }

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

    @Test
    void shouldRestoreActiveTurnFromJson() throws JsonProcessingException {
        ObjectNode previousTurn = ObjectMapperFactory.get().createObjectNode();
        previousTurn.put("turnNumber", 3);
        previousTurn.put("turnStart", startTime.atZone(zoneID).toString());
        previousTurn.put("isTurnActive", true);
        previousTurn.set("turnActivity", ObjectMapperFactory.get().createArrayNode());

        String json = ObjectMapperFactory.get().writeValueAsString(previousTurn);
        boolean active = turn.setPreviousTurnJSON(json);

        assertThat(active).isTrue();
        assertThat(turn.getTurnNumber()).isEqualTo(3);
    }

    @Test
    void shouldRestoreInactiveTurnFromJson() throws JsonProcessingException {
        ObjectNode previousTurn = ObjectMapperFactory.get().createObjectNode();
        previousTurn.put("turnNumber", 2);
        previousTurn.put("turnStart", startTime.atZone(zoneID).toString());
        previousTurn.put("isTurnActive", false);
        previousTurn.set("turnActivity", ObjectMapperFactory.get().createArrayNode());

        String json = ObjectMapperFactory.get().writeValueAsString(previousTurn);
        boolean active = turn.setPreviousTurnJSON(json);

        assertThat(active).isFalse();
    }

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
        TurnDetails basic = turn.getDetailedTurnInformation();
        assertThat(basic.getTotalSpending()).isEqualTo(-20000);
        assertThat(basic.getTotalTurn()).isEqualTo(-20000);
        assertThat(basic.getTotalNet()).isEqualTo(-20000);
    }

    @Test
    void shouldRegisterExtraChangeBankTransfer() {
        turn.setNewTurn(1, startTime);
        turn.registerExtraChangeTransaction("Transferencia recibida", -100000, ExtraChangeType.BANK_TRANSFER, 1, startTime);
        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("extraChange");
        assertThat(activities.get(0).getExtraType()).isEqualTo("bankTransfer");
    }

    @Test
    void shouldRegisterExtraChangeSafeDeposit() {
        turn.setNewTurn(1, startTime);
        turn.registerExtraChangeTransaction("Abono efectivo", -50000, ExtraChangeType.SAFE_DEPOSIT, 1, startTime);
        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("extraChange");
        assertThat(activities.get(0).getExtraType()).isEqualTo("safeDeposit");
    }

    @Test
    void shouldIncludeExtraChangesInFinancialTotals() {
        turn.setNewTurn(1, startTime);
        turn.registerExtraChangeTransaction("Transf Bancaria", -100000, ExtraChangeType.BANK_TRANSFER, 1, startTime);
        turn.registerExtraChangeTransaction("Abono caja", -50000, ExtraChangeType.SAFE_DEPOSIT, 2, startTime);
        TurnDetails detailed = turn.getDetailedTurnInformation();
        assertThat(detailed.getTotalBankTransfers()).isEqualTo(100000);
        assertThat(detailed.getTotalDeposits()).isEqualTo(50000);
        assertThat(detailed.getTotalNet()).isEqualTo(-150000);
    }

    @Test
    void shouldGroupExtraChangesInSummary() {
        turn.setNewTurn(1, startTime);
        turn.registerExtraChangeTransaction("Transf 1", -50000, ExtraChangeType.BANK_TRANSFER, 1, startTime);
        turn.registerExtraChangeTransaction("Transf 2", -30000, ExtraChangeType.BANK_TRANSFER, 2, startTime);
        List<TurnSummaryItemData> summaries = turn.getSummaryDataList();
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).summaryType()).isEqualTo("extraChange");
        assertThat(summaries.get(0).quantity()).isEqualTo(2);
        assertThat(summaries.get(0).price()).isEqualTo(-80000);
    }

    @Test
    void shouldRefundRoomTransactionFromTurn() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        turn.registerRoomChange(room, startTime, 30000, 0, 42);
        TurnActivity activity = turn.findActivity(42, ActivityType.ROOM);
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
        List<CartItem> register = new ArrayList<>();
        register.add(new CartItem(1L, "Coca-Cola", 3L, 7500L));
        turn.saveTransactionInformation(register, room, startTime, 42);
        TurnActivity activity = turn.findActivity(42, ActivityType.SALE);
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
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        turn.registerRoomChange(room, startTime, 30000, 0, 42);
        TurnActivity activity = turn.findActivity(42, ActivityType.ROOM);
        turn.refundTransactionFromTurn(activity, 43, startTime, 0, 0);
        TurnDetails basic = turn.getDetailedTurnInformation();
        assertThat(basic.getTotalRooms()).isEqualTo(30000);
        assertThat(basic.getTotalRefunds()).isEqualTo(-30000);
        assertThat(basic.getTotalNet()).isEqualTo(0);
    }

    @Test
    void shouldCalculateNetTotalAcrossAllTransactionTypes() {
        turn.setNewTurn(1, startTime);
        Room room = createRoom("1-101", 0, 1, 1);
        room.setRoomStatus(RoomStatus.OCCUPIED, startTime, 10800L);
        turn.registerRoomChange(room, startTime, 30000, 0, 1);
        turn.registerSpendingTransaction("Gasto", -5000, 2, startTime);
        turn.registerExtraChangeTransaction("Transf", -20000, ExtraChangeType.BANK_TRANSFER, 3, startTime);
        TurnDetails basic = turn.getDetailedTurnInformation();
        assertThat(basic.getTotalNet()).isEqualTo(5000);
    }

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
        turn.registerExtraChangeTransaction("Abono", -50000, ExtraChangeType.SAFE_DEPOSIT, 1, startTime);
        List<TurnActivityData> activities = turn.getActivityDataList();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getChangeType()).isEqualTo("extraChange");
        assertThat(activities.get(0).getExtraType()).isEqualTo("safeDeposit");
        assertThat(activities.get(0).getDescription()).isEqualTo("Abono");
        assertThat(activities.get(0).getPrice()).isEqualTo(-50000);
    }

    private static Room createRoom(String roomString, int floor, int roomNumber, int tower) {
        return new Room(roomString, floor, roomNumber, tower);
    }
}
