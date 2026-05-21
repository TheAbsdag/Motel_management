package view.helpers;

import javax.swing.JTextField;

/**
 * Utility for adjusting numeric values in text fields via +/- buttons.
 * Consolidates the repeated parse-adjust-validate-set pattern used across controllers.
 */
public final class PriceAdjustmentHelper {

    private PriceAdjustmentHelper() { }

    /**
     * Reads the current text field value as a long, adds the delta,
     * clamps to minValue, and writes back.
     */
    public static long adjust(JTextField field, long delta, long minValue) {
        long current = InputParser.parseLongSafe(field, 0L);
        long adjusted = current + delta;
        if (adjusted < minValue) {
            adjusted = minValue;
        }
        field.setText(String.valueOf(adjusted));
        return adjusted;
    }

    /**
     * Adjusts with a minimum value of 0.
     */
    public static long adjust(JTextField field, long delta) {
        return adjust(field, delta, 0L);
    }
}
