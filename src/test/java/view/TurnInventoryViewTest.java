package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import model.dto.TurnInventoryRowData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurnInventoryViewTest {

    private TurnInventoryView view;

    @BeforeEach
    void setUp() {
        view = new TurnInventoryView();
    }

    /**
     * Verifies that the back button is wired to the callback.
     * Expected: the flag is set after the button click.
     * Failure: the action listener is not registered.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that the up button is wired to the callback.
     * Expected: the flag is set after the button click.
     * Failure: the action listener is not registered.
     */
    @Test
    void shouldInvokeCallbackWhenUpButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onUpButton(() -> invoked.set(true));
        clickButton(view, "upButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that the down button is wired to the callback.
     * Expected: the flag is set after the button click.
     * Failure: the action listener is not registered.
     */
    @Test
    void shouldInvokeCallbackWhenDownButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDownButton(() -> invoked.set(true));
        clickButton(view, "downButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that the turn number is shown in the header.
     * Expected: the label reads "TURNO #7".
     * Failure: setTurnNumber is not wired to the turn label.
     */
    @Test
    void shouldShowTheTurnNumber() throws Exception {
        view.setTurnNumber(7);
        assertThat(textOf(view, "turnLabel")).isEqualTo("TURNO #7");
    }

    /**
     * Verifies the table columns and values of one row.
     * Expected: seven columns with name, received, increase, total, sold,
     * courtesy and current stock.
     * Failure: a column returns another value or the headers changed.
     */
    @Test
    void shouldUpdateInventoryRowsInTheTable() {
        view.updateInventoryRows(List.of(
                new TurnInventoryRowData(1L, "Cerveza", 4, 6, 2, 1, 7)));

        JTable table = tableOf(view);
        assertThat(table.getRowCount()).isEqualTo(1);
        assertThat(table.getColumnCount()).isEqualTo(7);
        assertThat(table.getColumnName(3)).isEqualTo("Total Ingresado");
        assertThat(table.getValueAt(0, 0)).isEqualTo("Cerveza");
        assertThat(table.getValueAt(0, 1)).isEqualTo(4L);
        assertThat(table.getValueAt(0, 2)).isEqualTo(6L);
        assertThat(table.getValueAt(0, 3)).isEqualTo(10L);
        assertThat(table.getValueAt(0, 4)).isEqualTo(2L);
        assertThat(table.getValueAt(0, 5)).isEqualTo(1L);
        assertThat(table.getValueAt(0, 6)).isEqualTo(7L);
    }

    /**
     * Verifies that an empty list clears the table.
     * Expected: zero rows.
     * Failure: updateInventoryRows does not replace the previous rows.
     */
    @Test
    void shouldClearTheTableWithAnEmptyList() {
        view.updateInventoryRows(List.of());
        assertThat(tableOf(view).getRowCount()).isZero();
    }

    /**
     * Verifies that the clock labels are updated.
     * Expected: both labels hold the received texts.
     * Failure: updateTimeDisplay writes another component.
     */
    @Test
    void shouldUpdateTimeDisplay() throws Exception {
        view.updateTimeDisplay("10:30 PM", "18 de noviembre");
        assertThat(textOf(view, "timeLabel")).isEqualTo("10:30 PM");
        assertThat(textOf(view, "dateLabel")).isEqualTo("18 de noviembre");
    }

    private static JTable tableOf(TurnInventoryView view) {
        try {
            Field field = view.getClass().getDeclaredField("inventoryTable");
            field.setAccessible(true);
            return (JTable) field.get(view);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
