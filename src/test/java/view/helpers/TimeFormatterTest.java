package view.helpers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimeFormatterTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    /**
     * Verifies that {@link TimeFormatter#formatTime(ZonedDateTime)} formats an
     * afternoon time (2:30:45 PM) using 12-hour clock with the correct AM/PM
     * suffix and zero-padded hour/minute/second components.
     * Expected: The result contains {@code "02:30:45"} and {@code "PM"}.
     * Failure: The hour is incorrectly converted to 12-hour format, the AM/PM
     *          indicator is wrong, or the components lack zero-padding.
     */
    @Test
    void shouldFormatTimeWithAmPm() {
        ZonedDateTime time = ZonedDateTime.of(LocalDate.of(2026, 5, 23), LocalTime.of(14, 30, 45), BOGOTA);
        String result = TimeFormatter.formatTime(time);
        assertThat(result).contains("02:30:45").contains("PM");
    }

    /**
     * Verifies that {@link TimeFormatter#formatTime(ZonedDateTime)} formats
     * midnight (00:00:00) as {@code "12:00:00 AM"} rather than {@code "00:00:00"}.
     * Expected: The result contains {@code "12:00:00"} and {@code "AM"}.
     * Failure: Midnight is displayed as 00:xx, or the AM/PM suffix is missing or
     *          wrong, which is a common 12-hour-clock edge-case bug.
     */
    @Test
    void shouldFormatMidnightAsAm() {
        ZonedDateTime time = ZonedDateTime.of(LocalDate.of(2026, 5, 23), LocalTime.of(0, 0, 0), BOGOTA);
        String result = TimeFormatter.formatTime(time);
        assertThat(result).contains("12:00:00").contains("AM");
    }

    /**
     * Verifies that {@link TimeFormatter#formatTime(ZonedDateTime)} formats
     * noon (12:00:00) as {@code "12:00:00 PM"} rather than {@code "12:00:00 AM"}.
     * Expected: The result contains {@code "12:00:00"} and {@code "PM"}.
     * Failure: Noon is mislabeled as AM, which is a classic 12-hour-clock bug
     *          caused by improper boundary handling at hour 12.
     */
    @Test
    void shouldFormatNoonAsPm() {
        ZonedDateTime time = ZonedDateTime.of(LocalDate.of(2026, 5, 23), LocalTime.of(12, 0, 0), BOGOTA);
        String result = TimeFormatter.formatTime(time);
        assertThat(result).contains("12:00:00").contains("PM");
    }

    /**
     * Verifies that {@link TimeFormatter#formatDate(ZonedDateTime)} produces a
     * Spanish-locale date string containing the day number, the month name in
     * Spanish, and the full year.
     * Expected: For May 23, 2026, the result contains {@code "23"}, {@code "mayo"},
     *           and {@code "2026"}.
     * Failure: The month name is in English or another language, the day/month
     *          order is wrong, or locale fallback is not working correctly.
     */
    @Test
    void shouldFormatDateInSpanish() {
        ZonedDateTime time = ZonedDateTime.of(LocalDate.of(2026, 5, 23), LocalTime.NOON, BOGOTA);
        String result = TimeFormatter.formatDate(time);
        assertThat(result).contains("23").contains("mayo").contains("2026");
    }

    /**
     * Verifies that {@link TimeFormatter#formatDuration(int)} formats durations
     * that are exact multiples of one hour as whole-hour strings without minute
     * or second components.
     * Expected: 3600s → {@code "1h"}, 7200s → {@code "2h"}, 10800s → {@code "3h"}.
     * Failure: Whole hours include spurious minute/second suffixes (e.g.,
     *          {@code "1h 0min"}) or are off by one hour.
     */
    @Test
    void shouldFormatDurationWholeHours() {
        assertThat(TimeFormatter.formatDuration(3600)).isEqualTo("1h");
        assertThat(TimeFormatter.formatDuration(7200)).isEqualTo("2h");
        assertThat(TimeFormatter.formatDuration(10800)).isEqualTo("3h");
    }

    /**
     * Verifies that {@link TimeFormatter#formatDuration(int)} formats durations
     * with both hours and minutes, rounding seconds down.
     * Expected: 3660s → {@code "1h 1min"}, 3750s → {@code "1h 2min"}.
     * Failure: Minutes are not displayed, are off by one, or the rounding of
     *          remaining seconds is incorrect.
     */
    @Test
    void shouldFormatDurationHoursAndMinutes() {
        assertThat(TimeFormatter.formatDuration(3660)).isEqualTo("1h 1min");
        assertThat(TimeFormatter.formatDuration(3750)).isEqualTo("1h 2min");
    }

    /**
     * Verifies that {@link TimeFormatter#formatDuration(int)} truncates the
     * minutes component when remaining seconds after hour division are less
     * than one minute.
     * Expected: 3601s → {@code "1h"} (not {@code "1h 0min"}).
     * Failure: The output includes a zero-minute component, polluting the
     *          display with unnecessary detail.
     */
    @Test
    void shouldFormatDurationHoursOnlyWhenNoMinutes() {
        assertThat(TimeFormatter.formatDuration(3601)).isEqualTo("1h");
    }

    /**
     * Verifies that {@link TimeFormatter#formatDuration(int)} formats durations
     * under one hour using only the minutes unit, without an hour component.
     * Expected: 60s → {@code "1min"}, 120s → {@code "2min"}, 3599s → {@code "59min"}.
     * Failure: Sub-hour durations show a zero-hour prefix, or the minute
     *          calculation is wrong.
     */
    @Test
    void shouldFormatDurationMinutesOnly() {
        assertThat(TimeFormatter.formatDuration(60)).isEqualTo("1min");
        assertThat(TimeFormatter.formatDuration(120)).isEqualTo("2min");
        assertThat(TimeFormatter.formatDuration(3599)).isEqualTo("59min");
    }

    /**
     * Verifies that {@link TimeFormatter#formatDuration(int)} formats durations
     * under one minute using only the seconds unit.
     * Expected: 0s → {@code "0s"}, 30s → {@code "30s"}, 59s → {@code "59s"}.
     * Failure: Sub-minute durations are misrepresented (e.g., zero shown as
     *          empty string) or incorrectly converted to a larger unit.
     */
    @Test
    void shouldFormatDurationSecondsOnly() {
        assertThat(TimeFormatter.formatDuration(0)).isEqualTo("0s");
        assertThat(TimeFormatter.formatDuration(30)).isEqualTo("30s");
        assertThat(TimeFormatter.formatDuration(59)).isEqualTo("59s");
    }

    @Test
    void shouldFormatDurationAsHHMMSS() {
        assertThat(TimeFormatter.formatDurationAsHHMMSS(0)).isEqualTo("00:00:00");
        assertThat(TimeFormatter.formatDurationAsHHMMSS(30)).isEqualTo("00:00:30");
        assertThat(TimeFormatter.formatDurationAsHHMMSS(3661)).isEqualTo("01:01:01");
        assertThat(TimeFormatter.formatDurationAsHHMMSS(7322)).isEqualTo("02:02:02");
        assertThat(TimeFormatter.formatDurationAsHHMMSS(86399)).isEqualTo("23:59:59");
    }
}
