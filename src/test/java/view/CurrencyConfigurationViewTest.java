package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import model.json.CurrencyConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyConfigurationViewTest {

    private CurrencyConfigurationView view;

    @BeforeEach
    void setUp() {
        view = new CurrencyConfigurationView();
    }

    // --- callbacks ---

    /**
     * Verifies that clicking the backButton fires the callback registered via
     * {@link CurrencyConfigurationView#onBackButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The back button action listener is not wired to the onBackButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the saveButton fires the callback registered via
     * {@link CurrencyConfigurationView#onSaveButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The save button action listener is not wired to the onSaveButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenSaveButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onSaveButton(() -> invoked.set(true));
        clickButton(view, "saveButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that {@link CurrencyConfigurationView#removeSaveListeners()} detaches previously
     * registered save callbacks, as required by the first-boot re-wiring flow.
     * Expected: The AtomicBoolean stays false after the button click.
     * Failure: removeSaveListeners leaves a stale listener attached, so first boot would run both flows.
     */
    @Test
    void shouldNotInvokeSaveCallbackAfterListenersRemoved() throws Exception {
        AtomicBoolean firstCallback = new AtomicBoolean(false);
        view.onSaveButton(() -> firstCallback.set(true));
        view.removeSaveListeners();
        clickButton(view, "saveButton");
        assertThat(firstCallback).isFalse();
    }

    // --- state round trips ---

    /**
     * Verifies that a configuration loaded through {@link CurrencyConfigurationView#populate(CurrencyConfig)}
     * is reported back unchanged by {@link CurrencyConfigurationView#toConfig()}.
     * Expected: The round-tripped record equals the original (code, decimals, symbol, symbol position).
     * Failure: One of the form fields does not retain what populate wrote into it.
     */
    @Test
    void shouldRoundtripConfigurationThroughPopulateAndToConfig() {
        CurrencyConfig original = new CurrencyConfig("EUR", 2, "\u20AC", false);
        view.populate(original);
        assertThat(view.toConfig()).isEqualTo(original);
    }

    /**
     * Verifies that choosing a currency code applies that currency's default symbol and decimals.
     * Expected: JPY yields "\u00A5" with 0 decimals, EUR yields "\u20AC" with 2 decimals.
     * Failure: The codeComboBox listener no longer maps the selection onto the symbol/decimals fields.
     */
    @Test
    void shouldApplyDefaultsWhenCurrencyCodeSelected() {
        view.setCurrencyCode("JPY");
        assertThat(view.getSymbol()).isEqualTo("\u00A5");
        assertThat(view.getDecimalPlaces()).isZero();

        view.setCurrencyCode("EUR");
        assertThat(view.getSymbol()).isEqualTo("\u20AC");
        assertThat(view.getDecimalPlaces()).isEqualTo(2);
    }

    /**
     * Verifies that the preview label is recomputed from the current form values when a field changes.
     * Expected: The label shows the formatted sample amount using the configured symbol and 0 decimals.
     * Failure: The document listener no longer calls updatePreview, leaving a stale preview.
     */
    @Test
    void shouldUpdatePreviewWhenSymbolChanges() throws Exception {
        view.setSymbol("\u20AC");
        assertThat(textOf(view, "previewLabel")).isEqualTo("Vista previa: \u20AC 40,000");
    }

    /**
     * Verifies dirty tracking: the view reports clean by default and dirty after a field edit.
     * Expected: isDirty() is false initially, true after editing a field, and false again after clearDirty().
     * Failure: The dirty flag is not being updated (or cleared) when form fields are modified.
     */
    @Test
    void shouldTrackDirtyStateAndClear() {
        assertThat(view.isDirty()).isFalse();

        view.setSymbol("$");
        assertThat(view.isDirty()).isTrue();

        view.clearDirty();
        assertThat(view.isDirty()).isFalse();
    }

    /**
     * Verifies that {@link CurrencyConfigurationView#setBackEnabled(boolean)} hides and disables the
     * back button, as required by the first-boot flow where there is nowhere to go back to.
     * Expected: The back button is neither visible nor enabled after setBackEnabled(false).
     * Failure: The first-boot flow leaves an active back button on screen.
     */
    @Test
    void shouldDisableBackButton() throws Exception {
        view.setBackEnabled(false);
        AbstractButton backButton = (AbstractButton) getField(view, "backButton");
        assertThat(backButton.isVisible()).isFalse();
        assertThat(backButton.isEnabled()).isFalse();
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

    private static String textOf(Component view, String fieldName) throws Exception {
        JLabel label = (JLabel) getField(view, fieldName);
        return label.getText();
    }
}
