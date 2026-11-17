package view.helpers;

import java.awt.Component;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;

/**
 * Helper for wiring mutually exclusive print checkboxes (no-print / summarized / detailed).
 *
 * <p>The three checkboxes behave as a single mandatory choice: when one is selected the
 * other two are cleared and the associated print-related buttons are enabled; when no
 * checkbox is selected any more the buttons are disabled again, so the user cannot print
 * or end the turn without choosing a valid option.
 */
public final class PrintCheckboxHelper {

    private PrintCheckboxHelper() {
    }

    /**
     * Creates an {@link ItemListener} that keeps the three checkboxes mutually exclusive
     * and enables the provided component(s) only while at least one option is selected.
     *
     * @param noPrint        the "no print" checkbox
     * @param summarized     the "summarized print" checkbox
     * @param detailed       the "detailed print" checkbox
     * @param primaryButton  the primary button to enable (usually "print")
     * @param secondaryButton an optional second button to also enable (may be the same as primary)
     * @return an ItemListener for all three checkboxes
     */
    public static ItemListener createPrintCheckboxListener(
            JCheckBox noPrint,
            JCheckBox summarized,
            JCheckBox detailed,
            Component primaryButton,
            Component secondaryButton) {

        return e -> {
            JCheckBox changed = (JCheckBox) e.getSource();
            if (changed.isSelected()) {
                if (changed != noPrint)    noPrint.setSelected(false);
                if (changed != summarized) summarized.setSelected(false);
                if (changed != detailed)   detailed.setSelected(false);
            }
            boolean anySelected = noPrint.isSelected() || summarized.isSelected() || detailed.isSelected();
            primaryButton.setEnabled(anySelected);
            if (secondaryButton != null && secondaryButton != primaryButton) {
                secondaryButton.setEnabled(anySelected);
            }
        };
    }
}
