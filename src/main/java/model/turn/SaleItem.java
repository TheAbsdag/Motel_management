package model.turn;

import org.json.JSONObject;

/**
 * Represents a single item sold as part of a {@link SaleActivity}.
 *
 * @param itemName display name of the product
 * @param itemID   unique inventory identifier
 * @param quantity number of units sold
 * @param price    total price for the quantity sold
 * @param refunded whether this item has been refunded
 */
public record SaleItem(String itemName, long itemID, long quantity, long price, boolean refunded) {

    /**
     * Serializes this item to a JSON object.
     *
     * @return JSON representation suitable for persistence
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("itemName", itemName);
        json.put("itemID", itemID);
        json.put("quantity", quantity);
        json.put("price", price);
        if (refunded) {
            json.put("refunded", true);
        }
        return json;
    }

    /**
     * Deserializes a {@code SaleItem} from its JSON representation.
     *
     * @param json JSON object previously produced by {@link #toJson()}
     * @return the reconstructed item
     */
    public static SaleItem fromJson(JSONObject json) {
        return new SaleItem(
                json.getString("itemName"),
                json.getLong("itemID"),
                json.getLong("quantity"),
                json.getLong("price"),
                json.optBoolean("refunded", false)
        );
    }
}
