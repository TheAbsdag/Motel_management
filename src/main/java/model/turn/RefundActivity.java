package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

/**
 * Records a refund — either for a room booking or for previously sold items.
 *
 * <p>When {@code refundType} is {@link RefundType#ROOM_REFUND} the fields
 * {@code itemID}, {@code quantity} and {@code itemName} are unused; when it is
 * {@link RefundType#SALE_REFUND} the field {@code refundServiceDuration} is unused.
 *
 * @param changeDate               when the refund was processed
 * @param refundType               type of refund (room or sale)
 * @param consecutiveTrans         consecutive transaction number for this refund
 * @param refundConsecutiveTrans   original transaction number being refunded
 * @param refundRoom               room identifier associated with the refund
 * @param price                    monetary amount being refunded
 * @param refundServiceDuration    service duration for room refunds (in seconds)
 * @param itemID                   inventory item ID for sale refunds
 * @param quantity                 quantity refunded for sale refunds
 * @param itemName                 item name for sale refunds
 */
public record RefundActivity(
        ZonedDateTime changeDate,
        RefundType refundType,
        int consecutiveTrans,
        int refundConsecutiveTrans,
        String refundRoom,
        long price,
        long refundServiceDuration,
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
            json.put("refundServiceDuration", refundServiceDuration);
        } else {
            json.put("itemID", itemID);
            json.put("quantity", quantity);
            json.put("itemName", itemName);
        }
        return json;
    }

    /**
     * Deserializes a {@code RefundActivity} from its JSON representation.
     * Supports both the new format ({@code refundServiceDuration} in seconds)
     * and the legacy format ({@code refundService} in hours, multiplied by 3600).
     *
     * @param json JSON object previously produced by {@link #toJson()} (or legacy format)
     * @return the reconstructed activity
     */
    public static RefundActivity fromJson(JSONObject json) {
        RefundType type = RefundType.fromString(json.getString("refundType"));
        long refundDur = 0;
        if (type == RefundType.ROOM_REFUND) {
            refundDur = json.has("refundServiceDuration")
                    ? json.getLong("refundServiceDuration")
                    : (long) json.getInt("refundService") * 3600L;
        }
        return new RefundActivity(
                ZonedDateTime.parse(json.getString("changeDate")),
                type,
                json.getInt("consecutiveTrans"),
                json.optInt("refundConsecutiveTrans", 0),
                json.getString("refundRoom"),
                json.getLong("price"),
                refundDur,
                type == RefundType.SALE_REFUND ? json.getLong("itemID") : 0,
                type == RefundType.SALE_REFUND ? json.getLong("quantity") : 0,
                type == RefundType.SALE_REFUND ? json.getString("itemName") : null
        );
    }
}
