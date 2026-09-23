package model.print;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import model.dto.TurnSummaryItemData;
import model.json.CurrencyConfig;
import model.turn.ExtraChangeActivity;
import model.turn.ExtraChangeType;
import model.turn.RefundActivity;
import model.turn.RefundType;
import model.turn.RoomBookingActivity;
import model.turn.RoomSwapActivity;
import model.turn.SaleActivity;
import model.turn.SaleItem;
import model.turn.SpendingActivity;
import model.turn.TurnDetails;
import view.helpers.CurrencyFormatter;
import view.helpers.TimeFormatter;

/**
 * Builds the field values a {@link PrintTemplate} resolves when printing.
 *
 * <p>Producing the values here keeps the printer free of layout concerns and makes
 * every mapping unit-testable without touching a printer or the UI.
 *
 * <p>The formatters and literals are the same ones the built-in layouts use, so a
 * template renders identical text to the hard-coded receipt it replaces.
 */
public final class PrintDataBuilder {

    /** Time zone used by every printed timestamp. */
    public static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    /** Hour format of receipts, e.g. {@code 08:30 PM}. */
    public static final DateTimeFormatter HOUR_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("hh:mm").appendLiteral(' ')
            .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "AM", 1L, "PM"))
            .toFormatter()
            .withZone(BOGOTA);
    /** Short date format of receipts, e.g. {@code 2026-09-15}. */
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", new Locale("es", "ES")).withZone(BOGOTA);
    /** Turn report timestamp, e.g. {@code 2026/09/15 - 20:30:00}. */
    public static final DateTimeFormatter TURN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss").withZone(BOGOTA);
    /** Detailed report timestamp, e.g. {@code 09/15-20:30}. */
    public static final DateTimeFormatter DETAIL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd-HH:mm").withZone(BOGOTA);

    /** Legal text printed on every receipt. */
    public static final String LEGAL_TEXT = "  PERSONA NATURAL.\n NO RESPONSABLE DE IVA";
    /** Reminder printed on room receipts. */
    public static final String REMINDER_TEXT = "NO OLVIDE SUS PERTENENCIAS";
    /** Value of the turn end field while the turn is still running. */
    public static final String NOT_FINISHED = "No finalizado";

    /**
     * Motel identity and currency needed to resolve template fields.
     *
     * @param motelName    motel name
     * @param motelAddress motel address
     * @param motelID      motel tax identifier
     * @param currency     currency configuration used to format amounts; defaults when {@code null}
     */
    public record PrintContext(String motelName, String motelAddress, String motelID, CurrencyConfig currency) {}

    private PrintDataBuilder() {}

    /**
     * Builds the data of a room booking receipt.
     *
     * @param activity    the booking activity
     * @param consecutive invoice number
     * @param context     motel and currency data
     * @return field values for {@link PrintTemplateType#ROOM_RECEIPT}
     */
    public static Map<String, String> roomReceipt(RoomBookingActivity activity, int consecutive, PrintContext context) {
        Map<String, String> data = base(context);
        ZonedDateTime date = activity.startStatus();

        data.put("consecutive", String.valueOf(consecutive));
        data.put("roomString", orEmpty(activity.roomString()));
        data.put("roomNumber", String.valueOf(activity.roomNumber()));
        data.put("floorNumber", String.valueOf(activity.floorNumber()));
        data.put("towerNumber", String.valueOf(activity.towerNumber()));
        data.put("entryTime", date.format(HOUR_FORMATTER));
        data.put("date", date.format(DATE_FORMATTER));
        data.put("serviceDuration", TimeFormatter.formatDuration(activity.getEffectiveServiceDuration()));
        data.put("extensionDuration", activity.extensionDuration() > 0
                ? TimeFormatter.formatDuration(activity.extensionDuration()) : "");
        data.put("totalPrice", money(activity.price(), context));
        data.put("reminderText", REMINDER_TEXT);
        return data;
    }

    /**
     * Builds the data of an item sale receipt.
     *
     * @param activity    the sale activity
     * @param consecutive invoice number
     * @param context     motel and currency data
     * @return field values for {@link PrintTemplateType#SALE_RECEIPT}
     */
    public static Map<String, String> saleReceipt(SaleActivity activity, int consecutive, PrintContext context) {
        Map<String, String> data = base(context);
        ZonedDateTime date = activity.changeDate();
        List<SaleItem> items = activity.items() != null ? activity.items() : List.of();

        long total = 0;
        StringBuilder rows = new StringBuilder();
        for (SaleItem item : items) {
            total += item.price();
            appendTabRow(rows, String.valueOf(item.quantity()), orEmpty(item.itemName()),
                    money(item.price(), context));
        }

        data.put("consecutive", String.valueOf(consecutive));
        data.put("roomSoldTo", orEmpty(activity.roomSoldTo()));
        data.put("saleTime", date.format(HOUR_FORMATTER));
        data.put("date", date.format(DATE_FORMATTER));
        data.put("items", rows.toString());
        data.put("totalPrice", money(total, context));
        return data;
    }

    /**
     * Builds the data of a summarized turn report.
     *
     * @param details   the turn data
     * @param isCurrent whether the turn is still running
     * @param context   motel and currency data
     * @return field values for {@link PrintTemplateType#TURN_SUMMARY}
     */
    public static Map<String, String> turnSummary(TurnDetails details, boolean isCurrent, PrintContext context) {
        Map<String, String> data = turnBase(details, isCurrent, context);
        data.put("summaryList", summaryRows(details, context, false));
        data.put("refundList", summaryRows(details, context, true));
        return data;
    }

    /**
     * Builds the data of a detailed turn report.
     *
     * @param details   the turn data
     * @param isCurrent whether the turn is still running
     * @param context   motel and currency data
     * @return field values for {@link PrintTemplateType#TURN_DETAIL}
     */
    public static Map<String, String> turnDetail(TurnDetails details, boolean isCurrent, PrintContext context) {
        Map<String, String> data = turnBase(details, isCurrent, context);
        data.put("activityList", activityRows(details, context));
        return data;
    }

    private static Map<String, String> base(PrintContext context) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("motelName", orEmpty(context.motelName()));
        data.put("motelAddress", orEmpty(context.motelAddress()));
        data.put("motelID", orEmpty(context.motelID()));
        data.put("legalText", LEGAL_TEXT);
        return data;
    }

    private static Map<String, String> turnBase(TurnDetails details, boolean isCurrent, PrintContext context) {
        Map<String, String> data = base(context);
        ZonedDateTime turnEnd = details.getTurnEnd();

        data.put("turnNumber", String.valueOf(details.getTurnNumber()));
        data.put("turnStart", details.getTurnStart().format(TURN_DATE_FORMATTER));
        data.put("turnEnd", isCurrent || turnEnd == null
                ? NOT_FINISHED : turnEnd.format(TURN_DATE_FORMATTER));
        data.put("totalRooms", money(details.getTotalRooms(), context));
        data.put("totalItems", money(details.getTotalItems(), context));
        data.put("totalSales", money(details.getTotalSales(), context));
        data.put("totalRoomRefunds", money(details.getTotalRoomRefunds(), context));
        data.put("totalItemRefunds", money(details.getTotalItemRefunds(), context));
        data.put("totalRefunds", money(details.getTotalRefunds(), context));
        data.put("totalSpending", money(details.getTotalSpending(), context));
        data.put("totalTurn", money(details.getTotalTurn(), context));
        data.put("totalBankTransfers", money(details.getTotalBankTransfers(), context));
        data.put("totalDeposits", money(details.getTotalDeposits(), context));
        data.put("totalNet", money(details.getTotalNet(), context));
        return data;
    }

    /**
     * Builds the concept table of a turn, either the sold concepts or the refunded ones.
     * Extra changes are reported through their totals only, matching the built-in layout.
     */
    private static String summaryRows(TurnDetails details, PrintContext context, boolean refunds) {
        StringBuilder rows = new StringBuilder();
        for (TurnSummaryItemData item : details.getSummaryItems()) {
            String type = item.summaryType();
            boolean refund = type.endsWith("Refund");
            boolean room = type.startsWith("room");
            if (!room && !"item".equals(type) && !refund) {
                continue;
            }
            if (refund != refunds) {
                continue;
            }
            String concept = room
                    ? "Alquiler " + TimeFormatter.formatDuration(item.serviceDuration())
                    : orEmpty(item.name());
            appendTabRow(rows, String.valueOf(item.quantity()), concept, money(item.price(), context));
        }
        return rows.toString();
    }

    /**
     * Builds the activity listing of a detailed turn report, one row per printed movement.
     */
    private static String activityRows(TurnDetails details, PrintContext context) {
        StringBuilder rows = new StringBuilder();
        for (var activity : details.getActivities()) {
            String date = activity.changeDate() != null ? activity.changeDate().format(DETAIL_DATE_FORMATTER) : "";
            switch (activity) {
                case SaleActivity sale -> {
                    for (SaleItem item : sale.items()) {
                        appendPipeRow(rows, date, orEmpty(sale.roomSoldTo()), orEmpty(item.itemName()),
                                money(item.price(), context));
                    }
                }
                case RoomBookingActivity room -> {
                    if (room.isOccupied()) {
                        appendPipeRow(rows, date, orEmpty(room.roomString()),
                                "Alquiler " + TimeFormatter.formatDuration(room.getEffectiveServiceDuration()),
                                money(room.price(), context));
                    }
                }
                case RoomSwapActivity swap -> appendPipeRow(rows, date, orEmpty(swap.originalRoom()),
                        "Cambio a: " + orEmpty(swap.swappedRoom()), "");
                case RefundActivity refund -> appendPipeRow(rows, date, refundConcept(refund),
                        money(refund.price(), context));
                case SpendingActivity spending -> appendPipeRow(rows, date,
                        "Gasto de: " + orEmpty(spending.description()), money(spending.value(), context));
                case ExtraChangeActivity extra -> appendPipeRow(rows, date,
                        (extra.extraType() == ExtraChangeType.SAFE_DEPOSIT ? "Deposito de: " : "Transferencia de: ")
                                + orEmpty(extra.description()),
                        money(extra.value(), context));
            }
        }
        return rows.toString();
    }

    private static String refundConcept(RefundActivity refund) {
        return refund.refundType() == RefundType.SALE_REFUND
                ? "Reembolso de " + refund.quantity() + " de " + orEmpty(refund.itemName())
                : "Reembolso de habitacion " + orEmpty(refund.refundRoom());
    }

    private static void appendTabRow(StringBuilder rows, String quantity, String concept, String amount) {
        if (!rows.isEmpty()) {
            rows.append('\n');
        }
        rows.append("   ").append(quantity).append(' ').append(concept).append('\t').append(amount);
    }

    private static void appendPipeRow(StringBuilder rows, String... cells) {
        if (!rows.isEmpty()) {
            rows.append('\n');
        }
        rows.append(' ').append(String.join("|", cells));
    }

    private static String money(long value, PrintContext context) {
        CurrencyConfig currency = context.currency() != null ? context.currency() : CurrencyConfig.defaultConfig();
        return CurrencyFormatter.format(value, currency);
    }

    private static String orEmpty(String value) {
        return value != null ? value : "";
    }
}
