package view.helpers;

import java.awt.Font;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * On-screen numeric keypad for the touchscreen: digits, clear-all and backspace, written
 * into the text field it is bound to.
 *
 * <p>Everything is typed through the field itself ({@code replaceSelection} / document
 * edits), so the field's own {@link NumericDocumentFilter} keeps validating the value and
 * its document listeners keep seeing every change — a value entered here is saved exactly
 * like a typed one.
 *
 * @author SECC
 */
public class NumericKeypad extends JPanel {

    /** Clear-all key of the keypad. */
    public static final String CLEAR_KEY = "C";

    /** Backspace key of the keypad. */
    public static final String BACKSPACE_KEY = "\u2190";

    /** Keys in layout order: three columns of digits, then clear-all and backspace. */
    private static final String[] KEYS = {
        "7", "8", "9",
        "4", "5", "6",
        "1", "2", "3",
        CLEAR_KEY, "0", BACKSPACE_KEY
    };

    private static final Font KEY_FONT = new Font("Segoe UI Black", Font.PLAIN, 22);
    private static final Insets KEY_MARGIN = new Insets(8, 10, 8, 10);

    private JTextField target;

    public NumericKeypad() {
        super(new MigLayout("insets 4", "[grow,fill][grow,fill][grow,fill]"));
        for (String key : KEYS) {
            JButton keyButton = new JButton(key);
            keyButton.setFont(KEY_FONT);
            keyButton.setMargin(KEY_MARGIN);
            keyButton.setFocusable(false);
            keyButton.addActionListener(e -> press(key));
            add(keyButton);
        }
    }

    /**
     * Binds the keypad to the field that receives what is typed.
     *
     * @param field target field, or null to make the keypad inert
     */
    public void setTarget(JTextField field) {
        this.target = field;
    }

    /** @return the field the keypad writes into */
    public JTextField getTarget() {
        return target;
    }

    /**
     * Applies one key of the keypad to the bound field. Keys act on the value the field
     * shows: the first key after the popup opened (which selects the value) replaces it,
     * later keys are appended at the end.
     *
     * @param key a digit, {@link #CLEAR_KEY} or {@link #BACKSPACE_KEY}
     */
    void press(String key) {
        if (target == null) return;
        String text = target.getText() == null ? "" : target.getText();
        if (CLEAR_KEY.equals(key)) {
            target.setText("");
        } else if (BACKSPACE_KEY.equals(key)) {
            target.setText(text.isEmpty() ? "" : text.substring(0, text.length() - 1));
        } else if (target.getSelectedText() != null) {
            target.replaceSelection(key);
        } else {
            target.setText(text + key);
        }
    }
}
