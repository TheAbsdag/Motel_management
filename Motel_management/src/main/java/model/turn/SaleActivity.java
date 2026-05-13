package model.turn;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

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
