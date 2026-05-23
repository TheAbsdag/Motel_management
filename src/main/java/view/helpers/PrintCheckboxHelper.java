package view.helpers;

import java.awt.Component;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;

/**
 * Helper for wiring mutually exclusive print checkboxes (no-print / summarized / detailed).
 *
 * <p>When one checkbox is selected the other two are cleared and the associated
 * print-related buttons are enabled.
 */
public final class PrintCheckboxHelper {

    private PrintCheckboxHelper() {
    }

    /**
     * Creates an {@link ItemListener} that manages the three checkboxes and
     * enables the provided component(s) when any checkbox is selected.
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
            JCheckBox selected = (JCheckBox) e.getSource();
            if (selected.isSelected()) {
                primaryButton.setEnabled(true);
                if (secondaryButton != null && secondaryButton != primaryButton) {
                    secondaryButton.setEnabled(true);
                }
                if (selected != noPrint)    noPrint.setSelected(false);
                if (selected != summarized) summarized.setSelected(false);
                if (selected != detailed)   detailed.setSelected(false);
            }
        };
    }
}
