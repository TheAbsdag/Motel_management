package view.helpers;

import model.json.CurrencyConfig;

public final class CurrencyFormatter {

    private CurrencyFormatter() { }

    public static String format(long valueInSmallestUnit, CurrencyConfig cfg) {
        if (cfg == null) cfg = CurrencyConfig.defaultConfig();
        int dp = cfg.decimalPlaces();
        long absValue = Math.abs(valueInSmallestUnit);
        long wholePart = absValue / (long) Math.pow(10, dp);
        long fracPart = absValue % (long) Math.pow(10, dp);

        String sign = valueInSmallestUnit < 0 ? "-" : "";
        String whole = String.format("%,d", wholePart);
        String frac = dp > 0 ? String.format(".%0" + dp + "d", fracPart) : "";

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
        try {
            double parsed = Double.parseDouble(cleaned);
            long factor = (long) Math.pow(10, cfg.decimalPlaces());
            return Math.round(parsed * factor);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
