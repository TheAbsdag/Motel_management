package view.helpers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormatHelperTest {

    /**
     * Verifies that {@link FormatHelper#formatPrice(int)} formats a typical
     * positive price value with locale-appropriate thousands separators.
     * Expected: {@code formatPrice(1234)} equals {@code String.format("%,d", 1234)}.
     * Failure: The formatted output is missing thousands separators or differs
     *          from the standard locale-formatting pattern.
     */
    @Test
    void shouldFormatPositivePrice() {
        assertThat(FormatHelper.formatPrice(1234)).isEqualTo(String.format("%,d", 1234));
    }

    /**
     * Verifies that {@link FormatHelper#formatPrice(int)} correctly inserts
     * thousands separators for large price values (e.g., one million).
     * Expected: {@code formatPrice(1_000_000)} equals "1,000,000".
     * Failure: Large values are not segmented correctly, which would impair
     *          readability of high-value transactions in the UI.
     */
    @Test
    void shouldFormatLargePrice() {
        assertThat(FormatHelper.formatPrice(1_000_000)).isEqualTo(String.format("%,d", 1_000_000));
    }

    /**
     * Verifies that {@link FormatHelper#formatPrice(int)} handles a zero price
     * value without throwing an exception or producing unexpected output.
     * Expected: {@code formatPrice(0)} equals {@code String.format("%,d", 0)}.
     * Failure: Zero is formatted with a negative sign, thousands separator, or
     *          causes a division-by-zero / formatting error.
     */
    @Test
    void shouldFormatZeroPrice() {
        assertThat(FormatHelper.formatPrice(0)).isEqualTo(String.format("%,d", 0));
    }

    /**
     * Verifies that {@link FormatHelper#formatPrice(int)} correctly formats
     * negative price values, preserving the minus sign alongside group separators.
     * Expected: {@code formatPrice(-500)} equals {@code String.format("%,d", -500)}.
     * Failure: The negative sign is stripped, prepended incorrectly, or the
     *          absolute value is formatted instead of the signed value.
     */
    @Test
    void shouldFormatNegativePrice() {
        assertThat(FormatHelper.formatPrice(-500)).isEqualTo(String.format("%,d", -500));
    }

    /**
     * Verifies that {@link FormatHelper#formatQuantity(int)} formats a positive
     * quantity with thousands separators, behaving identically to
     * {@link FormatHelper#formatPrice(int)}.
     * Expected: {@code formatQuantity(50)} equals {@code String.format("%,d", 50)}.
     * Failure: The quantity formatting diverges from price formatting, suggesting
     *          inconsistent locale handling or a copy-paste error in the helper.
     */
    @Test
    void shouldFormatPositiveQuantity() {
        assertThat(FormatHelper.formatQuantity(50)).isEqualTo(String.format("%,d", 50));
    }

    /**
     * Verifies that {@link FormatHelper#formatQuantity(int)} handles a zero
     * quantity without errors, matching the zero-price formatting behavior.
     * Expected: {@code formatQuantity(0)} equals {@code String.format("%,d", 0)}.
     * Failure: Zero is formatted with unexpected characters (signs, separators)
     *          or causes a runtime exception.
     */
    @Test
    void shouldFormatZeroQuantity() {
        assertThat(FormatHelper.formatQuantity(0)).isEqualTo(String.format("%,d", 0));
    }
}
