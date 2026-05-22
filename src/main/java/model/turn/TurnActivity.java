package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

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
public sealed interface TurnActivity
        permits RoomBookingActivity, SaleActivity, RoomSwapActivity,
                RefundActivity, SpendingActivity, ExtraChangeActivity {

    ZonedDateTime changeDate();
    int consecutiveTrans();
    JSONObject toJson();
}
