package view.helpers;

/**
 * Shared formatting utilities for currency and quantity display.
 */
public final class FormatHelper {

    private FormatHelper() { }

    public static String formatPrice(long price) {
        return String.format("%,d", price);
    }

    public static String formatQuantity(long quantity) {
        return String.format("%,d", quantity);
    }
}
