package com.bot.slack.cab.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    public static String getPlannedStartAndEnd() {
        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();

        // Tính thời gian plannedStart (hiện tại + 1 tiếng)
        LocalDateTime plannedStart = now.plusHours(1);

        // Tính thời gian plannedEnd (plannedStart + 1 tiếng)
        LocalDateTime plannedEnd = plannedStart.plusHours(1);

        // Trả về chuỗi theo định dạng "Planned start - Planned end"
        return plannedStart.format(FORMATTER) + " - " + plannedEnd.format(FORMATTER);
    }
}
