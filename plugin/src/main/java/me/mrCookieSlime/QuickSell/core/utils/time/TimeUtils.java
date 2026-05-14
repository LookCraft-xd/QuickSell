package me.mrCookieSlime.QuickSell.core.utils.time;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtils {

    private static final Map<String, Long> TIME_UNITS = new HashMap<>();
    private static final Pattern parseTimePattern = Pattern.compile("(\\d+)([smhdwMy])");

    private static final String HOUR_FORMAT = "%02d:%02d:%02d";
    private static final String MINUTE_FORMAT = "%02d:%02d";

    private static String yearShortString = "y";
    private static String yearString = "year";
    private static String yearsString = "years";
    private static String monthShortString = "M";
    private static String monthString = "month";
    private static String monthsString = "months";
    private static String weekShortString = "w";
    private static String weekString = "week";
    private static String weeksString = "weeks";
    private static String dayShortString = "d";
    private static String dayString = "day";
    private static String daysString = "days";
    private static String hourShortString = "h";
    private static String hourString = "hour";
    private static String hoursString = "hours";
    private static String minuteShortString = "m";
    private static String minuteString = "minute";
    private static String minutesString = "minutes";
    private static String secondShortString = "s";
    private static String secondString = "second";
    private static String secondsString = "seconds";
    private static String now = "now";


    static {
        TIME_UNITS.put("s", 1L);
        TIME_UNITS.put("m", 60L);
        TIME_UNITS.put("h", 60L * 60);
        TIME_UNITS.put("d", 60L * 60 * 24);
        TIME_UNITS.put("w", 60L * 60 * 24 * 7);
        TIME_UNITS.put("M", 60L * 60 * 24 * 30);
        TIME_UNITS.put("y", 60L * 60 * 24 * 365);
    }

    private TimeUtils() {
        throw new RuntimeException("Cannot instantiate a utility class.");
    }

    public static void setTimeLang(YamlDocument config) {
        Section shortSection = config.getSection("TIME.SHORT");
        TimeUtils.secondShortString = shortSection.getString("SECOND");
        TimeUtils.minuteShortString = shortSection.getString("MINUTE");
        TimeUtils.hourShortString = shortSection.getString("HOUR");
        TimeUtils.dayShortString = shortSection.getString("DAY");
        TimeUtils.weekShortString = shortSection.getString("WEEK");
        TimeUtils.monthShortString = shortSection.getString("MONTH");
        TimeUtils.yearShortString = shortSection.getString("YEAR");

        Section longSection = config.getSection("TIME.LONG");

        TimeUtils.now = longSection.getString("NOW");
        TimeUtils.secondString = longSection.getString("SECOND");
        TimeUtils.secondsString = longSection.getString("SECONDS");
        TimeUtils.minuteString = longSection.getString("MINUTE");
        TimeUtils.minutesString = longSection.getString("MINUTES");
        TimeUtils.hourString = longSection.getString("HOUR");
        TimeUtils.hoursString = longSection.getString("HOURS");
        TimeUtils.dayString = longSection.getString("DAY");
        TimeUtils.daysString = longSection.getString("DAYS");
        TimeUtils.weekString = longSection.getString("WEEK");
        TimeUtils.weeksString = longSection.getString("WEEKS");
        TimeUtils.monthString = longSection.getString("MONTH");
        TimeUtils.monthsString = longSection.getString("MONTHS");
        TimeUtils.yearString = longSection.getString("YEAR");
        TimeUtils.yearsString = longSection.getString("YEARS");
    }

    /**
     * Converts milliseconds to a timer format (HH:mm:ss or mm:ss).
     *
     * @param millis the time in milliseconds.
     * @return the formatted timer string.
     */
    public static String millisToTimer(long millis) {
        long seconds = millis / 1000L;

        if (seconds > 3600L) {
            return String.format(HOUR_FORMAT, seconds / 3600L, seconds % 3600L / 60L, seconds % 60L);
        }

        return String.format(MINUTE_FORMAT, seconds / 60L, seconds % 60L);
    }

    /**
     * Converts milliseconds to plain seconds.
     *
     * @param millis the time in milliseconds.
     * @return the time in seconds as a string.
     */
    public static String millisToPlainSeconds(long millis) {
        return String.valueOf(millis / 1000L);
    }

    /**
     * Converts milliseconds to a string representation of seconds with one decimal place.
     *
     * @param millis the time in milliseconds.
     * @return the time in seconds as a string with one decimal place.
     */
    public static String millisToSeconds(long millis) {
        return new DecimalFormat("#0.0").format(millis / 1000.0F);
    }

    /**
     * Converts milliseconds to a rounded human-readable time format (e.g., "2 days").
     *
     * @param millis the time in milliseconds.
     * @return the formatted time string.
     */
    public static String millisToRoundedTime(long millis) {
        if (millis <= 0) return now;

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;

        StringJoiner timeString = new StringJoiner(" ");

        if (years > 0) {
            timeString.add(Long.toString(seconds)).add(years == 1 ? yearString : yearsString);
        } else if (months > 0) {
            timeString.add(Long.toString(months)).add(months == 1 ? monthString : monthsString);
        } else if (weeks > 0) {
            timeString.add(Long.toString(weeks)).add(weeks == 1 ? weekString : weeksString);
        } else if (days > 0) {
            timeString.add(Long.toString(days)).add(days == 1 ? dayString : daysString);
        } else if (hours > 0) {
            timeString.add(Long.toString(hours)).add(hours == 1 ? hourString : hoursString);
        } else if (minutes > 0) {
            timeString.add(Long.toString(minutes)).add(minutes == 1 ? minuteString : minutesString);
        } else {
            timeString.add(Long.toString(seconds)).add(seconds == 1 ? secondString : secondsString);
        }

        return timeString.toString();
    }

    public static String millisToRoundedTimeShort(long millis) {
        if (millis <= 0) return "0s";

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = days / 30L;
        long years = days / 365L;

        if (years > 0) {
            return years + yearShortString;
        } else if (months > 0) {
            return months + monthShortString;
        } else if (weeks > 0) {
            return weeks + weekShortString;
        } else if (days > 0) {
            return days + dayShortString;
        } else if (hours > 0) {
            return hours + hourShortString;
        } else if (minutes > 0) {
            return minutes + minuteShortString;
        } else {
            return seconds + secondShortString;
        }
    }


    /**
     * Converts milliseconds to a human-readable format (e.g., "2 days, 3 hours, 15 minutes").
     *
     * @param millis the time in milliseconds.
     * @return the human-readable time string.
     */
    public static String millisToHumanReadable(long millis) {
        if (millis <= 0) return now;

        long seconds = millis / 1000L;
        StringJoiner timeString = new StringJoiner(" ");

        long secondsInAMinute = 60;
        long secondsInAnHour = 60 * 60;
        long secondsInADay = 60 * 60 * 24;
        long secondsInAWeek = 60 * 60 * 24 * 7;
        long secondsInAMonth = 60 * 60 * 24 * 30;
        long secondsInAYear = 60 * 60 * 24 * 365;

        if (seconds < secondsInAMinute) {
            timeString.add(Long.toString(seconds)).add(seconds == 1 ? secondString : secondsString);
        } else if (seconds < secondsInAnHour) {
            long minutes = seconds / secondsInAMinute;
            timeString.add(Long.toString(minutes)).add(minutes == 1 ? minuteString : minutesString);
        } else if (seconds < secondsInADay) {
            long hours = seconds / secondsInAnHour;
            timeString.add(Long.toString(hours)).add(hours == 1 ? hourString : hoursString);
        } else if (seconds < secondsInAWeek) {
            long days = seconds / secondsInADay;
            timeString.add(Long.toString(days)).add(days == 1 ? dayString : daysString);
        } else if (seconds < secondsInAMonth) {
            long weeks = seconds / secondsInAWeek;
            timeString.add(Long.toString(weeks)).add(weeks == 1 ? weekString : weeksString);
        } else if (seconds < secondsInAYear) {
            long months = seconds / secondsInAMonth;
            timeString.add(Long.toString(months)).add(months == 1 ? monthString : monthsString);
        } else {
            long years = seconds / secondsInAYear;
            timeString.add(Long.toString(years)).add(years == 1 ? yearString : yearsString);
        }

        return timeString.toString();
    }

    public static String millisToHumanReadableShort(long millis) {
        if (millis <= 0) return "0s";

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = days / 30L;
        long years = days / 365L;

        if (years > 0) {
            return years + yearShortString;
        } else if (months > 0) {
            return months + monthShortString;
        } else if (weeks > 0) {
            return weeks + weekShortString;
        } else if (days > 0) {
            return days + dayShortString;
        } else if (hours > 0) {
            return hours + hourShortString;
        } else if (minutes > 0) {
            return minutes + minuteShortString;
        } else {
            return seconds + secondShortString;
        }
    }

    /**
     * Parses a time string (e.g., "1d 2h") into milliseconds.
     *
     * @param time the time string to parse.
     * @return the time in milliseconds.
     */
    public static long parseTime(String time) {
        long totalTime = 0L;
        boolean found = false;

        Matcher matcher = parseTimePattern.matcher(time);

        while (matcher.find()) {
            String valueStr = matcher.group(1);
            String type = matcher.group(2);
            try {
                long value = Long.parseLong(valueStr);
                Long unitMultiplier = TIME_UNITS.get(type);
                if (unitMultiplier != null) {
                    totalTime += value * unitMultiplier;
                    found = true;
                }
            } catch (NumberFormatException e) {
                // Handle invalid number format
                System.err.println("Invalid number format: " + valueStr);
            }
        }

        return found ? totalTime * 1000 : -1;
    }

}
