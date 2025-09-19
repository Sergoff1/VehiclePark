package ru.lessons.my.util;

public class TimeZoneContext {

    private static final ThreadLocal<String> timeZoneHolder = ThreadLocal.withInitial(() -> "UTC");

    public static String get() {
        return timeZoneHolder.get();
    }

    public static void set(String timeZoneName) {
        timeZoneHolder.set(timeZoneName);
    }

    public static void reset() {
        timeZoneHolder.remove();
    }
}
