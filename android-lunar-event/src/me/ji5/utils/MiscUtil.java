package me.ji5.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ohjongin on 14. 1. 17.
 */
public class MiscUtil {
    protected final static boolean DEBUG_LOG = false;
    protected final static String TAG = "MiscUtil";

    /**
     * Get international age from birthday
     *
     * @param birth_year
     * @param birth_month Janunary is '1' although January is '0' at Calendar class
     * @param birth_day
     *
     * @return A new instance of fragment NewEventFragment.
     */
    public static int getInternationalAge(int birth_year, int birth_month, int birth_day) {
        Calendar cal_birth = Calendar.getInstance ();
        cal_birth.set(Calendar.YEAR, birth_year);
        cal_birth.set(Calendar.MONTH, birth_month - 1);
        cal_birth.set(Calendar.DATE, birth_day);

        return getInternationalAge(cal_birth);
    }

    public static int getInternationalAge(long time_milis) {
        Calendar cal_birth = Calendar.getInstance ();
        cal_birth.setTimeInMillis(time_milis);

        return getInternationalAge(cal_birth);
    }

    public static int getInternationalAge(Calendar cal_birth) {
        Calendar now = Calendar.getInstance ();

        int age = now.get(Calendar.YEAR) - cal_birth.get(Calendar.YEAR);
        if ((cal_birth.get(Calendar.MONTH) > now.get(Calendar.MONTH))
                || (cal_birth.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                && cal_birth.get(Calendar.DAY_OF_MONTH) > now.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }

    public static boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static String getDateString(String format, long time_in_milis) {
        if (format == null) format = "yyyy년 M월 d일 (EEE)";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.KOREA);
        return sdf.format(new Date(time_in_milis));
    }

    public static int getDayDuration(int year1, int month1, int day1, int year2, int month2, int day2)  {
        Calendar src = Calendar.getInstance();
        src.set(year1, month1, day1, 0, 0, 0);
        Calendar dest = Calendar.getInstance();
        dest.set(year2, month2, day2, 0, 0, 0);

        return getDayDuration(src, dest);
    }

    public static int getDayDuration(Calendar src, Calendar dest)  {
        long duration = (src.getTimeInMillis() > dest.getTimeInMillis()) ? src.getTimeInMillis() - dest.getTimeInMillis() : dest.getTimeInMillis() - src.getTimeInMillis();

        return (int)(duration / DateUtils.DAY_IN_MILLIS);
    }

    public static String getDayDurationString(int duration) {
        String result;

        if (duration % 7 == 0) {
            if (duration == 7) result = "일주일 전";
            else result = (duration / 7) + "주 전";
        } else if (duration == 0) {
            result = "오늘";
        } else if (duration < 0) {
            result = Math.abs(duration) + "일 후";
        } else {
            result = duration + "일 전";
        }

        return result;
    }

    public static String getValidString(String str) {
        return (str == null) ? "" : str;
    }
}
