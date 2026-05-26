package model.turn;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.ZonedDateTime;

/**
 * Sealed interface for all turn activity types.
 *
 * <p>Each activity represents a transaction that occurred during a shift (turn).
 * The six permitted subtypes cover room bookings, sales, room swaps, refunds,
 * spending, and extra changes (bank transfers / deposits).
 *
 * @see RoomBookingActivity
 * @see SaleActivity
 * @see RoomSwapActivity
 * @see RefundActivity
 * @see SpendingActivity
 * @see ExtraChangeActivity
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "changeType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RoomBookingActivity.class, name = "room"),
        @JsonSubTypes.Type(value = SaleActivity.class, name = "sale"),
        @JsonSubTypes.Type(value = RoomSwapActivity.class, name = "roomSwap"),
        @JsonSubTypes.Type(value = RefundActivity.class, name = "refund"),
        @JsonSubTypes.Type(value = SpendingActivity.class, name = "spending"),
        @JsonSubTypes.Type(value = ExtraChangeActivity.class, name = "extraChange"),
})
public sealed interface TurnActivity
        permits RoomBookingActivity, SaleActivity, RoomSwapActivity,
                RefundActivity, SpendingActivity, ExtraChangeActivity {

    ZonedDateTime changeDate();
    int consecutiveTrans();
}
