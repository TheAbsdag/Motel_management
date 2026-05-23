package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpendingRegisterViewTest {

    private SpendingRegisterView view;

    @BeforeEach
    void setUp() {
        view = new SpendingRegisterView();
    }

    /**
     * Verifies that clicking the cancellationButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the cancellation button.
     * Failure: The cancellationButton's action listener is not wired to the onCancellationButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenCancellationButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onCancellationButton(() -> invoked.set(true));
        clickButton(view, "cancellationButton");
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
     * Expected: The getter returns the same value that was set on the descriptionChangeText text field.
     * Failure: The descriptionChangeText field is not correctly referenced or getDescriptionText() returns a different value.
     */
    @Test
    void shouldRoundtripDescriptionText() throws Exception {
        JTextField field = (JTextField) getField(view, "descriptionChangeText");
        field.setText("Compra de insumos");
        assertThat(view.getDescriptionText()).isEqualTo("Compra de insumos");
    }

    /**
     * Verifies that value text set via setValueText() can be read back via getValueText().
     * Expected: setValueText("15000") results in getValueText() returning "15000".
     * Failure: The value text setter/getter pair is broken or the underlying field is not properly connected.
     */
    @Test
    void shouldRoundtripValueText() {
        view.setValueText("15000");
        assertThat(view.getValueText()).isEqualTo("15000");
    }

    /**
     * Verifies that clearFields() resets the value text field to "0".
     * Expected: After calling setValueText("15000") followed by clearFields(), getValueText() returns "0".
     * Failure: clearFields() does not reset the value text field or the reset value is not "0".
     */
    @Test
    void shouldClearAllFields() {
        view.setValueText("15000");
        view.clearFields();
        assertThat(view.getValueText()).isEqualTo("0");
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
