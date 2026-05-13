package model.turn;

import java.time.ZonedDateTime;
import org.json.JSONObject;

public sealed interface TurnActivity
        permits RoomBookingActivity, SaleActivity, RoomSwapActivity,
                RefundActivity, SpendingActivity, ExtraChangeActivity {

    ZonedDateTime changeDate();
    int consecutiveTrans();
    JSONObject toJson();
}
