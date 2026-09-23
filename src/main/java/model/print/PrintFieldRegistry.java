package model.print;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Catalog of the fields every {@link PrintTemplateType} can print.
 *
 * <p>Each entry binds the {@code dataKey} a template references to the Spanish
 * label shown by the template editor and to a sample value used to render the
 * editor preview without a real transaction.
 *
 * <p>Every key listed here is produced by {@link PrintDataBuilder} for the same
 * type, so any template built from this catalog renders with real data.
 */
public final class PrintFieldRegistry {

    private PrintFieldRegistry() {}

    /**
     * A single bindable template field.
     *
     * @param dataKey key resolved by {@link PrintDataBuilder}
     * @param label   Spanish label shown in the template editor
     * @param sample  example value used by the editor preview
     */
    public record FieldDef(String dataKey, String label, String sample) {}

    private static final List<FieldDef> MOTEL_FIELDS = List.of(
            new FieldDef("motelName", "Nombre del motel", "MOTEL LAS PALMAS"),
            new FieldDef("motelAddress", "Dirección del motel", "Cra 10 #20-30"),
            new FieldDef("motelID", "NIT", "900.123.456-7"),
            new FieldDef("legalText", "Texto legal (IVA)", "  PERSONA NATURAL.\n NO RESPONSABLE DE IVA"));

    private static final List<FieldDef> ROOM_FIELDS = List.of(
            new FieldDef("consecutive", "No. de factura", "1024"),
            new FieldDef("roomString", "Habitación (completa)", "Habitación 3"),
            new FieldDef("roomNumber", "Número de habitación", "3"),
            new FieldDef("floorNumber", "Piso", "1"),
            new FieldDef("towerNumber", "Torre", "2"),
            new FieldDef("entryTime", "Hora de entrada", "08:30 PM"),
            new FieldDef("serviceDuration", "Duración del servicio", "3h"),
            new FieldDef("extensionDuration", "Extensión", "1h"),
            new FieldDef("totalPrice", "Pago total", "$40.000"),
            new FieldDef("date", "Fecha", "2026-09-15"),
            new FieldDef("reminderText", "Recordatorio", "NO OLVIDE SUS PERTENENCIAS"));

    private static final List<FieldDef> SALE_FIELDS = List.of(
            new FieldDef("consecutive", "No. de factura", "1024"),
            new FieldDef("roomSoldTo", "Habitación", "Habitación 3"),
            new FieldDef("saleTime", "Hora de venta", "08:30 PM"),
            new FieldDef("date", "Fecha", "2026-09-15"),
            new FieldDef("items", "Ítems vendidos (lista)", "   1 Cerveza\t$8.000"),
            new FieldDef("totalPrice", "Pago total", "$11.000"));

    private static final List<FieldDef> TURN_FIELDS = List.of(
            new FieldDef("turnNumber", "Número de turno", "42"),
            new FieldDef("turnStart", "Inicio de turno", "2026/09/15 - 20:30:00"),
            new FieldDef("turnEnd", "Fin de turno", "2026/09/16 - 05:30:00"),
            new FieldDef("totalRooms", "Total habitaciones", "$40.000"),
            new FieldDef("totalItems", "Total productos", "$11.000"),
            new FieldDef("totalSales", "Total ventas", "$51.000"),
            new FieldDef("totalRoomRefunds", "Reembolsos de habitaciones", "$0"),
            new FieldDef("totalItemRefunds", "Reembolsos de productos", "$0"),
            new FieldDef("totalRefunds", "Total reembolsos", "$0"),
            new FieldDef("totalSpending", "Total gastos", "$5.000"),
            new FieldDef("totalTurn", "Total turno", "$46.000"),
            new FieldDef("totalBankTransfers", "Transferencias", "$0"),
            new FieldDef("totalDeposits", "Depósitos", "$0"),
            new FieldDef("totalNet", "Total neto", "$46.000"));

    private static final FieldDef SUMMARY_LIST =
            new FieldDef("summaryList", "Conceptos vendidos (lista)", "   1 Alquiler 3h\t$40.000");
    private static final FieldDef REFUND_LIST =
            new FieldDef("refundList", "Conceptos reembolsados (lista)", "   1 Alquiler 3h\t$40.000");
    private static final FieldDef ACTIVITY_LIST =
            new FieldDef("activityList", "Actividades (lista)", " 09/15-08:30 PM|Habitación 3|Alquiler 3h|$40.000");

    private static final List<FieldDef> ROOM_RECEIPT_FIELDS = merge(MOTEL_FIELDS, ROOM_FIELDS);
    private static final List<FieldDef> SALE_RECEIPT_FIELDS = merge(MOTEL_FIELDS, SALE_FIELDS);
    private static final List<FieldDef> TURN_SUMMARY_FIELDS =
            merge(MOTEL_FIELDS, TURN_FIELDS, List.of(SUMMARY_LIST, REFUND_LIST));
    private static final List<FieldDef> TURN_DETAIL_FIELDS =
            merge(MOTEL_FIELDS, TURN_FIELDS, List.of(ACTIVITY_LIST));

    /**
     * Returns the fields available to a template type.
     *
     * @param type the template type
     * @return the bindable fields, in editor display order
     */
    public static List<FieldDef> getFieldsForType(PrintTemplateType type) {
        return switch (type) {
            case ROOM_RECEIPT -> ROOM_RECEIPT_FIELDS;
            case SALE_RECEIPT -> SALE_RECEIPT_FIELDS;
            case TURN_SUMMARY -> TURN_SUMMARY_FIELDS;
            case TURN_DETAIL -> TURN_DETAIL_FIELDS;
        };
    }

    /**
     * Returns sample data covering every field of a template type.
     *
     * @param type the template type
     * @return field keys mapped to their example values
     */
    public static Map<String, String> sampleData(PrintTemplateType type) {
        Map<String, String> data = new LinkedHashMap<>();
        for (FieldDef field : getFieldsForType(type)) {
            data.put(field.dataKey(), field.sample());
        }
        return data;
    }

    @SafeVarargs
    private static List<FieldDef> merge(List<FieldDef>... lists) {
        return Stream.of(lists).flatMap(List::stream).toList();
    }
}
