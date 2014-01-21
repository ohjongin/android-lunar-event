package me.ji5.utils;

import android.content.Context;
import android.content.pm.PackageManager;

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

    public static String getValidString(String str) {
        return (str == null) ? "" : str;
    }
}
