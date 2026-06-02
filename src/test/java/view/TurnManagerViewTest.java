package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import model.json.CurrencyConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.helpers.CurrencyFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class TurnManagerViewTest {

    private TurnManagerView view;

    @BeforeEach
    void setUp() {
        view = new TurnManagerView();
    }

    // --- callbacks ---

    /**
     * Verifies that clicking the backButton fires the callback registered via
     * {@link TurnManagerView#onBackButton(Runnable)}.
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
     * Verifies that clicking the printButton fires the callback registered via
     * {@link TurnManagerView#onPrintButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The print button action listener is not wired to the onPrintButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenPrintButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onPrintButton(() -> invoked.set(true));
        clickButton(view, "printButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the endTurnButton fires the callback registered via
     * {@link TurnManagerView#onEndTurnButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The end-turn button action listener is not wired to the onEndTurnButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenEndTurnButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onEndTurnButton(() -> invoked.set(true));
        clickButton(view, "endTurnButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the refundButton fires the callback registered via
     * {@link TurnManagerView#onRefundButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The refund button action listener is not wired to the onRefundButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenRefundButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onRefundButton(() -> invoked.set(true));
        clickButton(view, "refundButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the summarizedTurnButton fires the callback registered via
     * {@link TurnManagerView#onSummarizedTurn(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The summarized-turn button action listener is not wired to the onSummarizedTurn callback.
     */
    @Test
    void shouldInvokeCallbackWhenSummarizedTurnButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onSummarizedTurn(() -> invoked.set(true));
        clickButton(view, "summarizedTurnButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the backFromSummarizedTurn button fires the callback registered via
     * {@link TurnManagerView#onBackFromSummarizedTurn(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The back-from-summarized-turn button action listener is not wired to the onBackFromSummarizedTurn callback.
     */
    @Test
    void shouldInvokeCallbackWhenBackFromSummarizedTurnClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackFromSummarizedTurn(() -> invoked.set(true));
        clickButton(view, "backFromSummarizedTurn");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the upButton fires the callback registered via
     * {@link TurnManagerView#onUpButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The up/navigate-up button action listener is not wired to the onUpButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenUpButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onUpButton(() -> invoked.set(true));
        clickButton(view, "upButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the downButton fires the callback registered via
     * {@link TurnManagerView#onDownButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The down/navigate-down button action listener is not wired to the onDownButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenDownButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDownButton(() -> invoked.set(true));
        clickButton(view, "downButton");
        assertThat(invoked).isTrue();
    }

    // --- print checkboxes ---

    /**
     * Verifies that the three print checkboxes (noPrint, summarized, detailed) are mutually exclusive
     * via the PrintCheckboxHelper configured in {@link TurnManagerView#setupPrintCheckboxes()}.
     * Expected: Selecting one checkbox deselects the other two in each round (noPrint -> summarized -> detailed).
     * Failure: The mutual exclusion logic in PrintCheckboxHelper is not wired correctly, or the checkboxes
     *          allow multiple selections simultaneously.
     */
    @Test
    void shouldSelectOnlyOnePrintCheckbox() throws Exception {
        view.setupPrintCheckboxes();

        JCheckBox noPrint = (JCheckBox) getField(view, "noPrintCheckBox");
        JCheckBox summarized = (JCheckBox) getField(view, "summarizedPrintCheckBox");
        JCheckBox detailed = (JCheckBox) getField(view, "detailedPrintCheckBox");

        noPrint.setSelected(true);
        assertThat(noPrint.isSelected()).isTrue();
        assertThat(summarized.isSelected()).isFalse();
        assertThat(detailed.isSelected()).isFalse();

        summarized.setSelected(true);
        assertThat(noPrint.isSelected()).isFalse();
        assertThat(summarized.isSelected()).isTrue();
        assertThat(detailed.isSelected()).isFalse();

        detailed.setSelected(true);
        assertThat(noPrint.isSelected()).isFalse();
        assertThat(summarized.isSelected()).isFalse();
        assertThat(detailed.isSelected()).isTrue();
    }

    /**
     * Verifies that {@link TurnManagerView#isNoPrintSelected()} correctly reflects the selected state
     * of the noPrintCheckBox.
     * Expected: isNoPrintSelected() returns false initially, and true after the checkbox is selected.
     * Failure: The isNoPrintSelected() method reads from the wrong checkbox.
     */
    @Test
    void shouldReflectNoPrintSelected() throws Exception {
        JCheckBox cb = (JCheckBox) getField(view, "noPrintCheckBox");
        assertThat(view.isNoPrintSelected()).isFalse();
        cb.setSelected(true);
        assertThat(view.isNoPrintSelected()).isTrue();
    }

    /**
     * Verifies that {@link TurnManagerView#isSummarizedPrintSelected()} correctly reflects the selected state
     * of the summarizedPrintCheckBox.
     * Expected: isSummarizedPrintSelected() returns false initially, and true after the checkbox is selected.
     * Failure: The isSummarizedPrintSelected() method reads from the wrong checkbox.
     */
    @Test
    void shouldReflectSummarizedPrintSelected() throws Exception {
        JCheckBox cb = (JCheckBox) getField(view, "summarizedPrintCheckBox");
        assertThat(view.isSummarizedPrintSelected()).isFalse();
        cb.setSelected(true);
        assertThat(view.isSummarizedPrintSelected()).isTrue();
    }

    /**
     * Verifies that {@link TurnManagerView#isDetailedPrintSelected()} correctly reflects the selected state
     * of the detailedPrintCheckBox.
     * Expected: isDetailedPrintSelected() returns false initially, and true after the checkbox is selected.
     * Failure: The isDetailedPrintSelected() method reads from the wrong checkbox.
     */
    @Test
    void shouldReflectDetailedPrintSelected() throws Exception {
        JCheckBox cb = (JCheckBox) getField(view, "detailedPrintCheckBox");
        assertThat(view.isDetailedPrintSelected()).isFalse();
        cb.setSelected(true);
        assertThat(view.isDetailedPrintSelected()).isTrue();
    }

    // --- state ---

    /**
     * Verifies that {@link TurnManagerView#setTurnDetailsData(List, int, int, long, long, long, long, long, long, long)}
     * correctly updates the totalRoomsLabel, totalItemsLabel, and totalSalesLabel JLabel components.
     * Expected: The labels display the formatted numeric values passed to the method.
     * Failure: The turn details data setter is not wired to the correct label fields, or the
     *          parameter-to-label mapping is incorrect.
     */
    @Test
    void shouldSetTurnDetailsData() throws Exception {
        view.setTurnDetailsData(
                List.of(), 5, 10, 50000, 2000, 3000, 52000, 10000, 20000, 42000);

        assertThat(textOf(view, "totalRoomsLabel")).isEqualTo(CurrencyFormatter.format(5, CurrencyConfig.defaultConfig()));
        assertThat(textOf(view, "totalItemsLabel")).isEqualTo(CurrencyFormatter.format(10, CurrencyConfig.defaultConfig()));
        assertThat(textOf(view, "totalSalesLabel")).isEqualTo(CurrencyFormatter.format(50000, CurrencyConfig.defaultConfig()));
    }

    /**
     * Verifies that {@link TurnManagerView#updateSummarizedTurnData(List)} accepts a list of
     * TurnSummaryItemData without throwing exceptions.
     * Expected: The method completes successfully when called with one TurnSummaryItemData element.
     * Failure: The updateSummarizedTurnData method throws an unexpected exception, indicating a bug
     *          in the summarized turn data rendering pipeline.
     */
    @Test
    void shouldUpdateSummarizedTurnData() {
        List<TurnSummaryItemData> items = List.of(
                new TurnSummaryItemData("item", 5, 12500, "Coca-Cola", 0));
        view.updateSummarizedTurnData(items);
    }

    /**
     * Verifies that the button enabled-state setters ({@link TurnManagerView#setBackEnabled(boolean)},
     * {@link TurnManagerView#setPrintEnabled(boolean)}, {@link TurnManagerView#setEndTurnEnabled(boolean)},
     * {@link TurnManagerView#setRefundEnabled(boolean)}) complete without throwing exceptions.
     * Expected: All four setEnabled calls complete successfully with false.
     * Failure: Any setter throws an exception, indicating a missing or misnamed button field.
     */
    @Test
    void shouldSetButtonEnabledStates() {
        view.setBackEnabled(false);
        view.setPrintEnabled(false);
        view.setEndTurnEnabled(false);
        view.setRefundEnabled(false);
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
