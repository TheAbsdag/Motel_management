package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;

/**
 * Adds a visible focus border to text fields so users can see which field
 * is active for virtual-keyboard input (important on Linux touch-screen kiosks).
 *
 * <p>Usage: after a view's components are initialized, call
 * {@link #applyToAll(Container)} to walk the component tree and decorate
 * every {@link JTextField}.</p>
 */
public class FocusHighlighter {

    private static final Color FOCUS_COLOR = new Color(0x0078D7);
    private static final int FOCUS_THICKNESS = 3;

    private FocusHighlighter() {
    }

    /**
     * Walks the container tree and adds focus highlighting to every
     * {@link JTextField} found.
     */
    public static void applyToAll(Container root) {
        Map<JTextField, Border> originalBorders = new HashMap<>();

        walk(root, originalBorders);
    }

    private static void walk(Container container, Map<JTextField, Border> borderStore) {
        for (Component child : container.getComponents()) {
            if (child instanceof JTextField textField) {
                Border original = textField.getBorder();
                borderStore.put(textField, original);

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
                walk(childContainer, borderStore);
            }
        }
    }
}
