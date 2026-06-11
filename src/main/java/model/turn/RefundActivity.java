package model.turn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RefundActivity(
        @JsonProperty("changeDate") ZonedDateTime changeDate,
        @JsonProperty("refundType") RefundType refundType,
        @JsonProperty("consecutiveTrans") int consecutiveTrans,
        @JsonProperty("refundConsecutiveTrans") int refundConsecutiveTrans,
        @JsonProperty("refundRoom") String refundRoom,
        @JsonProperty("price") long price,
        @JsonProperty("refundServiceDuration") long refundServiceDuration,
        @JsonProperty("itemID") long itemID,
        @JsonProperty("quantity") long quantity,
        @JsonProperty("itemName") String itemName,
        @JsonProperty("refundRoomData") RoomData refundRoomData
) implements TurnActivity {

    public RefundActivity(
            ZonedDateTime changeDate, RefundType refundType, int consecutiveTrans,
            int refundConsecutiveTrans, String refundRoom, long price,
            long refundServiceDuration, long itemID, long quantity, String itemName) {
        this(changeDate, refundType, consecutiveTrans, refundConsecutiveTrans, refundRoom,
                price, refundServiceDuration, itemID, quantity, itemName, null);
    }

    @JsonCreator
    public static RefundActivity createFromJson(
            @JsonProperty("changeDate") ZonedDateTime changeDate,
            @JsonProperty("refundType") RefundType refundType,
            @JsonProperty("consecutiveTrans") int consecutiveTrans,
            @JsonProperty("refundConsecutiveTrans") Integer refundConsecutiveTrans,
            @JsonProperty("refundRoom") String refundRoom,
            @JsonProperty("price") long price,
            @JsonProperty("refundServiceDuration") Long refundServiceDuration,
            @JsonProperty("refundService") Integer legacyRefundService,
            @JsonProperty("itemID") Long itemID,
            @JsonProperty("quantity") Long quantity,
            @JsonProperty("itemName") String itemName,
            @JsonProperty("refundRoomData") RoomData refundRoomData) {
        long refundDur = 0L;
        if (refundType == RefundType.ROOM_REFUND) {
            refundDur = refundServiceDuration != null ? refundServiceDuration
                    : (legacyRefundService != null ? (long) legacyRefundService * 3600L : 0L);
        }
        return new RefundActivity(
                changeDate, refundType, consecutiveTrans,
                refundConsecutiveTrans != null ? refundConsecutiveTrans : 0,
                refundRoom, price, refundDur,
                refundType == RefundType.SALE_REFUND ? (itemID != null ? itemID : 0L) : 0L,
                refundType == RefundType.SALE_REFUND ? (quantity != null ? quantity : 0L) : 0L,
                refundType == RefundType.SALE_REFUND ? itemName : null,
                refundRoomData);
    }
}
