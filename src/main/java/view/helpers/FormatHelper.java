package view.helpers;

import model.json.CurrencyConfig;

/**
 * Shared formatting utilities for currency and quantity display.
 */
public final class FormatHelper {

    private FormatHelper() { }

    public static String formatPrice(long price) {
        return String.format("%,d", price);
    }

    public static String formatPrice(long price, CurrencyConfig cfg) {
        return CurrencyFormatter.format(price, cfg);
    }

    public static String formatQuantity(long quantity) {
        return String.format("%,d", quantity);
    }
}
