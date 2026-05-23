package view.helpers;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrintCheckboxHelperTest {

    private JCheckBox noPrint;
    private JCheckBox summarized;
    private JCheckBox detailed;
    private JButton printButton;
    private ItemListener listener;

    @BeforeEach
    void setUp() {
        noPrint = new JCheckBox();
        summarized = new JCheckBox();
        detailed = new JCheckBox();
        printButton = new JButton();
        printButton.setEnabled(false);

        listener = PrintCheckboxHelper.createPrintCheckboxListener(
                noPrint, summarized, detailed, printButton, null);
        noPrint.addItemListener(listener);
        summarized.addItemListener(listener);
        detailed.addItemListener(listener);
    }

    /**
     * Verifies that selecting the "no print" checkbox enables the print button
     * and deselects the "summarized" and "detailed" checkboxes, enforcing mutual
     * exclusivity.
     * Expected: Button is enabled; only "no print" remains selected.
     * Failure: Mutually-exclusive deselection does not occur, or the button
     *          remains disabled, blocking the user from proceeding.
     */
    @Test
    void shouldEnableButtonWhenNoPrintSelected() {
        noPrint.setSelected(true);
        fireItemEvent(noPrint, ItemEvent.SELECTED);

        assertThat(printButton.isEnabled()).isTrue();
        assertThat(summarized.isSelected()).isFalse();
        assertThat(detailed.isSelected()).isFalse();
    }

    /**
     * Verifies that selecting the "summarized" checkbox enables the print button
     * and deselects the "no print" and "detailed" checkboxes.
     * Expected: Button is enabled; only "summarized" remains selected.
     * Failure: The listener does not enforce mutual exclusivity for the
     *          summarized option, allowing conflicting selections.
     */
    @Test
    void shouldEnableButtonWhenSummarizedSelected() {
        summarized.setSelected(true);
        fireItemEvent(summarized, ItemEvent.SELECTED);

        assertThat(printButton.isEnabled()).isTrue();
        assertThat(noPrint.isSelected()).isFalse();
        assertThat(detailed.isSelected()).isFalse();
    }

    /**
     * Verifies that selecting the "detailed" checkbox enables the print button
     * and deselects the "no print" and "summarized" checkboxes.
     * Expected: Button is enabled; only "detailed" remains selected.
     * Failure: The listener does not enforce mutual exclusivity for the
     *          detailed option, allowing multiple print options to coexist.
     */
    @Test
    void shouldEnableButtonWhenDetailedSelected() {
        detailed.setSelected(true);
        fireItemEvent(detailed, ItemEvent.SELECTED);

        assertThat(printButton.isEnabled()).isTrue();
        assertThat(noPrint.isSelected()).isFalse();
        assertThat(summarized.isSelected()).isFalse();
    }

    /**
     * Verifies that switching the selection from one checkbox to another
     * correctly deselects the previously selected option, maintaining strict
     * mutual exclusivity across all three print-option checkboxes.
     * Expected: Selecting "summarized" then "detailed" leaves only "detailed"
     *           selected, with "no print" and "summarized" both deselected.
     * Failure: Previously selected checkboxes remain selected, violating the
     *          single-selection contract and potentially causing ambiguous
     *          print behavior.
     */
    @Test
    void shouldDeselectOthersWhenSwitchingSelection() {
        summarized.setSelected(true);
        fireItemEvent(summarized, ItemEvent.SELECTED);

        assertThat(noPrint.isSelected()).isFalse();
        assertThat(detailed.isSelected()).isFalse();
        assertThat(summarized.isSelected()).isTrue();

        // Now select detailed -- should deselect summarized
        detailed.setSelected(true);
        fireItemEvent(detailed, ItemEvent.SELECTED);

        assertThat(noPrint.isSelected()).isFalse();
        assertThat(summarized.isSelected()).isFalse();
        assertThat(detailed.isSelected()).isTrue();
    }

    private static void fireItemEvent(JCheckBox source, int stateChange) {
        ItemEvent event = new ItemEvent(source, ItemEvent.ITEM_STATE_CHANGED,
                source, stateChange);
        for (ItemListener l : source.getItemListeners()) {
            l.itemStateChanged(event);
        }
    }
}
