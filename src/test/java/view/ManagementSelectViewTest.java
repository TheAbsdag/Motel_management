package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManagementSelectViewTest {

    private ManagementSelectView view;

    @BeforeEach
    void setUp() {
        view = new ManagementSelectView();
    }

    /**
     * Verifies that clicking the turnButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the turn button.
     * Failure: The turn button's action listener is not wired to the onTurnButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenTurnButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onTurnButton(() -> invoked.set(true));
        clickButton(view, "turnButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the inventoryButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the inventory button.
     * Failure: The inventory button's action listener is not wired to the onInventoryButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenInventoryButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onInventoryButton(() -> invoked.set(true));
        clickButton(view, "inventoryButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the historyButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after enabling and clicking the history button.
     * Failure: The history button's action listener is not wired to the onHistoryButton callback,
     *          or the button is disabled by default and the test fails to enable it first.
     */
    @Test
    void shouldInvokeCallbackWhenHistoryButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onHistoryButton(() -> invoked.set(true));
        AbstractButton btn = getButton(view, "historyButton");
        btn.setEnabled(true);
        btn.doClick();
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the appOptionsButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the app options button.
     * Failure: The app options button's action listener is not wired to the onAppOptionsButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenAppOptionsButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onAppOptionsButton(() -> invoked.set(true));
        clickButton(view, "appOptionsButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the roomSummaryButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the room summary button.
     * Failure: The room summary button's action listener is not wired to the onRoomSummaryButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenRoomSummaryButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onRoomSummaryButton(() -> invoked.set(true));
        clickButton(view, "roomSummaryButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the extraChangesButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the extra changes button.
     * Failure: The extra changes button's action listener is not wired to the onExtraChangesButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenExtraChangesButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onExtraChangesButton(() -> invoked.set(true));
        clickButton(view, "extraChangesButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the registerSpendingButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the register spending button.
     * Failure: The register spending button's action listener is not wired to the onRegisterSpendingButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenRegisterSpendingButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onRegisterSpendingButton(() -> invoked.set(true));
        clickButton(view, "registerSpendingButton");
        assertThat(invoked).isTrue();
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
     * Verifies that the management info label is set to the expected value.
     * Expected: setManagementInfo("Turno #42 activo") causes the label to display that exact text.
     * Failure: The management info label is not being updated or the field name has changed.
     */
    @Test
    void shouldSetManagementInfo() throws Exception {
        view.setManagementInfo("Turno #42 activo");
        assertThat(textOf(view, "managementInfoLabel")).isEqualTo("Turno #42 activo");
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

    private static AbstractButton getButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (AbstractButton) field.get(parent);
    }

    private static String textOf(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        JLabel label = (JLabel) field.get(parent);
        return label.getText();
    }
}
