package view.helpers;

import javax.swing.JTextField;

/**
 * Safe numeric parsing utilities for view input fields.
 */
public final class InputParser {

    private InputParser() { }

    /**
     * Parses the text of a JTextField as a long, returning {@code defaultValue}
     * for null, empty, or non-numeric input instead of throwing.
     */
    public static long parseLongSafe(JTextField field, long defaultValue) {
        if (field == null) return defaultValue;
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) return defaultValue;
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses the text of a JTextField as a long, returning 0 as default.
     */
    public static long parseLongSafe(JTextField field) {
        return parseLongSafe(field, 0L);
    }
}
