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
}
