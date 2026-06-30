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
