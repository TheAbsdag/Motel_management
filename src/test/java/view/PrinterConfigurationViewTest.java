package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrinterConfigurationViewTest {

    private PrinterConfigurationView view;

    @BeforeEach
    void setUp() {
        view = new PrinterConfigurationView();
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
     * Verifies that clicking the confirmPrinterButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the confirm printer button.
     * Failure: The confirmPrinterButton's action listener is not wired to the onConfirmPrinterButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenConfirmPrinterClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onConfirmPrinterButton(() -> invoked.set(true));
        clickButton(view, "confirmPrinterButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that setPrinterUsedText() updates the printerUsedLabel JLabel text.
     * Expected: After calling setPrinterUsedText("Microsoft Print to PDF"), the label reflects that text.
     * Failure: The setter does not update the label or the label field is named incorrectly.
     */
    @Test
    void shouldSetPrinterUsedText() throws Exception {
        view.setPrinterUsedText("Microsoft Print to PDF");
        assertThat(textOf(view, "printerUsedLabel")).isEqualTo("Microsoft Print to PDF");
    }

    /**
     * Verifies that setSelectedPrinterText() updates the selectedPrinterLabel JLabel text.
     * Expected: After calling setSelectedPrinterText("Seleccionada: PDF"), the label reflects that text.
     * Failure: The setter does not update the selected printer label or the label field is named incorrectly.
     */
    @Test
    void shouldSetSelectedPrinterText() throws Exception {
        view.setSelectedPrinterText("Seleccionada: PDF");
        assertThat(textOf(view, "selectedPrinterLabel")).isEqualTo("Seleccionada: PDF");
    }

    /**
     * Verifies that setPrinterListModel() applies a list model and resets selection.
     * Expected: After setting a model with two printers, getSelectedPrinterIndex() returns -1 (no selection).
     * Failure: The printer list or selection index is not properly initialized after model assignment.
     */
    @Test
    void shouldSetPrinterListModel() {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("Printer A");
        model.addElement("Printer B");
        view.setPrinterListModel(model);
        assertThat(view.getSelectedPrinterIndex()).isEqualTo(-1);
    }

    /**
     * Verifies that setConfirmPrinterEnabled(false) can be called without throwing exceptions.
     * Expected: The method completes normally, confirming the enable/disable API is wired.
     * Failure: The confirm printer button or its enabled state setter is not properly initialized,
     *          causing a NullPointerException.
     */
    @Test
    void shouldSetConfirmPrinterEnabled() {
        view.setConfirmPrinterEnabled(false);
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
