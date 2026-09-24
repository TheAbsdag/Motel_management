package view.helpers;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

/**
 * Opens a {@link NumericKeypad} under a text field when it is tapped, so a value can be
 * entered on the touchscreen without a keyboard. Typing on a physical keyboard keeps
 * working, and the field's own filter and listeners are untouched.
 *
 * <p>Attaching it to a field is a single call after the view is initialized:
 * <pre>{@code NumericKeypadPopup.attach(valueTextField);}</pre>
 * The popup is created in code, so no form (`.jfd`) has to change.
 *
 * @author SECC
 */
public final class NumericKeypadPopup {

    private static final Font DONE_FONT = new Font("Segoe UI Black", Font.PLAIN, 22);

    /**
     * Whether tapping a field opens the keypad. Installations that have a keyboard turn it
     * off; the setting is loaded at start-up and can be changed from the options screen.
     */
    private static boolean enabled = true;

    private NumericKeypadPopup() { }

    /**
     * Enables or disables the keypad for every field it is attached to.
     *
     * @param keypadEnabled true to open the keypad on a tap, false to leave the fields to
     *                      the keyboard
     */
    public static void setEnabled(boolean keypadEnabled) {
        enabled = keypadEnabled;
    }

    /** @return true when a tap on an attached field opens the keypad */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Makes a field open the keypad when it is tapped, as long as the keypad is enabled.
     *
     * @param field field the keypad types into; null is ignored
     */
    public static void attach(JTextField field) {
        if (field == null) return;
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (enabled) {
                    show(field);
                }
            }
        });
    }

    /**
     * Opens the keypad under the field, with the value it holds selected, so the first key
     * replaces it instead of appending to it.
     *
     * @param field field the keypad types into
     */
    public static void show(JTextField field) {
        JPopupMenu popup = create(field);
        field.requestFocusInWindow();
        field.selectAll();
        popup.show(field, 0, field.getHeight());
    }

    /**
     * Builds the popup of a field without showing it, so the keys can be exercised without
     * a display.
     *
     * @param field field the keypad types into
     * @return the popup: the keypad in the centre and the closing button at the bottom
     */
    static JPopupMenu create(JTextField field) {
        NumericKeypad keypad = new NumericKeypad();
        keypad.setTarget(field);

        JPopupMenu popup = new JPopupMenu();
        popup.setLayout(new BorderLayout());
        popup.add(keypad, BorderLayout.CENTER);

        JButton done = new JButton("OK");
        done.setFont(DONE_FONT);
        done.setFocusable(false);
        done.addActionListener(e -> popup.setVisible(false));
        popup.add(done, BorderLayout.SOUTH);
        return popup;
    }
}
