package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

/**
 * Records an operational expense (spending) incurred during the turn.
 *
 * @param changeDate       when the expense was recorded
 * @param description      free-text description of the expense
 * @param value            monetary amount of the expense
 * @param consecutiveTrans consecutive transaction counter
 */
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

    /**
     * Deserializes a {@code SpendingActivity} from its JSON representation.
     *
     * @param json JSON object previously produced by {@link #toJson()}
     * @return the reconstructed activity
     */
    public static SpendingActivity fromJson(JSONObject json) {
        return new SpendingActivity(
                ZonedDateTime.parse(json.getString("changeDate")),
                json.getString("spendingDescription"),
                json.getLong("value"),
                json.getInt("consecutiveTrans")
        );
    }
}
