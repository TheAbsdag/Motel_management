package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

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
