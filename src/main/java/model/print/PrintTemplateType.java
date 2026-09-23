package model.print;

/**
 * The printable documents whose layout can be customized with a {@link PrintTemplate}.
 */
public enum PrintTemplateType {

    ROOM_RECEIPT("Recibo de habitación"),
    SALE_RECEIPT("Recibo de venta"),
    TURN_SUMMARY("Resumen de turno"),
    TURN_DETAIL("Detalle de turno");

    private final String displayName;

    PrintTemplateType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the Spanish name shown in the template editor
     */
    public String displayName() {
        return displayName;
    }

    /**
     * @return the file name used both under {@code data/printTemplates} and on the classpath
     */
    public String fileName() {
        return name().toLowerCase() + ".json";
    }
}
