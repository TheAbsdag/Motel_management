package view.helpers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputParserTest {

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} returns the
     * parsed long value when given a valid positive integer string.
     * Expected: {@code parseLongSafe("42", -1L)} returns 42L.
     * Failure: The parser incorrectly handles valid numeric strings or the
     *          default value is returned when parsing should succeed.
     */
    @Test
    void shouldParseValidPositiveLong() {
        assertThat(InputParser.parseLongSafe("42", -1L)).isEqualTo(42L);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} correctly
     * parses the string {@code "0"} as the long value 0.
     * Expected: {@code parseLongSafe("0", -1L)} returns 0L.
     * Failure: The parser treats zero as an invalid value, suggesting a
     *          regression in numeric string validation logic.
     */
    @Test
    void shouldParseZero() {
        assertThat(InputParser.parseLongSafe("0", -1L)).isEqualTo(0L);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} correctly
     * parses a negative integer string (e.g., {@code "-100"}) as a negative long.
     * Expected: {@code parseLongSafe("-100", 0L)} returns -100L.
     * Failure: The parser strips the minus sign or treats negative inputs as
     *          invalid, which would silently corrupt financial calculations.
     */
    @Test
    void shouldParseNegativeLong() {
        assertThat(InputParser.parseLongSafe("-100", 0L)).isEqualTo(-100L);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} returns the
     * provided default value when the input is an empty string.
     * Expected: {@code parseLongSafe("", 99L)} returns 99L.
     * Failure: An empty string causes an exception or returns a value other than
     *          the supplied default, which would break input form resilience.
     */
    @Test
    void shouldReturnDefaultForEmptyString() {
        assertThat(InputParser.parseLongSafe("", 99L)).isEqualTo(99L);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} returns the
     * default value when the input consists solely of whitespace characters.
     * Expected: {@code parseLongSafe("   ", 99L)} returns 99L.
     * Failure: Whitespace is not trimmed before parsing, causing spurious
     *          {@code NumberFormatException} or incorrect parsing results.
     */
    @Test
    void shouldReturnDefaultForWhitespaceOnly() {
        assertThat(InputParser.parseLongSafe("   ", 99L)).isEqualTo(99L);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} returns the
     * provided default value when the input string is {@code null}, protecting
     * against {@code NullPointerException}.
     * Expected: {@code parseLongSafe((String) null, 77L)} returns 77L.
     * Failure: A {@code NullPointerException} is thrown, or a value other than
     *          the default is returned, indicating missing null-safety handling.
     */
    @Test
    void shouldReturnDefaultForNull() {
        assertThat(InputParser.parseLongSafe((String) null, 77L)).isEqualTo(77L);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} returns the
     * default value when the input is entirely non-numeric (e.g., {@code "abc"}).
     * Expected: {@code parseLongSafe("abc", 55L)} returns 55L.
     * Failure: Non-numeric strings cause an exception instead of gracefully
     *          falling back to the default, breaking user-input validation.
     */
    @Test
    void shouldReturnDefaultForNonNumeric() {
        assertThat(InputParser.parseLongSafe("abc", 55L)).isEqualTo(55L);
    }

    /**
     * Verifies that the zero-argument overload
     * {@link InputParser#parseLongSafe(String)} returns {@code 0L} when the input
     * is {@code null}, confirming the implicit default value is zero.
     * Expected: {@code parseLongSafe((String) null)} returns 0L.
     * Failure: The implicit default is not zero, or the overload does not behave
     *          consistently with the two-argument variant.
     */
    @Test
    void shouldReturnZeroByDefaultForNull() {
        assertThat(InputParser.parseLongSafe((String) null)).isEqualTo(0L);
    }

    /**
     * Verifies that the zero-argument overload
     * {@link InputParser#parseLongSafe(String)} returns {@code 0L} when the
     * input is an empty string, confirming the implicit default is zero.
     * Expected: {@code parseLongSafe("")} returns 0L.
     * Failure: The overload does not delegate correctly to the two-argument
     *          variant, or the implicit default is not zero.
     */
    @Test
    void shouldReturnZeroByDefaultForEmpty() {
        assertThat(InputParser.parseLongSafe("")).isEqualTo(0L);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} trims
     * leading and trailing whitespace before parsing the inner number.
     * Expected: {@code parseLongSafe("  123  ", -1L)} returns 123L.
     * Failure: Surrounding whitespace causes a parse failure, meaning the
     *          trim operation is missing or applied incorrectly.
     */
    @Test
    void shouldHandleWhitespaceAroundNumber() {
        assertThat(InputParser.parseLongSafe("  123  ", -1L)).isEqualTo(123L);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} can parse
     * the string representation of {@link Long#MAX_VALUE} without overflow.
     * Expected: Parsed value equals {@code Long.MAX_VALUE}.
     * Failure: The maximum long value cannot be parsed, suggesting an internal
     *          overflow bug or an overly restrictive validation guard.
     */
    @Test
    void shouldHandleLongMaxValue() {
        String maxLong = String.valueOf(Long.MAX_VALUE);
        assertThat(InputParser.parseLongSafe(maxLong, -1L)).isEqualTo(Long.MAX_VALUE);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} can parse
     * the string representation of {@link Long#MIN_VALUE} without underflow.
     * Expected: Parsed value equals {@code Long.MIN_VALUE}.
     * Failure: The minimum long value cannot be parsed, indicating the parser
     *          mishandles extreme negative values or the minus sign.
     */
    @Test
    void shouldHandleLongMinValue() {
        String minLong = String.valueOf(Long.MIN_VALUE);
        assertThat(InputParser.parseLongSafe(minLong, 0L)).isEqualTo(Long.MIN_VALUE);
    }

    /**
     * Verifies that {@link InputParser#parseLongSafe(String, long)} returns the
     * default value when the numeric string exceeds {@link Long#MAX_VALUE}.
     * Expected: {@code parseLongSafe(Long.MAX_VALUE + "0", 99L)} returns 99L.
     * Failure: An overflow value silently wraps around to a garbage number,
     *          which would corrupt application data.
     */
    @Test
    void shouldReturnDefaultForOverflowValue() {
        String overflow = Long.MAX_VALUE + "0";
        assertThat(InputParser.parseLongSafe(overflow, 99L)).isEqualTo(99L);
    }
}
