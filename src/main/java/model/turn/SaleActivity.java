package model.turn;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Records a sale of one or more items to a guest in a specific room.
 *
 * @param changeDate       when the sale was processed
 * @param roomSoldTo       room identifier where the items were delivered
 * @param items            list of individual items sold
 * @param consecutiveTrans consecutive transaction counter
 */
public record SaleActivity(
        ZonedDateTime changeDate,
        String roomSoldTo,
        List<SaleItem> items,
        int consecutiveTrans
) implements TurnActivity {

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("changeDate", changeDate.toString());
        json.put("changeType", "sale");
        json.put("roomSoldTo", roomSoldTo);
        JSONArray arr = new JSONArray();
        for (SaleItem item : items) {
            arr.put(item.toJson());
        }
        json.put("register", arr);
        json.put("consecutiveTrans", consecutiveTrans);
        return json;
    }

    /**
     * Deserializes a {@code SaleActivity} from its JSON representation.
     *
     * @param json JSON object previously produced by {@link #toJson()}
     * @return the reconstructed activity
     */
    public static SaleActivity fromJson(JSONObject json) {
        JSONArray arr = json.getJSONArray("register");
        List<SaleItem> items = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            items.add(SaleItem.fromJson(arr.getJSONObject(i)));
        }
        return new SaleActivity(
                ZonedDateTime.parse(json.getString("changeDate")),
                json.getString("roomSoldTo"),
                items,
                json.getInt("consecutiveTrans")
        );
    }
}
