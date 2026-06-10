package view.helpers;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Map;

public final class TimeFormatter {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("hh:mm:ss").appendLiteral(' ')
            .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "AM", 1L, "PM"))
            .toFormatter()
            .withZone(BOGOTA);

    private static final Locale LOCALE_ES = new Locale("es", "ES");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("d 'de' MMMM 'de' yyyy", LOCALE_ES)
            .withZone(BOGOTA);

    private static final DateTimeFormatter EMAIL_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("hh:mm:ss a")
            .appendLiteral(' ')
            .appendZoneText(TextStyle.SHORT)
            .toFormatter(Locale.ENGLISH)
            .withZone(BOGOTA);

    private TimeFormatter() { }

    public static String formatTime(ZonedDateTime time) {
        return time.format(TIME_FORMATTER);
    }

    public static String formatDate(ZonedDateTime time) {
        return time.format(DATE_FORMATTER);
    }

    /**
     * Formats a timestamp for email placeholders: "12:24:36 PM Bogotá Colombia"
     * Extracts the city name from the zone ID and appends the country.
     */
    public static String formatEmailDatetime(ZonedDateTime time) {
        String timePart = time.format(EMAIL_DATETIME_FORMATTER);
        String zoneId = time.getZone().getId();
        return timePart + " " + friendlyZoneName(zoneId);
    }

    private static String friendlyZoneName(String zoneId) {
        if (zoneId == null || !zoneId.contains("/")) return zoneId;
        String[] parts = zoneId.split("/", 2);
        String city = parts[1].replace("_", " ");
        if ("Bogota".equals(city)) city = "Bogotá";
        return city + " Colombia";
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
