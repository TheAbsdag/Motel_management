package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

/**
 * Records a non-operational financial movement such as a bank transfer or safe deposit.
 *
 * @param changeDate      when the extra change occurred
 * @param extraType       indicates whether this is a bank transfer or a safe deposit
 * @param description     free-text description of the movement
 * @param value           monetary amount (positive for deductions from the turn)
 * @param consecutiveTrans consecutive transaction counter for ordering
 */
public record ExtraChangeActivity(
        ZonedDateTime changeDate,
        ExtraChangeType extraType,
        String description,
        long value,
        int consecutiveTrans
) implements TurnActivity {

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("changeDate", changeDate.toString());
        json.put("changeType", "extraChange");
        json.put("extraType", extraType.getValue());
        json.put("extraChangeDescription", description);
        json.put("value", value);
        json.put("consecutiveTrans", consecutiveTrans);
        return json;
    }

    /**
     * Deserializes an {@code ExtraChangeActivity} from its JSON representation.
     *
     * @param json JSON object previously produced by {@link #toJson()}
     * @return the reconstructed activity
     */
    public static ExtraChangeActivity fromJson(JSONObject json) {
        return new ExtraChangeActivity(
                ZonedDateTime.parse(json.getString("changeDate")),
                ExtraChangeType.fromString(json.getString("extraType")),
                json.getString("extraChangeDescription"),
                json.getLong("value"),
                json.getInt("consecutiveTrans")
        );
    }
}
