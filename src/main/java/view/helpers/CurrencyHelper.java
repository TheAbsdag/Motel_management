package view.helpers;

import javax.swing.JTextField;
import model.json.CurrencyConfig;

/**
 * Shared utilities for currency configuration and price adjustment.
 * Consolidates the {@code setCurrencyConfig} null-safety pattern and
 * provides convenience wrappers for {@link PriceAdjustmentHelper}.
 */
public final class CurrencyHelper {

    private CurrencyHelper() { }

    /**
     * Returns the given config, or a default config if null.
     * Use in setCurrencyConfig implementations to avoid repeating the null check.
     */
    public static CurrencyConfig ensureConfig(CurrencyConfig cfg) {
        return cfg != null ? cfg : CurrencyConfig.defaultConfig();
    }

    /**
     * Adjusts a text field value by delta and returns the adjusted value,
     * using the currency's decimal precision to determine step size.
     */
    public static long adjustPrice(JTextField field, long delta, long minValue) {
        return PriceAdjustmentHelper.adjust(field, delta, minValue);
    }

    /**
     * Adjusts a text field value by delta with a minimum of 0.
     */
    public static long adjustPrice(JTextField field, long delta) {
        return PriceAdjustmentHelper.adjust(field, delta);
    }
}