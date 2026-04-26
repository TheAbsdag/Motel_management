package model.dto;

/**
 * Typed data object for items in the active selling/cart list.
 * Replaces raw JSONObject usage for selling list data at the model→view boundary.
 *
 * @param itemID    unique identifier for the item
 * @param itemName  display name of the item
 * @param quantity  number of units being sold
 * @param price     total price for this line item (quantity * unit price)
 * @param isCourtesy whether this item is a courtesy (zero price)
 */
public record SellingItemData(long itemID, String itemName, long quantity, long price, boolean isCourtesy) {

    /**
     * Returns a formatted price string for display.
     */
    public String formattedPrice() {
        return String.format("%,d", price);
    }

    /**
     * Returns the effective price for column display (0 for courtesy items).
     */
    public long displayPrice() {
        return isCourtesy ? 0 : price;
    }
}
