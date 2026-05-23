package view;

import java.awt.Component;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomViewTest {

    /**
     * Verifies that clicking the backRoomButton fires the callback registered via
     * {@link RoomView#onBackButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The back button action listener is not wired to the onBackButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        RoomView view = new RoomView();
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backRoomButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the roomSellingButton fires the callback registered via
     * {@link RoomView#onRoomSellingButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The check-in/selling button action listener is not wired to the onRoomSellingButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenRoomSellingButtonClicked() throws Exception {
        RoomView view = new RoomView();
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onRoomSellingButton(() -> invoked.set(true));
        clickButton(view, "roomSellingButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the endTimeButton fires the callback registered via
     * {@link RoomView#onEndTimeButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The checkout/end-time button action listener is not wired to the onEndTimeButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenEndTimeButtonClicked() throws Exception {
        RoomView view = new RoomView();
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onEndTimeButton(() -> invoked.set(true));
        clickButton(view, "endTimeButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the addTimeButton fires the callback registered via
     * {@link RoomView#onAddTimeButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The add-time/extend-time button action listener is not wired to the onAddTimeButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenAddTimeButtonClicked() throws Exception {
        RoomView view = new RoomView();
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onAddTimeButton(() -> invoked.set(true));
        clickButton(view, "addTimeButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the roomChangeButton fires the callback registered via
     * {@link RoomView#onRoomChangeButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The room-switch/change button action listener is not wired to the onRoomChangeButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenRoomChangeButtonClicked() throws Exception {
        RoomView view = new RoomView();
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onRoomChangeButton(() -> invoked.set(true));
        clickButton(view, "roomChangeButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking any booking-hour button (3h, 12h, 24h) fires the callbacks registered via
     * {@link RoomView#onBookingHourButton(int, Runnable)}.
     * Expected: Each slot's AtomicBoolean is set to true when its corresponding button is clicked.
     * Failure: The booking-hour button action listeners are not wired to the onBookingHourButton callbacks,
     *          or the slot indices (0, 1, 2) are mapped incorrectly.
     */
    @Test
    void shouldInvokeCallbackWhenBookingHourButtonClicked() throws Exception {
        RoomView view = new RoomView();
        AtomicBoolean slot0 = new AtomicBoolean(false);
        AtomicBoolean slot1 = new AtomicBoolean(false);
        AtomicBoolean slot2 = new AtomicBoolean(false);
        view.onBookingHourButton(0, () -> slot0.set(true));
        view.onBookingHourButton(1, () -> slot1.set(true));
        view.onBookingHourButton(2, () -> slot2.set(true));

        clickButton(view, "booking3HoursButton");
        assertThat(slot0).isTrue();

        clickButton(view, "booking12HoursButton");
        assertThat(slot1).isTrue();

        clickButton(view, "booking24HoursButton");
        assertThat(slot2).isTrue();
    }

    /**
     * Verifies that clicking any price-adjust button (-100, +100, -1000, +1000) fires the callbacks
     * registered via {@link RoomView#onPriceAdjust(int, Runnable)}.
     * Expected: Each adjustment amount's AtomicBoolean is set to true when its corresponding button is clicked.
     * Failure: The price-adjust button action listeners are not wired to the onPriceAdjust callbacks,
     *          or the amounts are mapped to the wrong buttons.
     */
    @Test
    void shouldInvokeCallbackWhenPriceAdjustButtonClicked() throws Exception {
        RoomView view = new RoomView();
        AtomicBoolean[] invoked = new AtomicBoolean[4];
        for (int i = 0; i < 4; i++) invoked[i] = new AtomicBoolean(false);

        view.onPriceAdjust(-100, () -> invoked[0].set(true));
        view.onPriceAdjust(100, () -> invoked[1].set(true));
        view.onPriceAdjust(-1000, () -> invoked[2].set(true));
        view.onPriceAdjust(1000, () -> invoked[3].set(true));

        clickButton(view, "removeSmallQuantityButton");
        assertThat(invoked[0]).isTrue();

        clickButton(view, "addSmallQuantityButton");
        assertThat(invoked[1]).isTrue();

        clickButton(view, "removeBigQuantity");
        assertThat(invoked[2]).isTrue();

        clickButton(view, "addBigQuantityButton");
        assertThat(invoked[3]).isTrue();
    }

    /**
     * Verifies that the display price can be set and retrieved, and that the underlying
     * priceTextField reflects the same value.
     * Expected: {@link RoomView#getDisplayPrice()} returns the value set by {@link RoomView#setDisplayPrice(String)},
     *           and the priceTextField component shows the same text.
     * Failure: The price text field is not updated when setDisplayPrice is called, or getDisplayPrice
     *          does not read from the correct component.
     */
    @Test
    void shouldRoundtripDisplayPrice() throws Exception {
        RoomView view = new RoomView();
        view.setDisplayPrice("2500");

        assertThat(view.getDisplayPrice()).isEqualTo("2500");
        JTextField field = (JTextField) getField(view, "priceTextField");
        assertThat(field.getText()).isEqualTo("2500");
    }

    /**
     * Verifies that {@link RoomView#setRoomInfo(String, String, String)} correctly updates the
     * roomNumber, statusLabel, and roomStatusInformative JLabel components.
     * Expected: All three labels display the exact strings passed to setRoomInfo.
     * Failure: Room info labels are not wired or are incorrectly updated, indicating a mismatch
     *          between the method parameters and the target label fields.
     */
    @Test
    void shouldSetRoomInfoLabels() throws Exception {
        RoomView view = new RoomView();
        view.setRoomInfo("T1-101", "OCUPADO", "3h restantes");

        assertThat(textOf(view, "roomNumber")).isEqualTo("T1-101");
        assertThat(textOf(view, "statusLabel")).isEqualTo("OCUPADO");
        assertThat(textOf(view, "roomStatusInformative")).isEqualTo("3h restantes");
    }

    /**
     * Verifies that {@link RoomView#setOvertimeWarning(boolean)} changes the background color of
     * the roomStatusBackground panel to the overtime warning color (amber: 241, 196, 15).
     * Expected: When setOvertimeWarning(true) is called, the panel background is set to amber.
     * Failure: The overtime warning visual indicator is not wired to the roomStatusBackground panel,
     *          or setOvertimeWarning does not apply the correct color.
     */
    @Test
    void shouldSetOvertimeWarningAppearance() throws Exception {
        RoomView view = new RoomView();
        view.setOvertimeWarning(true);
        JPanel panel = (JPanel) getField(view, "roomStatusBackground");
        assertThat(panel.getBackground()).isEqualTo(new Color(241, 196, 15));
    }

    /**
     * Verifies that {@link RoomView#setBookingButtonHighlight(int)} applies the highlight color
     * (green: 103, 159, 51) to the specified booking-hour button.
     * Expected: The booking3HoursButton background is green after calling setBookingButtonHighlight(0).
     * Failure: The booking button highlight logic does not correctly set the background color,
     *          or the button field lookup/slot-to-button mapping is broken.
     */
    @Test
    void shouldHighlightBookingButton() throws Exception {
        RoomView view = new RoomView();
        view.setBookingButtonHighlight(0);
        assertThat(getBookingButtonBackground(view, "booking3HoursButton")).isEqualTo(new Color(103, 159, 51));
    }

    /**
     * Verifies that {@link RoomView#resetBookingHighlights()} clears all booking button backgrounds
     * back to white after a highlight has been applied.
     * Expected: After setBookingButtonHighlight(0) followed by resetBookingHighlights(),
     *           the booking3HoursButton background is Color.WHITE.
     * Failure: The reset method does not clear previously applied highlights, leaving stale visual state.
     */
    @Test
    void shouldResetBookingHighlights() throws Exception {
        RoomView view = new RoomView();
        view.setBookingButtonHighlight(0);
        view.resetBookingHighlights();
        assertThat(getBookingButtonBackground(view, "booking3HoursButton")).isEqualTo(Color.WHITE);
    }

    /**
     * Verifies that {@link RoomView#isPrintSelected()} correctly reflects the selected state of
     * the printingCheckBox JCheckBox.
     * Expected: isPrintSelected() returns false initially, and true after the checkbox is selected.
     * Failure: The isPrintSelected() method reads from the wrong checkbox or does not properly
     *          reflect the component's selected state.
     */
    @Test
    void shouldReflectPrintCheckboxState() throws Exception {
        RoomView view = new RoomView();
        JCheckBox cb = (JCheckBox) getField(view, "printingCheckBox");
        assertThat(view.isPrintSelected()).isFalse();
        cb.setSelected(true);
        assertThat(view.isPrintSelected()).isTrue();
    }

    /**
     * Verifies that {@link RoomView#setRoomNumber(String)} correctly updates the roomNumber label.
     * Expected: The roomNumber JLabel displays "P2-305" after setRoomNumber("P2-305") is called.
     * Failure: The setRoomNumber method does not update the correct label component, or the label
     *          field name has changed.
     */
    @Test
    void shouldSetRoomNumber() throws Exception {
        RoomView view = new RoomView();
        view.setRoomNumber("P2-305");
        assertThat(textOf(view, "roomNumber")).isEqualTo("P2-305");
    }

    /**
     * Verifies that {@link RoomView#setStartTimeLabel(String)}, {@link RoomView#setRemainingTimeLabel(String)},
     * and {@link RoomView#setStartDateLabel(String)} correctly update their respective JLabel components.
     * Expected: Each label displays the exact string passed to its setter method.
     * Failure: The time/date label setters are not wired to the correct JLabel fields, or the field
     *          names (startTimeLabel, remainingTimeLabel, startDateLabel) have changed.
     */
    @Test
    void shouldSetStartAndRemainingTimeLabels() throws Exception {
        RoomView view = new RoomView();
        view.setStartTimeLabel("10:30 AM");
        view.setRemainingTimeLabel("1h 30min");
        view.setStartDateLabel("23 de mayo de 2026");
        assertThat(textOf(view, "startTimeLabel")).isEqualTo("10:30 AM");
        assertThat(textOf(view, "remainingTimeLabel")).isEqualTo("1h 30min");
        assertThat(textOf(view, "startDateLabel")).isEqualTo("23 de mayo de 2026");
    }

    /**
     * Verifies that {@link RoomView#setAddTimeEnabled(boolean)} correctly enables or disables the
     * addTimeButton component.
     * Expected: After setAddTimeEnabled(false), the addTimeButton's enabled state is false.
     * Failure: The setAddTimeEnabled method does not correctly control the button's enabled state,
     *          or the addTimeButton field name has changed.
     */
    @Test
    void shouldSetAddTimeEnabled() throws Exception {
        RoomView view = new RoomView();
        view.setAddTimeEnabled(false);
        AbstractButton btn = (AbstractButton) getField(view, "addTimeButton");
        assertThat(btn.isEnabled()).isFalse();
    }

    // --- helpers ---

    private static void clickButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }

    private static Object getField(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(parent);
    }

    private static String textOf(Component parent, String fieldName) throws Exception {
        JLabel label = (JLabel) getField(parent, fieldName);
        return label.getText();
    }

    private static Color getBookingButtonBackground(Component parent, String fieldName) throws Exception {
        AbstractButton btn = (AbstractButton) getField(parent, fieldName);
        return btn.getBackground();
    }
}
