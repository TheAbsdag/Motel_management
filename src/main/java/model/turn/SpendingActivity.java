package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

public record SpendingActivity(
        ZonedDateTime changeDate,
        String description,
        long value,
        int consecutiveTrans
) implements TurnActivity {

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("changeDate", changeDate.toString());
        json.put("changeType", "spending");
        json.put("spendingDescription", description);
        json.put("value", value);
        json.put("consecutiveTrans", consecutiveTrans);
        return json;
    }

    public static SpendingActivity fromJson(JSONObject json) {
        return new SpendingActivity(
                ZonedDateTime.parse(json.getString("changeDate")),
                json.getString("spendingDescription"),
                json.getLong("value"),
                json.getInt("consecutiveTrans")
        );
    }
}
