package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

/**
 * Records a refund — either for a room booking or for previously sold items.
 *
 * <p>When {@code refundType} is {@link RefundType#ROOM_REFUND} the fields
 * {@code itemID}, {@code quantity} and {@code itemName} are unused; when it is
 * {@link RefundType#SALE_REFUND} the field {@code refundService} is unused.
 *
 * @param changeDate             when the refund was processed
 * @param refundType             type of refund (room or sale)
 * @param consecutiveTrans       consecutive transaction number for this refund
 * @param refundConsecutiveTrans original transaction number being refunded
 * @param refundRoom             room identifier associated with the refund
 * @param price                  monetary amount being refunded
 * @param refundService          service duration for room refunds (in minutes)
 * @param itemID                 inventory item ID for sale refunds
 * @param quantity               quantity refunded for sale refunds
 * @param itemName               item name for sale refunds
 */
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

    /**
     * Deserializes a {@code RefundActivity} from its JSON representation.
     *
     * @param json JSON object previously produced by {@link #toJson()}
     * @return the reconstructed activity
     */
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
