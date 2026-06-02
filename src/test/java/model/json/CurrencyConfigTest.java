package model.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrencyConfigTest {

    @Test
    void defaultConfigIsCOP() {
        CurrencyConfig cfg = CurrencyConfig.defaultConfig();
        Assertions.assertThat(cfg.currencyCode()).isEqualTo("COP");
        Assertions.assertThat(cfg.decimalPlaces()).isEqualTo(0);
        Assertions.assertThat(cfg.symbol()).isEqualTo("$");
        Assertions.assertThat(cfg.symbolBefore()).isTrue();
    }

    @Test
    void compactConstructorDefaults() {
        CurrencyConfig cfg = new CurrencyConfig(null, -1, null, false);
        Assertions.assertThat(cfg.currencyCode()).isEqualTo("COP");
        Assertions.assertThat(cfg.decimalPlaces()).isEqualTo(0);
        Assertions.assertThat(cfg.symbol()).isEqualTo("$");
        Assertions.assertThat(cfg.symbolBefore()).isFalse();
    }

    @Test
    void jsonRoundtripUSD() throws JsonProcessingException {
        CurrencyConfig cfg = new CurrencyConfig("USD", 2, "$", true);
        String json = ObjectMapperFactory.get().writeValueAsString(cfg);
        CurrencyConfig parsed = ObjectMapperFactory.get().readValue(json, CurrencyConfig.class);
        Assertions.assertThat(parsed).isEqualTo(cfg);
    }

    @Test
    void jsonRoundtripEUR() throws JsonProcessingException {
        CurrencyConfig cfg = new CurrencyConfig("EUR", 2, "\u20AC", false);
        String json = ObjectMapperFactory.get().writeValueAsString(cfg);
        CurrencyConfig parsed = ObjectMapperFactory.get().readValue(json, CurrencyConfig.class);
        Assertions.assertThat(parsed).isEqualTo(cfg);
    }

    @Test
    void noArgConstructorUsesDefaults() {
        CurrencyConfig cfg = new CurrencyConfig();
        Assertions.assertThat(cfg.currencyCode()).isEqualTo("COP");
        Assertions.assertThat(cfg.decimalPlaces()).isEqualTo(0);
        Assertions.assertThat(cfg.symbol()).isEqualTo("$");
        Assertions.assertThat(cfg.symbolBefore()).isTrue();
    }
}
