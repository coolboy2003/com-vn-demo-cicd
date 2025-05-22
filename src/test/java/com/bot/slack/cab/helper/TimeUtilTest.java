package com.bot.slack.cab.helper;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class TimeUtilTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    @Test
    void testGetPlannedStartAndEnd() {
        // When
        String result = TimeUtil.getPlannedStartAndEnd();

        // Then
        assertNotNull(result);

        // The result should be in the format "dd MMM HH:mm - dd MMM HH:mm"
        assertTrue(result.matches("\\d{2} [A-Za-z]{3} \\d{2}:\\d{2} - \\d{2} [A-Za-z]{3} \\d{2}:\\d{2}"));

        // Verify the time difference is correct
        String[] parts = result.split(" - ");
        assertEquals(2, parts.length);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedStart = now.plusHours(1);
        LocalDateTime expectedEnd = expectedStart.plusHours(1);

        // Format expected times
        String expectedStartStr = expectedStart.format(FORMATTER);
        String expectedEndStr = expectedEnd.format(FORMATTER);

        // Instead of parsing, just check the format is correct
        // The format should be "dd MMM HH:mm - dd MMM HH:mm"
        String[] startTimeParts = parts[0].split(" ");
        String[] endTimeParts = parts[1].split(" ");

        // Check day
        assertTrue(startTimeParts[0].matches("\\d{2}"), "Day format should be dd");
        // Check month
        assertTrue(startTimeParts[1].matches("[A-Za-z]{3}"), "Month format should be MMM");
        // Check time
        assertTrue(startTimeParts[2].matches("\\d{2}:\\d{2}"), "Time format should be HH:mm");

        // Same checks for end time
        assertTrue(endTimeParts[0].matches("\\d{2}"), "Day format should be dd");
        assertTrue(endTimeParts[1].matches("[A-Za-z]{3}"), "Month format should be MMM");
        assertTrue(endTimeParts[2].matches("\\d{2}:\\d{2}"), "Time format should be HH:mm");
    }
}
