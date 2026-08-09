package com.wordcrush.server.common.util;

import com.wordcrush.server.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TimeFormats {

    public static final DateTimeFormatter GAME_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS");

    private TimeFormats() {
    }

    public static LocalDateTime parseGameTime(String value) {
        try {
            return LocalDateTime.parse(value, GAME_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BusinessException("time format must be yyyy-MM-dd-HH:mm:ss.SSS");
        }
    }

    public static String formatGameTime(LocalDateTime value) {
        return value.format(GAME_TIME_FORMATTER);
    }
}
