package model.dto;

/**
 * Typed data object for a single entry in a turn's summary report.
 * Summaries aggregate room bookings and item sales into grouped line items.
 * <p>
 * Examples: "Alquiler 3" with quantity 5 and total 150000, or "Coca-Cola" with quantity 10 and total 25000.
 *
 * @param summaryType "room" or "item"
 * @param quantity    number of units sold/booked
 * @param price       total price for this summary line
 * @param name        for items: the item name; for rooms: "Alquiler " + service
 * @param service     the service duration in hours (only meaningful for room summaries)
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
        if ("room".equals(summaryType)) {
            return "Alquiler " + service;
        }
        return name;
    }
}
