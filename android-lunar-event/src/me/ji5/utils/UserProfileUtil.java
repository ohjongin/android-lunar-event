package me.ji5.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import me.ji5.lunarevent.R;

/**
 * Created by ohjongin on 14. 1. 20.
 */
public class UserProfileUtil {
    protected final static boolean DEBUG_LOG = false;
    protected final static String TAG = "UserProfileUtil";

    public static String getPrimaryEmailAddress(Context context) {
        String email_addr = null;

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
        for (Account account : accounts) {
            if (DEBUG_LOG) Log.d(account.name + ": " + account.toString());
            if (emailPattern.matcher(account.name).matches()) {
                email_addr = account.name;
                break;
            }
        }

        return email_addr;
    }



    @SuppressLint("NewApi")
    public static String getUserProfileDisplayName(Context context) {
        if (Build.VERSION.SDK_INT < 14) {
            Log.e(TAG, "SDK version is too low! Requiring 14 at least");
            return null;
        }

        if (!MiscUtil.hasPermission(context, "android.permission.READ_PROFILE")) {
            Log.e(TAG, "Permissiong missing... 'android.permission.READ_PROFILE'");
            return "";
        }

        if (!MiscUtil.hasPermission(context, "android.permission.READ_CONTACTS")) {
            Log.e(TAG, "Permissiong missing... 'android.permission.READ_CONTACTS'");
            return "";
        }


        // Sets the columns to retrieve for the user profile
        String[] projections = new String[] {
                ContactsContract.Profile._ID,
                ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
                ContactsContract.Profile.LOOKUP_KEY,
                ContactsContract.Profile.PHOTO_THUMBNAIL_URI,
                ContactsContract.Profile.IS_USER_PROFILE
        };

        // Retrieves the profile from the Contacts Provider
        Cursor c = context.getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI,
                projections ,
                null,
                null,
                null);

        String name = "";
        if (c != null) {
            int count = c.getCount();
            c.moveToFirst();
            do {
                try {
                    int is_user_profile = c.getInt(c.getColumnIndexOrThrow(ContactsContract.Profile.IS_USER_PROFILE));
                    if (count == 1 || (count > 1 && is_user_profile == 1)) {
                        name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Profile.DISPLAY_NAME_PRIMARY));
                    }
                    if (DEBUG_LOG) Log.e("[" + is_user_profile + "] - " + name);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (c.moveToNext());
            c.close();
        }

        return name;
    }

    @SuppressLint("NewApi")
    public static InputStream getUserProfilePhotoStream(Context context) {
        if (Build.VERSION.SDK_INT < 14) {
            Log.e(TAG, "SDK version is too low! Requiring 14 at least");
            return null;
        }

        if (!MiscUtil.hasPermission(context, "android.permission.READ_PROFILE")) {
            Log.e(TAG, "Permissiong missing... 'android.permission.READ_PROFILE'");
            return null;
        }

        if (!MiscUtil.hasPermission(context, "android.permission.READ_CONTACTS")) {
            Log.e(TAG, "Permissiong missing... 'android.permission.READ_CONTACTS'");
            return null;
        }

        // Sets the columns to retrieve for the user profile
        String[] projections = new String[]  {
                ContactsContract.Profile._ID,
                ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
                ContactsContract.Profile.LOOKUP_KEY,
                ContactsContract.Profile.PHOTO_THUMBNAIL_URI,
                ContactsContract.Profile.IS_USER_PROFILE
        };

        // Retrieves the profile from the Contacts Provider
        Cursor c = context.getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI,
                projections ,
                null,
                null,
                null);

        Uri photoUri = null;
        InputStream in = null;

        if (c != null) {
            int count = c.getCount();
            c.moveToFirst();
            do {
                try {
                    int is_user_profile = c.getInt(c.getColumnIndex(ContactsContract.Profile.IS_USER_PROFILE));
                    if (DEBUG_LOG) Log.e("[" + is_user_profile + "]" + c.getString(c.getColumnIndexOrThrow(ContactsContract.Profile.DISPLAY_NAME_PRIMARY)) + ", " + c.getString(c.getColumnIndexOrThrow(ContactsContract.Profile.PHOTO_THUMBNAIL_URI)));
                    if (count == 1 || (count > 1 && is_user_profile == 1)) {
                        photoUri = Uri.parse(c.getString(c.getColumnIndexOrThrow(ContactsContract.Profile.PHOTO_THUMBNAIL_URI)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (c.moveToNext());
            c.close();
        }

        if (photoUri != null) {
            try {
                in = context.getContentResolver().openInputStream(photoUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return in;
    }

    @SuppressLint("NewApi")
    public static Bitmap getUserProfilePhotoBitmap(Context context) {
        InputStream in = getUserProfilePhotoStream(context);

        return in == null ? null : BitmapFactory.decodeStream(in);
    }

    @SuppressLint("NewApi")
    public static byte[] getUserProfilePhotoByteArray(Context context) {
        Bitmap org = getUserProfilePhotoBitmap(context);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (org != null) {
            org.compress(Bitmap.CompressFormat.PNG, 100, bos);
        } else {
            Log.e(TAG, "Bitmap photo is NULL!!");
        }

        return org == null ? null : bos.toByteArray();
    }

    public static byte[] getBytesFromInputStream(InputStream is) {
        if (is == null) {
            Log.e(TAG, "Input parameter is NULL at getBytesFromInputStream()");
            return null;
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[0xFFFF];

            for (int len; (len = is.read(buffer)) != -1;)
                os.write(buffer, 0, len);

            os.flush();

            return os.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
