package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import model.dto.TurnActivityData;
import model.dto.TurnHistoryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurnHistoryManagerViewTest {

    private TurnHistoryManagerView view;

    @BeforeEach
    void setUp() {
        view = new TurnHistoryManagerView();
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
     * Verifies that clicking the printButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the print button.
     * Failure: The print button's action listener is not wired to the onPrintButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenPrintButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onPrintButton(() -> invoked.set(true));
        clickButton(view, "printButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that TurnHistoryData is correctly propagated to the view labels.
     * Expected: setTurnDetailsData with turn number 42 results in turnNumberLabel showing "42".
     * Failure: The TurnHistoryData DTO is not being mapped to the view labels, or field names changed.
     */
    @Test
    void shouldSetTurnDetailsData() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        TurnHistoryData data = new TurnHistoryData(
                42, now, now, 50000, 10, 5, 2000, 3000, 52000, 10000, 20000, 42000,
                "23 de mayo", "4h", "10:00 AM", "2:00 PM", List.of());

        view.setTurnDetailsData(data);
        assertThat(textOf(view, "turnNumberLabel")).isEqualTo("42");
    }

    /**
     * Verifies that the noPrintCheckBox selection state is reflected in isNoPrintSelected().
     * Expected: isNoPrintSelected() returns false by default and true after selecting the checkbox.
     * Failure: The checkbox selection state is not synchronized with the isNoPrintSelected() method.
     */
    @Test
    void shouldReflectNoPrintSelected() throws Exception {
        JCheckBox cb = (JCheckBox) getField(view, "noPrintCheckBox");
        assertThat(view.isNoPrintSelected()).isFalse();
        cb.setSelected(true);
        assertThat(view.isNoPrintSelected()).isTrue();
    }

    /**
     * Verifies that the summarizedPrintCheckBox selection state is reflected in isSummarizedPrintSelected().
     * Expected: isSummarizedPrintSelected() returns false by default and true after selecting the checkbox.
     * Failure: The checkbox selection state is not synchronized with the isSummarizedPrintSelected() method.
     */
    @Test
    void shouldReflectSummarizedPrintSelected() throws Exception {
        JCheckBox cb = (JCheckBox) getField(view, "summarizedPrintCheckBox");
        assertThat(view.isSummarizedPrintSelected()).isFalse();
        cb.setSelected(true);
        assertThat(view.isSummarizedPrintSelected()).isTrue();
    }

    /**
     * Verifies that the detailedPrintCheckBox selection state is reflected in isDetailedPrintSelected().
     * Expected: isDetailedPrintSelected() returns false by default and true after selecting the checkbox.
     * Failure: The checkbox selection state is not synchronized with the isDetailedPrintSelected() method.
     */
    @Test
    void shouldReflectDetailedPrintSelected() throws Exception {
        JCheckBox cb = (JCheckBox) getField(view, "detailedPrintCheckBox");
        assertThat(view.isDetailedPrintSelected()).isFalse();
        cb.setSelected(true);
        assertThat(view.isDetailedPrintSelected()).isTrue();
    }

    /**
     * Verifies that the print button enabled state can be toggled without exception.
     * Expected: setPrintEnabled(false) completes without throwing.
     * Failure: The method throws an unexpected exception, possibly due to a missing UI component.
     */
    @Test
    void shouldSetPrintEnabled() {
        view.setPrintEnabled(false);
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

    private static String textOf(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        JLabel label = (JLabel) field.get(parent);
        return label.getText();
    }
}
