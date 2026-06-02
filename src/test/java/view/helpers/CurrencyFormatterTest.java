package view.helpers;

import model.json.CurrencyConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrencyFormatterTest {

    private final CurrencyConfig cop = new CurrencyConfig("COP", 0, "$", true);
    private final CurrencyConfig usd = new CurrencyConfig("USD", 2, "$", true);
    private final CurrencyConfig eur = new CurrencyConfig("EUR", 2, "\u20AC", false);
    private final CurrencyConfig jpy = new CurrencyConfig("JPY", 0, "\u00A5", true);

    @Test
    void formatCOP() {
        Assertions.assertThat(CurrencyFormatter.format(40000L, cop)).isEqualTo("$ 40,000");
    }

    @Test
    void formatUSD() {
        Assertions.assertThat(CurrencyFormatter.format(1250L, usd)).isEqualTo("$ 12.50");
    }

    @Test
    void formatUSDCentsOnly() {
        Assertions.assertThat(CurrencyFormatter.format(5L, usd)).isEqualTo("$ 0.05");
    }

    @Test
    void formatEURSymbolAfter() {
        Assertions.assertThat(CurrencyFormatter.format(5000L, eur)).isEqualTo("50.00 \u20AC");
    }

    @Test
    void formatJPY() {
        Assertions.assertThat(CurrencyFormatter.format(15000L, jpy)).isEqualTo("\u00A5 15,000");
    }

    @Test
    void formatNegative() {
        Assertions.assertThat(CurrencyFormatter.format(-1000L, usd)).isEqualTo("$ -10.00");
    }

    @Test
    void formatZero() {
        Assertions.assertThat(CurrencyFormatter.format(0L, usd)).isEqualTo("$ 0.00");
    }

    @Test
    void parseCOPInput() {
        Assertions.assertThat(CurrencyFormatter.parse("40000", cop)).isEqualTo(40000L);
    }

    @Test
    void parseUSDInput() {
        Assertions.assertThat(CurrencyFormatter.parse("12.50", usd)).isEqualTo(1250L);
    }

    @Test
    void parseUSDWithSymbol() {
        Assertions.assertThat(CurrencyFormatter.parse("$12.50", usd)).isEqualTo(1250L);
    }

    @Test
    void parseCommaDecimal() {
        Assertions.assertThat(CurrencyFormatter.parse("12,50", usd)).isEqualTo(1250L);
    }

    @Test
    void parseEmpty() {
        Assertions.assertThat(CurrencyFormatter.parse("", cop)).isEqualTo(0L);
    }

    @Test
    void parseNull() {
        Assertions.assertThat(CurrencyFormatter.parse(null, cop)).isEqualTo(0L);
    }

    @Test
    void formatNullConfigUsesDefault() {
        Assertions.assertThat(CurrencyFormatter.format(40000L, null)).isEqualTo("$ 40,000");
    }

    @Test
    void parseNullConfigUsesDefault() {
        Assertions.assertThat(CurrencyFormatter.parse("40000", null)).isEqualTo(40000L);
    }

    @Test
    void largeValueUSD() {
        Assertions.assertThat(CurrencyFormatter.format(12345678901L, usd)).isEqualTo("$ 123,456,789.01");
    }
}
