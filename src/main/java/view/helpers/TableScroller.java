package view.helpers;

import javax.swing.JTable;

/**
 * Touch-friendly table row scrolling utility.
 */
public final class TableScroller {

    private TableScroller() {
    }

    /**
     * Scrolls the given table by one row in the specified direction.
     *
     * @param table     the table to scroll
     * @param direction +1 for down, -1 for up
     */
    public static void scroll(JTable table, int direction) {
        if (table.getRowCount() == 0) {
            return;
        }
        int currentRow = table.getSelectedRow();
        int targetRow = Math.max(0, Math.min(currentRow + direction, table.getRowCount() - 1));
        if (targetRow >= 0) {
            table.setRowSelectionInterval(targetRow, targetRow);
            table.scrollRectToVisible(table.getCellRect(targetRow, 0, true));
        }
    }
}
