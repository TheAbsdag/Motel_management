package model.dto;

import model.StockRules;

/**
 * Typed data object for inventory items displayed in views.
 * Replaces raw JSONObject usage for inventory data at the model→view boundary.
 *
 * @param itemID   unique identifier for the item
 * @param name     display name of the item
 * @param price    unit price in Colombian Pesos
 * @param quantity current stock quantity
 * @param minimum  per-product low-stock minimum, zero when none is configured
 */
public record InventoryItemData(long itemID, String name, long price, long quantity, long minimum) {

    /**
     * Creates an item without a configured minimum.
     *
     * @param itemID   unique identifier for the item
     * @param name     display name of the item
     * @param price    unit price in Colombian Pesos
     * @param quantity current stock quantity
     */
    public InventoryItemData(long itemID, String name, long price, long quantity) {
        this(itemID, name, price, quantity, 0L);
    }

    /** @return true when there are no units left */
    public boolean isOut() {
        return StockRules.isOut(quantity);
    }

    /** @return true when there are units but the product is at or below its minimum */
    public boolean isLow() {
        return StockRules.isLow(quantity, minimum);
    }
}
