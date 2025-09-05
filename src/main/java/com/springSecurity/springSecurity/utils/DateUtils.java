package com.springSecurity.springSecurity.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtils {
    public static long getTimeMilliseconds(LocalDateTime localDateTime) {
        return localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}
