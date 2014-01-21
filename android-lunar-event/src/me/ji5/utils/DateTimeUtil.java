package me.ji5.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ohjongin on 14. 1. 2.
 */
public class DateTimeUtil {
    public static long getTimeinMillis(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        // Note that months start from 0 (January)
        cal.set(year, month - 1, day);
        return cal.getTimeInMillis();
    }

    public static SimpleDateFormat getSimpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String getDateTimeString(long time_milis) {
        SimpleDateFormat sdf = getSimpleDateFormat();
        return sdf.format(new Date(time_milis));
    }
}
