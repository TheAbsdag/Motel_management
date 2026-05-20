package view.helpers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * Adds a visible focus border to text fields so users can see which field
 * is active for virtual-keyboard input (important on Linux touch-screen kiosks).
 *
 * <p>Usage: after a view's components are initialized, call
 * {@link #applyToAll(Container)} to walk the component tree and decorate
 * every {@link JTextField}.</p>
 *
 * <p>Tapping outside any text field clears focus, which dismisses the
 * on-screen virtual keyboard on touchscreen environments.</p>
 */
public class FocusHighlighter {

    private static final Color FOCUS_COLOR = new Color(0x0078D7);
    private static final int FOCUS_THICKNESS = 3;

    private FocusHighlighter() {
    }

    /**
     * Walks the container tree and adds focus highlighting to every
     * {@link JTextField} found. Also installs a tap-dismiss listener
     * on the root container so that tapping outside any text field
     * clears focus (hiding the on-screen keyboard on touchscreens).
     */
    public static void applyToAll(Container root) {
        walk(root);
        installDismissOnOutsideTap(root);
    }

    private static void walk(Container container) {
        for (Component child : container.getComponents()) {
            if (child instanceof JTextField textField) {
                Border original = textField.getBorder();

                textField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        textField.setBorder(BorderFactory.createLineBorder(FOCUS_COLOR, FOCUS_THICKNESS));
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        textField.setBorder(original);
                    }
                });
            } else if (child instanceof Container childContainer) {
                walk(childContainer);
            }
        }
    }

    /**
     * Installs a mouse listener that clears keyboard focus when the user
     * taps or clicks on a non-interactive area of the container (e.g. a
     * label, panel, or empty space). This causes the on-screen virtual
     * keyboard to dismiss on touchscreen environments.
     */
    private static void installDismissOnOutsideTap(Container root) {
        root.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Component clicked = SwingUtilities.getDeepestComponentAt(root, e.getX(), e.getY());
                if (clicked == null) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                    return;
                }
                // Walk up the hierarchy; if any ancestor is focusable,
                // the tap is on an interactive element (button, table,
                // text field, etc.) so leave focus alone.
                Component c = clicked;
                while (c != null && c != root) {
                    if (c.isFocusable()) {
                        return;
                    }
                    c = c.getParent();
                }
                // Tap on a passive area — dismiss the keyboard
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
            }
        });
    }
}
