package com.rakuishi.weight;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

public class LocalDateTimeUtil {

    public static LocalDateTime from(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    public static long toEpochMilli(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static String formatLocalizedDate(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public static String formatLocalizedTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
    }
}
