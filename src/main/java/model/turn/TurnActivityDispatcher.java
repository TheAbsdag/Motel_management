package model.turn;

import java.util.List;

/**
 * Dispatches over the sealed {@link TurnActivity} hierarchy without
 * duplicating the type switch across the codebase.
 *
 * <p>Implement {@link Consumer} and pass it to {@link #forEach(List, Consumer)}
 * to handle each activity subtype in a single place.
 */
public final class TurnActivityDispatcher {

    private TurnActivityDispatcher() { }

    /**
     * Callback interface receiving each concrete subtype of {@link TurnActivity}.
     */
    public interface Consumer {
        default void acceptRoomBooking(RoomBookingActivity a) { }
        default void acceptSale(SaleActivity a) { }
        default void acceptRoomSwap(RoomSwapActivity a) { }
        default void acceptRefund(RefundActivity a) { }
        default void acceptSpending(SpendingActivity a) { }
        default void acceptExtraChange(ExtraChangeActivity a) { }
    }

    /**
     * Iterates over the activities and invokes the corresponding
     * {@link Consumer} method for each.
     */
    public static void forEach(List<TurnActivity> activities, Consumer consumer) {
        if (activities == null) return;
        for (TurnActivity a : activities) {
            switch (a) {
                case RoomBookingActivity r -> consumer.acceptRoomBooking(r);
                case SaleActivity s -> consumer.acceptSale(s);
                case RoomSwapActivity s -> consumer.acceptRoomSwap(s);
                case RefundActivity r -> consumer.acceptRefund(r);
                case SpendingActivity s -> consumer.acceptSpending(s);
                case ExtraChangeActivity e -> consumer.acceptExtraChange(e);
            }
        }
    }
}