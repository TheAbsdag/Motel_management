package model;

/**
 * Stock thresholds shared by the views and the printed reports.
 *
 * <p>A product is <b>out</b> when its quantity is zero or negative, and <b>low</b>
 * when it still has units but they are at or below its configured minimum.
 */
public final class StockRules {

    private StockRules() {
    }

    /**
     * @param quantity current stock
     * @return true when there are no units left (negative counts as out)
     */
    public static boolean isOut(long quantity) {
        return quantity <= 0;
    }

    /**
     * @param quantity current stock
     * @param minimum  per-product minimum, zero when none is configured
     * @return true when the product has units but is at or below its minimum
     */
    public static boolean isLow(long quantity, long minimum) {
        return quantity > 0 && minimum > 0 && quantity <= minimum;
    }
}
