package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CurrencyConfig(
        @JsonProperty("currencyCode") String currencyCode,
        @JsonProperty("decimalPlaces") int decimalPlaces,
        @JsonProperty("symbol") String symbol,
        @JsonProperty("symbolBefore") boolean symbolBefore
) {
    public CurrencyConfig {
        if (currencyCode == null || currencyCode.isBlank()) currencyCode = "COP";
        if (symbol == null || symbol.isBlank()) symbol = "$";
        if (decimalPlaces < 0) decimalPlaces = 0;
    }

    public CurrencyConfig() {
        this("COP", 0, "$", true);
    }

    public static CurrencyConfig defaultConfig() {
        return new CurrencyConfig("COP", 0, "$", true);
    }
}
