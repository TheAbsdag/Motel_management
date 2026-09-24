package view.helpers;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Covers the on-screen keypad and the fields it is attached to. The keys are pressed
 * directly, so nothing has to be shown on screen.
 */
class NumericKeypadTest {

    private JTextField field;
    private NumericKeypad keypad;

    @BeforeEach
    void setUp() {
        field = new JTextField();
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        keypad = new NumericKeypad();
        keypad.setTarget(field);
    }

    @AfterEach
    void tearDown() {
        NumericKeypadPopup.setEnabled(true);
    }

    // --- the keypad itself ---

    /**
     * Verifies that the twelve keys are laid out as a 3x4 pad: three columns and four rows,
     * the shape a numeric keypad is expected to have.
     * Expected: 3 distinct x positions and 4 distinct y positions among the keys.
     * Failure: the keys end up in a single long row (MigLayout without a wrap), which is
     *          hard to hit on a touchscreen.
     */
    @Test
    void shouldLayTheKeysOutAsAThreeByFourPad() {
        keypad.setSize(240, 260);
        keypad.doLayout();

        Set<Integer> columns = new HashSet<>();
        Set<Integer> rows = new HashSet<>();
        for (Component component : keypad.getComponents()) {
            columns.add(component.getX());
            rows.add(component.getY());
        }

        assertThat(columns).hasSize(3);
        assertThat(rows).hasSize(4);
    }

    /**
     * Verifies that the digits land in the bound field through its document, so the filter
     * and the listeners of the field still apply.
     * Expected: pressing 4, 5 and 0 leaves "450" in the field.
     * Failure: the keypad writes around the field and the value never reaches the screen.
     */
    @Test
    void shouldTypeDigitsIntoTheBoundField() {
        keypad.press("4");
        keypad.press("5");
        keypad.press("0");

        assertThat(field.getText()).isEqualTo("450");
    }

    /**
     * Verifies that the first key replaces a selected value instead of being appended to it.
     * Expected: with 40000 selected, pressing 4 and 5 leaves "45".
     * Failure: the value keeps growing (4000045) instead of being replaced.
     */
    @Test
    void shouldReplaceTheSelectedValueOnTheFirstKey() {
        field.setText("40000");
        field.selectAll();

        keypad.press("4");
        keypad.press("5");

        assertThat(field.getText()).isEqualTo("45");
    }

    /**
     * Verifies that the clear key empties the field.
     * Expected: an empty field after pressing C.
     * Failure: the old value stays and the user cannot start over.
     */
    @Test
    void shouldClearTheField() {
        field.setText("40000");

        keypad.press(NumericKeypad.CLEAR_KEY);

        assertThat(field.getText()).isEmpty();
    }

    /**
     * Verifies that the backspace key removes one character at a time and stops at the
     * beginning of the value.
     * Expected: 40000 -> 4000, and no error on an empty field.
     * Failure: backspace removes everything or throws.
     */
    @Test
    void shouldRemoveOneCharacterAtATime() {
        field.setText("40000");

        keypad.press(NumericKeypad.BACKSPACE_KEY);

        assertThat(field.getText()).isEqualTo("4000");

        field.setText("");
        keypad.press(NumericKeypad.BACKSPACE_KEY);
        assertThat(field.getText()).isEmpty();
    }

    /**
     * Verifies that backspace acts on the value the field shows rather than on a selection:
     * a keypad has no selection concept, and C is the key that empties the field.
     * Expected: with 40000 selected, one backspace leaves 4000 and C leaves nothing.
     * Failure: the keypad behaves like a text field and the two keys become unpredictable.
     */
    @Test
    void shouldKeepBackspaceAndClearDistinct() {
        field.setText("40000");
        field.selectAll();

        keypad.press(NumericKeypad.BACKSPACE_KEY);
        assertThat(field.getText()).isEqualTo("4000");

        keypad.press(NumericKeypad.CLEAR_KEY);
        assertThat(field.getText()).isEmpty();
    }

