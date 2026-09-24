package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppOptionsViewTest {

    private AppOptionsView view;

    @BeforeEach
    void setUp() {
        view = new AppOptionsView();
    }

    /**
     * Verifies that clicking the printerOptionsButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the printer options button.
     * Failure: The printerOptionsButton's action listener is not wired to the onPrinterOptions callback.
     */
    @Test
    void shouldInvokeCallbackWhenPrinterOptionsClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onPrinterOptions(() -> invoked.set(true));
        clickButton(view, "printerOptionsButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the dataConfigurationButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the data configuration button.
     * Failure: The dataConfigurationButton's action listener is not wired to the onDataConfiguration callback.
     */
    @Test
    void shouldInvokeCallbackWhenDataConfigurationClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDataConfiguration(() -> invoked.set(true));
        clickButton(view, "dataConfigurationButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the dateAndTimeConfigurationButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the date/time configuration button.
     * Failure: The dateAndTimeConfigurationButton's action listener is not wired to the onDateTimeConfiguration callback.
     */
    @Test
    void shouldInvokeCallbackWhenDateTimeConfigurationClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDateTimeConfiguration(() -> invoked.set(true));
        clickButton(view, "dateAndTimeConfigurationButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the floorConfigurationButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the floor configuration button.
     * Failure: The floorConfigurationButton's action listener is not wired to the onFloorConfiguration callback.
     */
    @Test
    void shouldInvokeCallbackWhenFloorConfigurationClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onFloorConfiguration(() -> invoked.set(true));
        clickButton(view, "floorConfigurationButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the saveConfigurationButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the save configuration button.
     * Failure: The saveConfigurationButton's action listener is not wired to the onSaveConfiguration callback.
     */
    @Test
    void shouldInvokeCallbackWhenSaveConfigurationClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onSaveConfiguration(() -> invoked.set(true));
        clickButton(view, "saveConfigurationButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the backButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the back button.
     * Failure: The backButton's action listener is not wired to the onBackButton callback,
     *          preventing navigation back from the app options screen.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that updateTimeDisplay() can be called without throwing exceptions.
     * Expected: The method completes normally, confirming the time display API is wired.
     * Failure: The time display labels are not properly initialized, causing a NullPointerException.
     */
    @Test
    void shouldUpdateTimeDisplay() {
        view.updateTimeDisplay("12:00 PM", "23 de mayo");
    }

    // --- on-screen keypad switch ---

    /**
     * Verifies that toggling the switch fires the callback registered via
     * {@link AppOptionsView#onKeypadChange(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the switch is clicked.
     * Failure: The switch is not wired, so the setting can never be stored.
     */
    @Test
    void shouldInvokeCallbackWhenKeypadSwitchToggled() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onKeypadChange(() -> invoked.set(true));

        clickButton(view, "keypadCheckBox");

        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that the switch starts on, which is what a touchscreen installation needs,
     * and that the stored setting can be shown on it.
     * Expected: selected by default, false after setKeypadSelected(false), true after (true).
     * Failure: the switch does not reflect what is stored.
     */
    @Test
    void shouldShowTheStoredKeypadSetting() {
        assertThat(view.isKeypadSelected()).isTrue();

        view.setKeypadSelected(false);
        assertThat(view.isKeypadSelected()).isFalse();

        view.setKeypadSelected(true);
        assertThat(view.isKeypadSelected()).isTrue();
    }

    private static void clickButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }
}
