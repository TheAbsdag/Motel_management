package view.helpers;

import javax.swing.JTextField;
import model.json.CurrencyConfig;

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

    /**
     * Parses a raw string as a long, returning {@code defaultValue}
     * for null, empty, or non-numeric input instead of throwing.
     */
    public static long parseLongSafe(String text, long defaultValue) {
        if (text == null || text.trim().isEmpty()) return defaultValue;
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses a raw string as a long, returning 0 as default.
     */
    public static long parseLongSafe(String text) {
        return parseLongSafe(text, 0L);
    }

    /**
     * Parses the text of a JTextField as a price value, converting from display
     * format to smallest unit using the provided CurrencyConfig.
     */
    public static long parsePriceSafe(JTextField field, CurrencyConfig cfg) {
        if (field == null) return 0L;
        return CurrencyFormatter.parse(field.getText(), cfg);
    }

    /**
     * Parses the text of a JTextField as a price value, using the default COP config.
     */
    public static long parsePriceSafe(JTextField field) {
        return parsePriceSafe(field, CurrencyConfig.defaultConfig());
    }
}
