package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtraTurnChangesViewTest {

    private ExtraTurnChangesView view;

    @BeforeEach
    void setUp() {
        view = new ExtraTurnChangesView();
    }

    /**
     * Verifies that clicking the backButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the back button.
     * Failure: The backButton's action listener is not wired to the onBackButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the confirmationButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the confirmation button.
     * Failure: The confirmationButton's action listener is not wired to the onConfirmationButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenConfirmationButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onConfirmationButton(() -> invoked.set(true));
        clickButton(view, "confirmationButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that description text set on the underlying JTextField can be read back via getDescriptionText().
     * Expected: The getter returns the same value that was set on the text field.
     * Failure: The descriptionText field is not correctly referenced or getDescriptionText() returns a different value.
     */
    @Test
    void shouldRoundtripDescriptionText() throws Exception {
        JTextField field = (JTextField) getField(view, "descriptionText");
        field.setText("Pago de servicios");
        assertThat(view.getDescriptionText()).isEqualTo("Pago de servicios");
    }

    /**
     * Verifies that value text set via setValueText() can be read back via getValueText().
     * Expected: setValueText("50000") results in getValueText() returning "50000".
     * Failure: The value text setter/getter pair is broken or the underlying field is not properly connected.
     */
    @Test
    void shouldRoundtripValueText() {
        view.setValueText("50000");
        assertThat(view.getValueText()).isEqualTo("50000");
    }

    /**
     * Verifies that the bank transfer checkbox state can be roundtripped.
     * Expected: isBankTransferSelected() is false by default, becomes true after setBankTransferSelected(true).
     * Failure: The bank transfer checkbox wiring is broken or the default state is incorrect.
     */
    @Test
    void shouldRoundtripBankTransferSelected() throws Exception {
        assertThat(view.isBankTransferSelected()).isFalse();
        view.setBankTransferSelected(true);
        assertThat(view.isBankTransferSelected()).isTrue();
    }

    /**
     * Verifies that the safe deposit checkbox state can be roundtripped.
     * Expected: isSafeDepositSelected() is false by default, becomes true after setSafeDepositSelected(true).
     * Failure: The safe deposit checkbox wiring is broken or the default state is incorrect.
     */
    @Test
    void shouldRoundtripSafeDepositSelected() throws Exception {
        assertThat(view.isSafeDepositSelected()).isFalse();
        view.setSafeDepositSelected(true);
        assertThat(view.isSafeDepositSelected()).isTrue();
    }

    /**
     * Verifies that clearFields() resets all input fields to their default empty state.
     * Expected: After setting value and selecting checkboxes, clearFields() resets valueText to "0"
     *           and deselects both bank transfer and safe deposit checkboxes.
     * Failure: clearFields() does not reset all fields or leaves stale state behind.
     */
    @Test
    void shouldClearAllFields() {
        view.setValueText("50000");
        view.setBankTransferSelected(true);
        view.clearFields();
        assertThat(view.getValueText()).isEqualTo("0");
        assertThat(view.isBankTransferSelected()).isFalse();
        assertThat(view.isSafeDepositSelected()).isFalse();
    }

    /**
     * Verifies that setConfirmationEnabled(false) can be called without throwing exceptions.
     * Expected: The method completes normally, confirming the enable/disable API is wired.
     * Failure: The confirmation button or its enabled state setter is not properly initialized,
     *          causing a NullPointerException.
     */
    @Test
    void shouldSetConfirmationEnabled() {
        view.setConfirmationEnabled(false);
    }

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
}
