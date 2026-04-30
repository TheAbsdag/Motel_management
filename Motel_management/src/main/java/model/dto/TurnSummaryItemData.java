package model.dto;

/**
 * Typed data object for a single entry in a turn's summary report.
 * Summaries aggregate room bookings, item sales, refunds, and extra changes
 * into grouped line items.
 *
 * @param summaryType "room", "item", "itemRefund", "roomRefund", or "extraChange"
 * @param quantity    number of units sold/booked/refunded
 * @param price       total price for this summary line
 * @param name        for items/itemRefunds: the item name; for extraChange: "bankTransfer" or "safeDeposit"
 * @param service     the service duration in hours (only meaningful for room/roomRefund summaries)
 */
public record TurnSummaryItemData(String summaryType, int quantity, long price, String name, int service) {

    /**
     * Returns a formatted price string for display.
     */
    public String formattedPrice() {
        return String.format("%,d", price);
    }

    /**
     * Returns the display concept column value.
     */
    public String displayConcept() {
        return switch (summaryType) {
            case "room" -> "Alquiler " + service;
            case "item" -> name;
            case "itemRefund" -> "Dev. " + name;
            case "roomRefund" -> "Dev. Alquiler " + service;
            case "extraChange" -> {
                if ("bankTransfer".equals(name)) yield "Transf. Bancaria";
                else if ("safeDeposit".equals(name)) yield "Abono a Caja";
                else yield name;
            }
            default -> name != null ? name : "";
        };
    }
}
