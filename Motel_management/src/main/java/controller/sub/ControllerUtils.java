package controller.sub;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JTable;

/**
 * Shared utilities used across multiple sub-controllers
 * to reduce code duplication.
 */
public final class ControllerUtils {

    private ControllerUtils() {
        // utility class — do not instantiate
    }

    /**
     * Scrolls the given table by one row in the specified direction.
     * Touch-friendly replacement for Robot-based key simulation.
     *
     * @param table     the table to scroll
     * @param direction +1 for down, -1 for up
     */
    public static void scrollTable(JTable table, int direction) {
        int currentRow = table.getSelectedRow();
        int targetRow = Math.max(0, Math.min(currentRow + direction, table.getRowCount() - 1));
        if (targetRow >= 0) {
            table.setRowSelectionInterval(targetRow, targetRow);
            table.scrollRectToVisible(table.getCellRect(targetRow, 0, true));
        }
    }

    /**
     * Creates an {@link ItemListener} that manages mutually exclusive print checkboxes.
     * When the selected checkbox is checked, the other two are unchecked and the
     * associated print button is enabled.
     *
     * @param noPrintCheckBox        the "no print" checkbox
     * @param summarizedPrintCheckBox the "summarized print" checkbox
     * @param detailedPrintCheckBox  the "detailed print" checkbox
     * @param printButton            the button to enable on selection
     * @param activateButton         an optional button to also enable (may be the same as printButton)
     * @return an ItemListener suitable for attaching to all three checkboxes
     */
    public static ItemListener createPrintCheckboxListener(
            JCheckBox noPrintCheckBox,
            JCheckBox summarizedPrintCheckBox,
            JCheckBox detailedPrintCheckBox,
            Component printButton,
            Component activateButton) {

        return e -> {
            JCheckBox selected = (JCheckBox) e.getSource();
            if (selected.isSelected()) {
                printButton.setEnabled(true);
                if (activateButton != null && activateButton != printButton) {
                    activateButton.setEnabled(true);
                }
                if (selected != noPrintCheckBox) noPrintCheckBox.setSelected(false);
                if (selected != summarizedPrintCheckBox) summarizedPrintCheckBox.setSelected(false);
                if (selected != detailedPrintCheckBox) detailedPrintCheckBox.setSelected(false);
            }
        };
    }
}
