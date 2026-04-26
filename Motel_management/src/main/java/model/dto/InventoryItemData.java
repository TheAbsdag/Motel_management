package model.dto;

/**
 * Typed data object for inventory items displayed in views.
 * Replaces raw JSONObject usage for inventory data at the model→view boundary.
 *
 * @param itemID   unique identifier for the item
 * @param name     display name of the item
 * @param price    unit price in Colombian Pesos
 * @param quantity current stock quantity
 */
public record InventoryItemData(long itemID, String name, long price, long quantity) {

    /**
     * Returns a formatted price string for display.
     */
    public String formattedPrice() {
        return String.format("%,d", price);
    }

    /**
     * Returns a formatted quantity string for display.
     */
    public String formattedQuantity() {
        return String.format("%,d", quantity);
    }
}
