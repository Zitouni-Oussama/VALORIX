package com.recyclix.backend.util;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@UtilityClass
public class DateUtils {

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN);
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(Constants.DATETIME_PATTERN);

    // ---- Formatting ----
    public static String format(LocalDate date) {
        return date == null ? null : date.format(DATE_FMT);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FMT);
    }

    // ---- Ranges ----
    public static LocalDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date == null ? null : date.atTime(LocalTime.MAX);
    }

    public static LocalDate startOfWeek(LocalDate date) {
        if (date == null) return null;
        // Lundi comme début de semaine
        return date.with(DayOfWeek.MONDAY);
    }

    public static LocalDate endOfWeek(LocalDate date) {
        if (date == null) return null;
        return date.with(DayOfWeek.SUNDAY);
    }

    public static LocalDate startOfMonth(LocalDate date) {
        return date == null ? null : date.with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate endOfMonth(LocalDate date) {
        return date == null ? null : date.with(TemporalAdjusters.lastDayOfMonth());
    }

    // ---- Conversions ----
    public static LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    // ---- Validation ----
    public static void requireValidRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) return;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("endDate ne peut pas être avant startDate");
        }
    }

    public static void requireValidRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("endDateTime ne peut pas être avant startDateTime");
        }
    }
}