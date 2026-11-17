package model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StockRulesTest {

    @Test
    void shouldTreatZeroAndNegativeAsOut() {
        assertThat(StockRules.isOut(0)).isTrue();
        assertThat(StockRules.isOut(-3)).isTrue();
        assertThat(StockRules.isOut(1)).isFalse();
    }

    @Test
    void shouldTreatPositiveAtOrBelowMinimumAsLow() {
        assertThat(StockRules.isLow(5, 5)).isTrue();
        assertThat(StockRules.isLow(3, 5)).isTrue();
        assertThat(StockRules.isLow(6, 5)).isFalse();
    }

    @Test
    void shouldNotMarkLowWhenNoMinimumIsConfigured() {
        assertThat(StockRules.isLow(1, 0)).isFalse();
        assertThat(StockRules.isLow(0, 0)).isFalse();
    }
}
