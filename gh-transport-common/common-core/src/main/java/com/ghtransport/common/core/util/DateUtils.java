package com.ghtransport.common.core.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Objects;

/**
 * 日期时间工具类
 */
public class DateUtils {

    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_TIME = "HH:mm:ss";
    public static final String PATTERN_MONTH = "yyyy-MM";
    public static final String PATTERN_YEAR = "yyyy";
    public static final String PATTERN_DATETIME_MINUTE = "yyyy-MM-dd HH:mm";
    public static final String PATTERN_DATETIME_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";

    private DateUtils() {
    }

    // ==================== 日期格式化 ====================

    public static String format(LocalDateTime dateTime, String pattern) {
        if (Objects.isNull(dateTime)) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDate date, String pattern) {
        if (Objects.isNull(date)) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(Date date, String pattern) {
        if (Objects.isNull(date)) {
            return null;
        }
        return format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), pattern);
    }

    public static String formatDateTime(Date date) {
        return format(date, PATTERN_DATETIME);
    }

    public static String formatDate(Date date) {
        return format(date, PATTERN_DATE);
    }

    public static String formatTime(Date date) {
        return format(date, PATTERN_TIME);
    }

    // ==================== 日期解析 ====================

    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (Objects.isNull(dateTimeStr)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseDate(String dateStr, String pattern) {
        if (Objects.isNull(dateStr)) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalTime parseTime(String timeStr, String pattern) {
        if (Objects.isNull(timeStr)) {
            return null;
        }
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, PATTERN_DATETIME);
    }

    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, PATTERN_DATE);
    }

    // ==================== Date 转换 ====================

    public static LocalDateTime toLocalDateTime(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDate toLocalDate(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalDate localDate) {
        if (Objects.isNull(localDate)) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // ==================== 日期计算 ====================

    public static LocalDateTime addDays(LocalDateTime dateTime, int days) {
        return dateTime.plusDays(days);
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, int hours) {
        return dateTime.plusHours(hours);
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, int minutes) {
        return dateTime.plusMinutes(minutes);
    }

    public static LocalDateTime addSeconds(LocalDateTime dateTime, int seconds) {
        return dateTime.plusSeconds(seconds);
    }

    public static LocalDateTime addMonths(LocalDateTime dateTime, int months) {
        return dateTime.plusMonths(months);
    }

    public static LocalDateTime addWeeks(LocalDateTime dateTime, int weeks) {
        return dateTime.plusWeeks(weeks);
    }

    public static LocalDateTime addYears(LocalDateTime dateTime, int years) {
        return dateTime.plusYears(years);
    }

    public static LocalDate addDays(LocalDate date, int days) {
        return date.plusDays(days);
    }

    public static LocalDate addMonths(LocalDate date, int months) {
        return date.plusMonths(months);
    }

    // ==================== 日期区间 ====================

    /**
     * 获取一天的开始时间
     */
    public static LocalDateTime getDayStart(LocalDateTime dateTime) {
        return dateTime.with(LocalTime.MIN);
    }

    /**
     * 获取一天的结束时间
     */
    public static LocalDateTime getDayEnd(LocalDateTime dateTime) {
        return dateTime.with(LocalTime.MAX);
    }

    /**
     * 获取一周的开始（周一）
     */
    public static LocalDateTime getWeekStart(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
    }

    /**
     * 获取一周的结束（周日）
     */
    public static LocalDateTime getWeekEnd(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
    }

    /**
     * 获取月的开始
     */
    public static LocalDateTime getMonthStart(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
    }

    /**
     * 获取月的结束
     */
    public static LocalDateTime getMonthEnd(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
    }

    /**
     * 获取季度的开始
     */
    public static LocalDateTime getQuarterStart(LocalDateTime dateTime) {
        int month = dateTime.getMonthValue();
        int quarterStartMonth = (month - 1) / 3 * 3 + 1;
        return LocalDateTime.of(dateTime.getYear(), quarterStartMonth, 1, 0, 0, 0);
    }

    /**
     * 获取季度的结束
     */
    public static LocalDateTime getQuarterEnd(LocalDateTime dateTime) {
        int month = dateTime.getMonthValue();
        int quarterEndMonth = (month - 1) / 3 * 3 + 3;
        return LocalDateTime.of(dateTime.getYear(), quarterEndMonth,
                LocalDate.of(dateTime.getYear(), quarterEndMonth, 1).lengthOfMonth(), 23, 59, 59);
    }

    // ==================== 日期比较 ====================

    /**
     * 计算两个日期之间的天数差
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return end.toEpochDay() - start.toEpochDay();
    }

    /**
     * 计算两个日期时间之间的小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toHours();
    }

    /**
     * 计算两个日期时间之间的分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }

    /**
     * 判断是否为同一天
     */
    public static boolean isSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
    }

    /**
     * 判断是否为同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        return toLocalDate(date1).equals(toLocalDate(date2));
    }

    // ==================== 时间戳 ====================

    public static long currentTimestamp() {
        return System.currentTimeMillis();
    }

    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static long localDateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // ==================== 年龄计算 ====================

    /**
     * 计算年龄
     */
    public static int calculateAge(LocalDate birthDate) {
        return calculateAge(birthDate, LocalDate.now());
    }

    /**
     * 计算年龄（指定计算日期）
     */
    public static int calculateAge(LocalDate birthDate, LocalDate calculateDate) {
        if (birthDate.isAfter(calculateDate)) {
            return 0;
        }
        int age = calculateDate.getYear() - birthDate.getYear();
        if (calculateDate.getDayOfYear() < birthDate.getDayOfYear()) {
            age--;
        }
        return age;
    }

    // ==================== 农历转换（可选） ====================

    // 如需农历转换，可引入第三方库如 com.netease.httpcomponents: lunar-calendar
}
