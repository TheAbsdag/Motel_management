package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import model.dto.TurnHistoryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryViewTest {

    private HistoryView view;

    @BeforeEach
    void setUp() {
        view = new HistoryView();
    }

    /**
     * Verifies that clicking the backButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the back button.
     * Failure: The back button's action listener is not wired to the onBackButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the turnDetailsButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the turn details button.
     * Failure: The turn details button's action listener is not wired to the onTurnDetailsButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenTurnDetailsButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onTurnDetailsButton(() -> invoked.set(true));
        clickButton(view, "turnDetailsButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the upButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the up button (navigation).
     * Failure: The up button's action listener is not wired to the onUpButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenUpButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onUpButton(() -> invoked.set(true));
        clickButton(view, "upButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the downButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the down button (navigation).
     * Failure: The down button's action listener is not wired to the onDownButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenDownButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDownButton(() -> invoked.set(true));
        clickButton(view, "downButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that setting turn history details to an empty list resets the selected row.
     * Expected: getSelectedTurnRow() returns -1 after setTurnHistoryDetails with an empty list.
     * Failure: The selected row is not reset to -1 when an empty history list is provided.
     */
    @Test
    void shouldSetTurnHistoryDetails() {
        view.setTurnHistoryDetails(List.of());
        assertThat(view.getSelectedTurnRow()).isEqualTo(-1);
    }

    /**
     * Verifies that the turn details button enabled state can be toggled without exception.
     * Expected: setTurnDetailsEnabled(false) completes without throwing.
     * Failure: The method throws an unexpected exception, possibly due to a missing UI component.
     */
    @Test
    void shouldSetTurnDetailsEnabled() {
        view.setTurnDetailsEnabled(false);
    }

    /**
     * Verifies that all four selected-turn info labels are updated correctly.
     * Expected: setSelectedTurnInfo populates turnStartLabel, turnEndLabel, turnDateLabel, and durationLabel.
     * Failure: One or more labels are not updated, indicating a broken setter or renamed fields.
     */
    @Test
    void shouldSetSelectedTurnInfo() throws Exception {
        view.setSelectedTurnInfo("10:00 AM", "2:00 PM", "23 de mayo", "4h");
        assertThat(textOf(view, "turnStartLabel")).isEqualTo("10:00 AM");
        assertThat(textOf(view, "turnEndLabel")).isEqualTo("2:00 PM");
        assertThat(textOf(view, "turnDateLabel")).isEqualTo("23 de mayo");
        assertThat(textOf(view, "durationLabel")).isEqualTo("4h");
    }

    /**
     * Verifies that the embedded TurnHistoryManagerView is not null after construction.
     * Expected: getTurnDetailsView() returns a non-null TurnHistoryManagerView instance.
     * Failure: The embedded details view was not created or the getter returns null.
     */
    @Test
    void shouldHaveEmbeddedTurnDetailsView() {
        TurnHistoryManagerView details = view.getTurnDetailsView();
        assertThat(details).isNotNull();
    }

    /**
     * Smoke test verifying that updateTimeDisplay can be called without throwing an exception.
     * Expected: No exception is thrown when updating the time/date labels.
     * Failure: A NullPointerException or other runtime exception indicates missing label components.
     */
    @Test
    void shouldUpdateTimeDisplay() {
        view.updateTimeDisplay("12:00 PM", "23 de mayo");
    }

    private static void clickButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }

    private static String textOf(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        JLabel label = (JLabel) field.get(parent);
        return label.getText();
    }
}
