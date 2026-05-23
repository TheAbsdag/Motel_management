package view.helpers;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableScrollerTest {

    private JTable table;

    @BeforeEach
    void setUp() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Col"}, 0);
        for (int i = 0; i < 10; i++) {
            model.addRow(new Object[]{"Row " + i});
        }
        table = new JTable(model);
        table.setRowSelectionInterval(5, 5);
    }

    /**
     * Verifies that {@link TableScroller#scroll(JTable, int)} moves the selection
     * down by one row when the delta is positive.
     * Expected: Starting at row 5, after scrolling +1 the selected row becomes 6.
     * Failure: The selection does not advance, advances by the wrong amount, or
     *          moves in the opposite direction.
     */
    @Test
    void shouldScrollDownOneRow() {
        TableScroller.scroll(table, 1);
        assertThat(table.getSelectedRow()).isEqualTo(6);
    }

    /**
     * Verifies that {@link TableScroller#scroll(JTable, int)} moves the selection
     * up by one row when the delta is negative.
     * Expected: Starting at row 5, after scrolling -1 the selected row becomes 4.
     * Failure: The selection does not retreat, retreats by the wrong amount, or
     *          moves in the opposite direction.
     */
    @Test
    void shouldScrollUpOneRow() {
        TableScroller.scroll(table, -1);
        assertThat(table.getSelectedRow()).isEqualTo(4);
    }

    /**
     * Verifies that {@link TableScroller#scroll(JTable, int)} does not scroll
     * past the last row of the table, clamping at the bottom boundary.
     * Expected: Starting at the last row (row 9) and scrolling +1 keeps the
     *           selection at row 9.
     * Failure: The selection advances past the last valid row, causing an
     *          {@code IndexOutOfBoundsException} or silent selection corruption.
     */
    @Test
    void shouldNotScrollPastLastRow() {
        table.setRowSelectionInterval(9, 9);
        TableScroller.scroll(table, 1);
        assertThat(table.getSelectedRow()).isEqualTo(9);
    }

    /**
     * Verifies that {@link TableScroller#scroll(JTable, int)} does not scroll
     * past the first row of the table, clamping at the top boundary.
     * Expected: Starting at the first row (row 0) and scrolling -1 keeps the
     *           selection at row 0.
     * Failure: The selection becomes negative, causing an
     *          {@code IndexOutOfBoundsException} or invalid selection state.
     */
    @Test
    void shouldNotScrollPastFirstRow() {
        table.setRowSelectionInterval(0, 0);
        TableScroller.scroll(table, -1);
        assertThat(table.getSelectedRow()).isEqualTo(0);
    }

    /**
     * Verifies that {@link TableScroller#scroll(JTable, int)} gracefully handles
     * an empty table (zero rows) without throwing an exception.
     * Expected: The call completes without throwing any exception.
     * Failure: An empty table causes an {@code IndexOutOfBoundsException} or
     *          other runtime error, crashing the scroll handler.
     */
    @Test
    void shouldHandleEmptyTable() {
        JTable empty = new JTable(new DefaultTableModel(new Object[]{"Col"}, 0));
        TableScroller.scroll(empty, 1);
    }

    /**
     * Verifies that {@link TableScroller#scroll(JTable, int)} selects the first
     * row when no row is currently selected and a positive delta is given.
     * Expected: After clearing the selection and scrolling +1, row 0 is selected.
     * Failure: The table remains without a selection or a wrong row is selected,
     *          breaking keyboard navigation when focus first lands on the table.
     */
    @Test
    void shouldHandleNoSelection() {
        table.clearSelection();
        TableScroller.scroll(table, 1);
        assertThat(table.getSelectedRow()).isEqualTo(0);
    }
}