    /**
     * Verifies that the keypad does nothing without a target field.
     * Expected: no exception and no change.
     * Failure: a null target breaks the dialog it is used in.
     */
    @Test
    void shouldDoNothingWithoutATarget() {
        keypad.setTarget(null);

        keypad.press("4");
        keypad.press(NumericKeypad.CLEAR_KEY);
        keypad.press(NumericKeypad.BACKSPACE_KEY);

        assertThat(field.getText()).isEmpty();
    }

    // --- attaching it to a field ---

    /**
     * Verifies that the popup of a field is a keypad bound to that same field, with a
     * button to close it.
     * Expected: the keys write into the field and the popup has an OK button.
     * Failure: the keypad types into another field or cannot be dismissed.
     */
    @Test
    void shouldBindThePopupToTheFieldItWasBuiltFor() {
        JPopupMenu popup = NumericKeypadPopup.create(field);

        NumericKeypad popupKeypad = childrenOf(popup, NumericKeypad.class).get(0);
        assertThat(popupKeypad.getTarget()).isSameAs(field);

        pressKey(popupKeypad, "7");
        assertThat(field.getText()).isEqualTo("7");

        assertThat(childrenOf(popup, JButton.class))
                .extracting(JButton::getText)
                .contains("OK");
    }

    /**
     * Verifies that attaching the keypad adds the tap listener that opens it.
     * Expected: one more mouse listener on the field.
     * Failure: tapping a numeric field does nothing.
     */
    @Test
    void shouldListenForTapsAfterAttaching() {
        int listenersBefore = field.getMouseListeners().length;

        NumericKeypadPopup.attach(field);

        assertThat(field.getMouseListeners().length).isEqualTo(listenersBefore + 1);
    }

    /**
     * Verifies that an installation with a keyboard can turn the keypad off, and that
     * turning it back on restores it.
     * Expected: the flag follows the setting.
     * Failure: the keypad cannot be disabled and it opens over every field.
     */
    @Test
    void shouldFollowTheStoredSetting() {
        assertThat(NumericKeypadPopup.isEnabled()).isTrue();

        NumericKeypadPopup.setEnabled(false);
        assertThat(NumericKeypadPopup.isEnabled()).isFalse();

        NumericKeypadPopup.setEnabled(true);
        assertThat(NumericKeypadPopup.isEnabled()).isTrue();
    }

    /**
     * Verifies that tapping an attached field does nothing while the keypad is switched off,
     * which is what an installation with a keyboard needs: the field is left to the keyboard
     * and no popup covers the screen.
     * Expected: the tap neither opens the popup nor throws.
     * Failure: the setting is ignored and the pad opens anyway.
     */
    @Test
    void shouldNotOpenTheKeypadWhenSwitchedOff() {
        NumericKeypadPopup.setEnabled(false);
        NumericKeypadPopup.attach(field);

        assertThatCode(this::tapOnField).doesNotThrowAnyException();
        assertThat(field.getText()).isEmpty();
    }

    // --- helpers ---

    /** Sends the tap the popup listens for to the first mouse listener of the field. */
    private void tapOnField() {
        for (java.awt.event.MouseListener listener : field.getMouseListeners()) {
            listener.mousePressed(new MouseEvent(field, MouseEvent.MOUSE_PRESSED,
                    System.currentTimeMillis(), 0, 5, 5, 1, false, MouseEvent.BUTTON1));
        }
    }

    private static void pressKey(NumericKeypad keypad, String label) {
        for (Component component : keypad.getComponents()) {
            JButton button = (JButton) component;
            if (label.equals(button.getText())) {
                button.doClick();
                return;
            }
        }
        throw new AssertionError("no key labelled " + label + " on the keypad");
    }

    private static <T> List<T> childrenOf(Component parent, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (parent instanceof java.awt.Container container) {
            for (Component child : container.getComponents()) {
                if (type.isInstance(child)) {
                    found.add(type.cast(child));
                } else {
                    found.addAll(childrenOf(child, type));
                }
            }
        }
        return found;
    }
}
