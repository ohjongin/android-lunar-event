package me.ji5.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import me.ji5.data.GoogleEvent;
import me.ji5.lunarevent.R;

/**
 * Created by ohjongin on 14. 1. 20.
 */
public class ParseUtil {
    public static boolean isAuthenticated() {
        boolean is_authenticated = (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated());

        return is_authenticated;
    }

    public static void loginParse(final Context context) {
        final String username = UserProfileUtil.getPrimaryEmailAddress(context);

        if (TextUtils.isEmpty(username)) {
            Log.e("Username is EMPTY!!!");
            return;
        }

        ParseUser.logInInBackground(username, username, new LogInCallback() {
            @Override
            public void done(ParseUser user, com.parse.ParseException e) {
                if (user != null) {
                    ParseUser me = ParseUser.getCurrentUser();
                    Calendar calendar = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
                    me.put("lastLogin", calendar.getTime());
                    try {
                        Toast.makeText(context, R.string.login_success, Toast.LENGTH_SHORT).show();
                        me.save();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    Toast.makeText(context, context.getString(R.string.account_not_registered) + "\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                    ParseUser user_signup = new ParseUser();

                    byte[] photo_array = UserProfileUtil.getUserProfilePhotoByteArray(context);
                    if (photo_array != null) {
                        ParseFile photoFile = new ParseFile(UserProfileUtil.getUserProfileDisplayName(context).replaceAll("[^a-zA-Z0-9.-]", "_") + ".png", photo_array);
                        try {
                            photoFile.save();
                            user_signup.put("photo", photoFile);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }

                    ParseACL acl = new ParseACL();
                    acl.setPublicReadAccess(false);
                    acl.setPublicWriteAccess(false);
                    if (ParseUser.getCurrentUser() != null) {
                        acl.setReadAccess(ParseUser.getCurrentUser(), true);
                        acl.setWriteAccess(ParseUser.getCurrentUser(), true);
                    }

                    user_signup.setUsername(username);
                    user_signup.setPassword(username);
                    user_signup.setEmail(username);
                    if (MiscUtil.hasPermission(context, "android.permission.READ_PHONE_STATE")) {
                        user_signup.put("phoneNumber", tm.getLine1Number());
                        user_signup.put("imei", tm.getDeviceId());
                    }
                    user_signup.put("displayName", UserProfileUtil.getUserProfileDisplayName(context));
                    user_signup.setACL(acl);

                    user_signup.signUpInBackground(new SignUpCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                // Hooray! Let them use the app now.
                                Toast.makeText(context, R.string.account_registered, Toast.LENGTH_SHORT).show();
                                loginParse(context);
                            } else {
                                // Sign up didn't succeed. Look at the ParseException
                                // to figure out what went wrong
                                Toast.makeText(context, context.getString(R.string.account_registered_fail) + "\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    // login failed. Look at the ParseException to see what happened.
                    Toast.makeText(context, context.getString(R.string.login_fail) + "\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    public static ParseObject getParseObject(GoogleEvent event) {
        ParseObject po = new ParseObject(GoogleEvent.PARSE_CLASSNAME);
        po.put("event_id", event.mId);
        po.put("calendar_id", event.mCalendarId);
        po.put("title", MiscUtil.getValidString(event.mTitle));
        po.put("description", MiscUtil.getValidString(event.mDescription));
        po.put("dtstart", new Date(event.mDtStart));
        po.put("dtend", new Date(event.mDtEnd));
        po.put("location", MiscUtil.getValidString(event.mEventLocation));
        return po;
    }

    public static GoogleEvent getGoogleEvent(ParseObject po) {
        if (GoogleEvent.PARSE_CLASSNAME != po.getClassName()) {
            Log.e("Classname is mismatch!! " + po.getClassName());
            return null;
        }

        GoogleEvent event = new GoogleEvent();
        event.mId = po.getLong("event_id");
        event.mCalendarId = po.getLong("calendar_id");
        event.mTitle = MiscUtil.getValidString(po.getString("title"));
        event.mDescription = MiscUtil.getValidString(po.getString("description"));
        event.mDtStart = po.getDate("dtstart").getTime();
        event.mDtEnd = po.getDate("dtend").getTime();
        event.mEventLocation = MiscUtil.getValidString(po.getString("location"));
        event.mParseObjectId = po.getObjectId();

        return event;
    }
}
