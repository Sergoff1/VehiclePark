package ru.lessons.my.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtils {

    public static LocalDateTime convertToUtc(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(zoneId)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    public static LocalDateTime convertFromUtc(LocalDateTime utcDateTime, ZoneId targetZone) {
        return utcDateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(targetZone)
                .toLocalDateTime();
    }
}
