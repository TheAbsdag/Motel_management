package model.print;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import model.RoomStatus;
import model.json.CurrencyConfig;
import model.turn.RoomBookingActivity;
import model.turn.SaleActivity;
import model.turn.SaleItem;
import model.turn.TurnDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import view.helpers.CurrencyFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PrintDataBuilder}: the data maps that resolve template fields.
 */
class PrintDataBuilderTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final ZonedDateTime START = ZonedDateTime.of(2026, 9, 15, 20, 30, 0, 0, BOGOTA);
    private static final CurrencyConfig CURRENCY = CurrencyConfig.defaultConfig();
    private static final PrintDataBuilder.PrintContext CONTEXT =
            new PrintDataBuilder.PrintContext("MOTEL TEST", "Calle 1 #2-3", "900-1", CURRENCY);

    /**
     * Verifies that templates can only reference fields the builders actually produce.
     * Expected: for every type the built data contains all keys of the field registry.
     * Failure: the editor offers a field that always renders empty when printing.
     */
    @ParameterizedTest
    @EnumSource(PrintTemplateType.class)
    void shouldFillEveryRegisteredField(PrintTemplateType type) {
        Map<String, String> data = buildFor(type);

        assertThat(data.keySet()).containsAll(PrintFieldRegistry.getFieldsForType(type).stream()
                .map(PrintFieldRegistry.FieldDef::dataKey)
                .toList());
        assertThat(data.values()).doesNotContainNull();
    }

    /**
     * Verifies room receipt values resolve to the printer's own formatting.
     * Expected: price, room and motel data are formatted exactly like the built-in receipt.
     * Failure: custom receipts print raw numbers or the wrong room.
     */
    @Test
    void shouldFormatRoomReceiptValues() {
        Map<String, String> data = PrintDataBuilder.roomReceipt(roomBooking(), 77, CONTEXT);

        assertThat(data.get("totalPrice")).isEqualTo(CurrencyFormatter.format(40_000L, CURRENCY));
        assertThat(data.get("roomString")).isEqualTo("Habitacion 3");
        assertThat(data.get("consecutive")).isEqualTo("77");
        assertThat(data.get("entryTime")).isEqualTo("08:30 PM");
        assertThat(data.get("date")).isEqualTo("2026-09-15");
        assertThat(data.get("serviceDuration")).isEqualTo("3h");
        assertThat(data.get("motelName")).isEqualTo("MOTEL TEST");
    }

    /**
     * Verifies that a room without extension leaves the extension field empty so the
     * template line is skipped instead of printing a zero.
     * Expected: extensionDuration is an empty string.
     * Failure: receipts show "Extension: 0s" for bookings without extension.
     */
    @Test
    void shouldLeaveExtensionEmptyWhenNotExtended() {
        assertThat(PrintDataBuilder.roomReceipt(roomBooking(), 77, CONTEXT).get("extensionDuration")).isEmpty();
    }

    /**
     * Verifies the sale receipt item block.
     * Expected: one line per item, total is the sum of item prices.
     * Failure: sold items are missing from custom sale receipts.
     */
    @Test
    void shouldListSoldItems() {
        Map<String, String> data = PrintDataBuilder.saleReceipt(sale(), 88, CONTEXT);

        assertThat(data.get("items")).contains("Cerveza").contains("Agua");
        assertThat(data.get("items")).contains(CurrencyFormatter.format(8_000L, CURRENCY));
        assertThat(data.get("totalPrice")).isEqualTo(CurrencyFormatter.format(11_000L, CURRENCY));
        assertThat(data.get("roomSoldTo")).isEqualTo("Habitacion 3");
        assertThat(data.get("saleTime")).isEqualTo("08:30 PM");
    }

    /**
     * Verifies the "not finished" marker for the active turn.
     * Expected: turnEnd is "No finalizado" while the turn is active.
     * Failure: custom reports print an invalid date or a null for the running turn.
     */
    @Test
    void shouldMarkActiveTurnAsNotFinished() {
        TurnDetails details = turnDetails();

        assertThat(PrintDataBuilder.turnSummary(details, true, CONTEXT).get("turnEnd")).isEqualTo("No finalizado");
    }

    /**
     * Verifies the end date of a closed turn.
     * Expected: turnEnd uses the turn report date format.
     * Failure: historical reports print the raw ISO timestamp.
     */
    @Test
    void shouldFormatClosedTurnEnd() {
        TurnDetails details = turnDetails();
        ZonedDateTime end = START.plusHours(9);
        details.setTurnEnd(end);

        assertThat(PrintDataBuilder.turnSummary(details, false, CONTEXT).get("turnEnd"))
                .isEqualTo(end.format(PrintDataBuilder.TURN_DATE_FORMATTER));
    }

    /**
     * Verifies the summary concept table.
     * Expected: room and item concepts land in summaryList.
     * Failure: the summarized report loses its concept table when templated.
     */
    @Test
    void shouldBuildSummaryConceptRows() {
        Map<String, String> data = PrintDataBuilder.turnSummary(turnDetails(), true, CONTEXT);

        assertThat(data.get("summaryList")).contains("Alquiler 3h").contains("Cerveza");
        assertThat(data.get("totalRooms")).isEqualTo(CurrencyFormatter.format(40_000L, CURRENCY));
        assertThat(data.get("totalItems")).isEqualTo(CurrencyFormatter.format(11_000L, CURRENCY));
    }

    /**
     * Verifies the detailed activity listing.
     * Expected: one pipe separated row per activity, with the booking concept.
     * Failure: the detailed report loses its activity listing when templated.
     */
    @Test
    void shouldBuildActivityRows() {
        Map<String, String> data = PrintDataBuilder.turnDetail(turnDetails(), true, CONTEXT);

        assertThat(data.get("activityList")).contains("|Habitacion 3|Alquiler 3h|").contains("|Cerveza|");
        assertThat(data.get("turnNumber")).isEqualTo("42");
    }

    /**
     * Verifies that unset motel data never leaks a literal "null" onto a receipt.
     * Expected: missing motel values resolve to empty strings.
     * Failure: receipts print "null" in the header.
     */
    @Test
    void shouldUseEmptyStringsForMissingMotelData() {
        PrintDataBuilder.PrintContext empty = new PrintDataBuilder.PrintContext(null, null, null, null);
        Map<String, String> data = PrintDataBuilder.roomReceipt(roomBooking(), 1, empty);

        assertThat(data.get("motelName")).isEmpty();
        assertThat(data.get("motelAddress")).isEmpty();
        assertThat(data.get("motelID")).isEmpty();
        assertThat(data.get("totalPrice")).isNotEmpty();
    }

    private static Map<String, String> buildFor(PrintTemplateType type) {
        return switch (type) {
            case ROOM_RECEIPT -> PrintDataBuilder.roomReceipt(roomBooking(), 77, CONTEXT);
            case SALE_RECEIPT -> PrintDataBuilder.saleReceipt(sale(), 88, CONTEXT);
            case TURN_SUMMARY -> PrintDataBuilder.turnSummary(turnDetails(), true, CONTEXT);
            case TURN_DETAIL -> PrintDataBuilder.turnDetail(turnDetails(), true, CONTEXT);
        };
    }

    private static RoomBookingActivity roomBooking() {
        return new RoomBookingActivity(START, "Habitacion 3", 3, 1, 2, RoomStatus.OCCUPIED,
                START, START.plusHours(3), 40_000L, 10_800L, 0L, 0L, 77, false);
    }

    private static SaleActivity sale() {
        return new SaleActivity(START, "Habitacion 3",
                List.of(new SaleItem("Cerveza", 1L, 2L, 8_000L, false),
                        new SaleItem("Agua", 2L, 1L, 3_000L, false)), 88);
    }

    private static TurnDetails turnDetails() {
        TurnDetails details = new TurnDetails(42L, START, true);
        details.addActivity(roomBooking());
        details.addActivity(sale());
        return details;
    }
}
