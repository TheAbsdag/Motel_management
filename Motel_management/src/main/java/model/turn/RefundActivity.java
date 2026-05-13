package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

public record RefundActivity(
        ZonedDateTime changeDate,
        RefundType refundType,
        int consecutiveTrans,
        int refundConsecutiveTrans,
        String refundRoom,
        long price,
        int refundService,
        long itemID,
        long quantity,
        String itemName
) implements TurnActivity {

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("changeDate", changeDate.toString());
        json.put("changeType", "refund");
        json.put("refundType", refundType.getValue());
        json.put("consecutiveTrans", consecutiveTrans);
        json.put("refundConsecutiveTrans", refundConsecutiveTrans);
        json.put("refundRoom", refundRoom);
        json.put("price", price);
        if (refundType == RefundType.ROOM_REFUND) {
            json.put("refundService", refundService);
        } else {
            json.put("itemID", itemID);
            json.put("quantity", quantity);
            json.put("itemName", itemName);
        }
        return json;
    }

    public static RefundActivity fromJson(JSONObject json) {
        RefundType type = RefundType.fromString(json.getString("refundType"));
        return new RefundActivity(
                ZonedDateTime.parse(json.getString("changeDate")),
                type,
                json.getInt("consecutiveTrans"),
                json.optInt("refundConsecutiveTrans", 0),
                json.getString("refundRoom"),
                json.getLong("price"),
                type == RefundType.ROOM_REFUND ? json.getInt("refundService") : 0,
                type == RefundType.SALE_REFUND ? json.getLong("itemID") : 0,
                type == RefundType.SALE_REFUND ? json.getLong("quantity") : 0,
                type == RefundType.SALE_REFUND ? json.getString("itemName") : null
        );
    }
}
