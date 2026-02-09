package util;

import java.util.Date;

/**
 * Formats a date as a relative "time ago" string (e.g. "5m ago", "2h ago", "3 days ago").
 */
public class TimeAgoUtil {

    public static String format(Date date) {
        if (date == null) return "";
        long diff = System.currentTimeMillis() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        if (seconds >= 5) return seconds + "s ago";
        return "just now";
    }
}
