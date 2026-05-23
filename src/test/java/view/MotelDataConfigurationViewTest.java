package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MotelDataConfigurationViewTest {

    private MotelDataConfigurationView view;

    @BeforeEach
    void setUp() {
        view = new MotelDataConfigurationView();
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
     * Verifies that clicking the saveButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the save button.
     * Failure: The save button's action listener is not wired to the onSaveButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenSaveButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onSaveButton(() -> invoked.set(true));
        clickButton(view, "saveButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that the motel name text field supports round-trip get/set.
     * Expected: set motel name text via setNameText and reading it back via getNameText returns the same value.
     * Failure: The name text field internal state is not properly stored or retrieved.
     */
    @Test
    void shouldRoundtripNameText() {
        view.setNameText("Hotel Test");
        assertThat(view.getNameText()).isEqualTo("Hotel Test");
    }

    /**
     * Verifies that the motel ID text field supports round-trip get/set.
     * Expected: set motel ID text via setIdText and reading it back via getIdText returns the same value.
     * Failure: The motel ID text field internal state is not properly stored or retrieved.
     */
    @Test
    void shouldRoundtripIdText() {
        view.setIdText("12345678-9");
        assertThat(view.getIdText()).isEqualTo("12345678-9");
    }

    /**
     * Verifies that the motel address text field supports round-trip get/set.
     * Expected: set address text via setAddressText and reading it back via getAddressText returns the same value.
     * Failure: The address text field internal state is not properly stored or retrieved.
     */
    @Test
    void shouldRoundtripAddressText() {
        view.setAddressText("Calle 123 #45-67");
        assertThat(view.getAddressText()).isEqualTo("Calle 123 #45-67");
    }

    /**
     * Verifies dirty tracking: the view reports clean by default and dirty after a text change.
     * Expected: isDirty() returns false initially, then true after changing the motel name text.
     * Failure: The dirty flag is not being updated when form fields are modified.
     */
    @Test
    void shouldTrackDirtyState() {
        assertThat(view.isDirty()).isFalse();
        view.setNameText("Changed");
        assertThat(view.isDirty()).isTrue();
    }

    /**
     * Verifies that the dirty flag can be cleared explicitly after changes.
     * Expected: isDirty() returns true after a text change, then false after calling clearDirty().
     * Failure: The clearDirty method does not reset the dirty tracking flag properly.
     */
    @Test
    void shouldClearDirtyState() {
        view.setNameText("Changed");
        assertThat(view.isDirty()).isTrue();
        view.clearDirty();
        assertThat(view.isDirty()).isFalse();
    }

    /**
     * Verifies that the consecutive transaction label is updated correctly.
     * Expected: setConsecutiveTransaction("42") causes the label text to be "42".
     * Failure: The consecutive transaction label is not being updated, or the field name has changed.
     */
    @Test
    void shouldSetConsecutiveTransaction() throws Exception {
        view.setConsecutiveTransaction("42");
        JLabel label = (JLabel) getField(view, "conescutiveTransactionLabel");
        assertThat(label.getText()).isEqualTo("42");
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
