package view.helpers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Map;

public final class TimeFormatter {

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("hh:mm:ss").appendLiteral(' ')
            .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "AM", 1L, "PM"))
            .toFormatter();

    private static final Locale LOCALE_ES = new Locale("es", "ES");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("d 'de' MMMM 'de' yyyy", LOCALE_ES);

    private TimeFormatter() { }

    public static String formatTime(ZonedDateTime time) {
        return time.format(TIME_FORMATTER);
    }

    public static String formatDate(ZonedDateTime time) {
        return time.format(DATE_FORMATTER);
    }

    /**
     * Formats a duration in seconds as a human-readable string, rounding to minutes.
     * <ul>
     *   <li>Whole hours: {@code "3h"}</li>
     *   <li>Hours + minutes: {@code "3h 5min"}</li>
     *   <li>Minutes only: {@code "45min"}</li>
     *   <li>Seconds only: {@code "45s"} (rare)</li>
     * </ul>
     * @param seconds duration in seconds
     * @return human-readable duration string
     */
    public static String formatDuration(long seconds) {
        if (seconds >= 3600 && seconds % 3600 == 0) {
            return (seconds / 3600) + "h";
        }
        if (seconds >= 3600) {
            long hours = seconds / 3600;
            long mins = (seconds % 3600) / 60;
            return mins > 0 ? hours + "h " + mins + "min" : hours + "h";
        }
        if (seconds >= 60) {
            return (seconds / 60) + "min";
        }
        return seconds + "s";
    }
}
