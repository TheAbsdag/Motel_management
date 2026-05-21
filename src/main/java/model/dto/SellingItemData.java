package model.dto;

import view.helpers.FormatHelper;

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

    public String formattedPrice() {
        return FormatHelper.formatPrice(price);
    }

    public long displayPrice() {
        return isCourtesy ? 0 : price;
    }
}
