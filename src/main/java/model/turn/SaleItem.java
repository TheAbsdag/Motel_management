package model.turn;

import org.json.JSONObject;

public record SaleItem(String itemName, long itemID, long quantity, long price, boolean refunded) {

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
