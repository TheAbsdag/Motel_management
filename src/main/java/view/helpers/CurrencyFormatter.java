package view.helpers;

import java.util.Locale;
import model.json.CurrencyConfig;

public final class CurrencyFormatter {

    /**
     * Locale used for grouping separators and digits. It must never be left implicit:
     * {@code String.format} without a locale uses
     * {@code Locale.getDefault(Locale.Category.FORMAT)}, which on Windows follows the
     * regional settings. On a machine set to Colombia that formats {@code 40000} as
     * {@code "40.000"}, while the decimal separator below is hardcoded to {@code "."},
     * producing the ambiguous {@code "$ 123.456.789.01"}. Swap this for a configurable
     * locale when internationalization lands.
     */
    private static final Locale MONEY_LOCALE = Locale.US;

    private CurrencyFormatter() { }

    public static String format(long valueInSmallestUnit, CurrencyConfig cfg) {
        if (cfg == null) cfg = CurrencyConfig.defaultConfig();
        int dp = cfg.decimalPlaces();
        long absValue = Math.abs(valueInSmallestUnit);
        long wholePart = absValue / (long) Math.pow(10, dp);
        long fracPart = absValue % (long) Math.pow(10, dp);

        String sign = valueInSmallestUnit < 0 ? "-" : "";
        String whole = String.format(MONEY_LOCALE, "%,d", wholePart);
        String frac = dp > 0 ? String.format(MONEY_LOCALE, ".%0" + dp + "d", fracPart) : "";

        String body = sign + whole + frac;
        if (cfg.symbolBefore()) {
            body = cfg.symbol() + " " + body;
        } else {
            body = body + " " + cfg.symbol();
        }
        return body.trim();
    }

    public static long parse(String input, CurrencyConfig cfg) {
        if (cfg == null) cfg = CurrencyConfig.defaultConfig();
        if (input == null || input.trim().isEmpty()) return 0L;

        String cleaned = input.replace(cfg.symbol(), "")
                .replace(",", ".")
                .replaceAll("[^0-9.\\-]", "")
                .trim();

        if (cleaned.isEmpty()) return 0L;
        boolean negative = cleaned.startsWith("-");
        String numeric = negative ? cleaned.substring(1) : cleaned;

        String[] parts = numeric.split("\\.", 2);
        try {
            long wholePart = parts[0].isEmpty() ? 0L : Long.parseLong(parts[0]);
            int dp = cfg.decimalPlaces();
            long factor = (long) Math.pow(10, dp);
            long fracPart = 0L;
            if (parts.length > 1 && dp > 0) {
                String frac = parts[1];
                if (frac.length() > dp) frac = frac.substring(0, dp);
                fracPart = Long.parseLong(frac);
                for (int i = frac.length(); i < dp; i++) fracPart *= 10;
            }
            long result = wholePart * factor + fracPart;
            return negative ? -result : result;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
