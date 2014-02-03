package me.ji5.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.Toast;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import me.ji5.utils.CalendarContentResolver;
import me.ji5.utils.IcuCalendarUtil;
import me.ji5.utils.Log;
import me.ji5.utils.MiscUtil;

/**
 * Created by ohjongin on 14. 1. 2.
 */
public class GoogleEvent implements Parcelable {
    protected final static boolean DEBUG_LOG = false;
    public static final String PARSE_CLASSNAME = "EventBirth";

    public long mId;
    public long mCalendarId;
    public String mTitle;
    public String mDescription;
    public long mDtStart;
    public long mDtEnd;
    public String mEventLocation;
    public String mCustomAppPackage;

    public String mParseObjectId;
    public long mDtStartLunar;
    public long mComingBirthLunar;

    public static String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.CUSTOM_APP_PACKAGE,
            CalendarContract.Events.HAS_ALARM,
            CalendarContract.Events.ALL_DAY};

    public GoogleEvent() {

    }

    public static GoogleEvent getInstance(Cursor cursor) {
        GoogleEvent event = new GoogleEvent();

        try {
            event.mId = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID));
            event.mCalendarId = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID));
            event.mTitle = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE));
            event.mDescription = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION));
            event.mDtStart = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART));
            event.mDtEnd = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND));
            event.mEventLocation = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION));
            event.mCustomAppPackage = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.CUSTOM_APP_PACKAGE));

            event.calcDate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return event;
    }
    
    public Uri insert(Context context, Uri content_uri) {
        ContentValues values = new ContentValues();
        ContentResolver cr = context.getContentResolver();

        values.put(CalendarContract.Events.DTSTART, mDtStart);
        values.put(CalendarContract.Events.DTEND, mDtEnd);
        values.put(CalendarContract.Events.TITLE, mTitle);
        values.put(CalendarContract.Events.DESCRIPTION, mDescription + "\n" + "만 " + MiscUtil.getInternationalAge(mDtStart, System.currentTimeMillis())  + "세 생일");
        values.put(CalendarContract.Events.CALENDAR_ID, mCalendarId);
        values.put(CalendarContract.Events.CUSTOM_APP_PACKAGE, context.getPackageName());
        values.put(CalendarContract.Events.ALL_DAY, 1);
        values.put(CalendarContract.Events.EVENT_LOCATION, mEventLocation);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        Uri uri = cr.insert(content_uri, values);

        return uri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeLong(this.mCalendarId);
        dest.writeString(this.mTitle);
        dest.writeString(this.mDescription);
        dest.writeLong(this.mDtStart);
        dest.writeLong(this.mDtEnd);
        dest.writeString(this.mEventLocation);
        dest.writeString(this.mParseObjectId);
        dest.writeString(this.mCustomAppPackage);
        dest.writeStringArray(this.EVENT_PROJECTION);
    }

    private GoogleEvent(Parcel in) {
        this.mId = in.readLong();
        this.mCalendarId = in.readLong();
        this.mTitle = in.readString();
        this.mDescription = in.readString();
        this.mDtStart = in.readLong();
        this.mDtEnd = in.readLong();
        this.mEventLocation = in.readString();
        this.mParseObjectId = in.readString();
        this.mCustomAppPackage = in.readString();
        this.EVENT_PROJECTION = in.createStringArray();
    }

    public static Parcelable.Creator<GoogleEvent> CREATOR = new Parcelable.Creator<GoogleEvent>() {
        public GoogleEvent createFromParcel(Parcel source) {
            return new GoogleEvent(source);
        }

        public GoogleEvent[] newArray(int size) {
            return new GoogleEvent[size];
        }
    };

    public GoogleEvent calcDate() {
        // 올해 음력 생일 계산을 위한 오늘 날짜
        final Calendar cal_today = Calendar.getInstance();
        cal_today.setTime(new Date());

        GoogleEvent event = calcDate(cal_today.get(Calendar.YEAR));
        if (event.mComingBirthLunar < cal_today.getTimeInMillis()) {
            event.calcDate(cal_today.get(Calendar.YEAR) + 1);
        }

        return event;
    }

    public GoogleEvent calcDate(int year) {
        // 올해 음력 생일 계산을 위한 오늘 날짜
        final Calendar cal_today = Calendar.getInstance();
        cal_today.setTime(new Date());

        // 양력 생일
        Calendar cal_birth = Calendar.getInstance();
        cal_birth.setTimeInMillis(mDtStart);

        // 양력 생일로부터 음력 생일과 금년도 음력 생일 날짜를 계산
        Calendar cal_birth_lunar = IcuCalendarUtil.getLunarCalendar(cal_birth);

        // 올해 생일
        Calendar coming_birth = Calendar.getInstance();
        coming_birth.set(year, cal_birth_lunar.get(Calendar.MONTH), cal_birth_lunar.get(Calendar.DAY_OF_MONTH));

        // 올해 음력 생일
        Calendar cal_coming_birth_lunar = IcuCalendarUtil.getCalendarFromLunar(coming_birth.get(Calendar.YEAR), cal_birth_lunar.get(Calendar.MONTH) + 1, cal_birth_lunar.get(Calendar.DAY_OF_MONTH));

        mDtStartLunar = cal_birth_lunar.getTimeInMillis();
        mComingBirthLunar = cal_coming_birth_lunar.getTimeInMillis();

        return this;
    }

    public long findEventId(Context context) {
        CalendarContentResolver ccr = new CalendarContentResolver(context);

        long start = mComingBirthLunar - DateUtils.DAY_IN_MILLIS;
        long end = mComingBirthLunar + DateUtils.DAY_IN_MILLIS;
        String selection = "((" + CalendarContract.Events.DTSTART + " >= " + start + ") AND (" + CalendarContract.Events.DTEND + " <= " + end + ") AND (" + CalendarContract.Events.TITLE + "='"  + mTitle.trim() + "'))";

        ArrayList<GoogleEvent> eventList = ccr.getEventList(selection);
        if (DEBUG_LOG) Log.e("event.mComingBirthLunar: " + MiscUtil.getDateString(null, mComingBirthLunar) + ", " + mComingBirthLunar);

        if (eventList.size() > 0) {
            this.mId = eventList.get(0).mId;
        }

        return (eventList.size() > 0) ? eventList.get(0).mId : -1;
    }

    public long addToCalendar(Context context) {
        this.mId = addToCalendar(context, this);
        return this.mId;
    }

    public int getYear() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mComingBirthLunar);

        return cal.get(Calendar.YEAR);
    }

    public static long addToCalendar(Context context, GoogleEvent event) {
        long event_id = -1;

        if (TextUtils.isEmpty(event.mTitle)) {
            Toast.makeText(context, "이벤트 제목이 유효하지 않아서 추가하지 못했습니다.", Toast.LENGTH_LONG).show();
            return event_id;
        }

        CalendarContentResolver ccr = new CalendarContentResolver(context);

        long start = event.mComingBirthLunar - DateUtils.DAY_IN_MILLIS;
        long end = event.mComingBirthLunar + DateUtils.DAY_IN_MILLIS;
        String selection = "((" + CalendarContract.Events.DTSTART + " >= " + start + ") AND (" + CalendarContract.Events.DTEND + " <= " + end + ") AND (" + CalendarContract.Events.TITLE + "='"  + event.mTitle.trim() + "'))";

        ArrayList<GoogleEvent> eventList = ccr.getEventList(selection);
        if (DEBUG_LOG) Log.e("event.mComingBirthLunar: " + MiscUtil.getDateString(null, event.mComingBirthLunar) + ", " + event.mComingBirthLunar);

        if (eventList.size() < 1) {
            event_id = ccr.addEvent(event);
            if (event_id < 0) {
                Toast.makeText(context, "이벤트(" + event.mTitle + ") 추가 중에 알수 없는 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "이벤트(" + event.mTitle + ")를 구글캘린더에 추가하였습니다. 네트워크 상황에 따라 반영에 시간 지연이 있을 수 있습니다.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "동일한 이름(" + event.mTitle + ")의 이벤트가 존재합니다.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            // Android 2.2+
            intent.setData(Uri.parse("content://com.android.calendar/events/" + String.valueOf(eventList.get(0).mId)));
            // Android 2.1 and below.
            // intent.setData(Uri.parse("content://calendar/events/" + String.valueOf(calendarEventID)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        event.mId = event_id;

        return event_id;
    }

    //Comparator 를 만든다.
    public final static Comparator<GoogleEvent> compareTitle = new Comparator<GoogleEvent>() {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(GoogleEvent obj1,GoogleEvent obj2) {
            return collator.compare(obj1.mTitle, obj2.mTitle);
        }
    };

    //Comparator 를 만든다.
    public final static Comparator<GoogleEvent> compareBirth = new Comparator<GoogleEvent>() {
        @Override
        public int compare(GoogleEvent obj1,GoogleEvent obj2) {
            return Long.valueOf(obj1.mDtStart).compareTo(Long.valueOf(obj2.mDtStart));
        }
    };

    //Comparator 를 만든다.
    public final static Comparator<GoogleEvent> compareRecent = new Comparator<GoogleEvent>() {
        @Override
        public int compare(GoogleEvent obj1,GoogleEvent obj2) {
            obj1.calcDate();
            obj2.calcDate();
            return Long.valueOf(obj1.mComingBirthLunar).compareTo(Long.valueOf(obj2.mComingBirthLunar));
        }
    };

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 (EEE)", Locale.KOREA);

        StringBuilder sb = new StringBuilder();
        sb.append("id:" + mId);
        sb.append(", title:" + mTitle);
        sb.append(", calendar_id:" + mCalendarId);
        sb.append(", dtstart:" + sdf.format(new Date(mDtStart)) + ", " + mDtStart);
        sb.append(", dtend:" + sdf.format(new Date(mDtEnd)) + ", " + mDtEnd);
        sb.append(", mComingBirthLunar:" + sdf.format(new Date(mComingBirthLunar)) + ", " + mComingBirthLunar);
        sb.append(", mDtStartLunar:" + sdf.format(new Date(mDtStartLunar)) + ", " + mDtStartLunar);
        sb.append(", app:" + mCustomAppPackage);

        return sb.toString();
    }

    public boolean equals(GoogleEvent event) {
        boolean result = false;

        if (event != null
                && mTitle.equals(event.mTitle)
                && mDtStart == event.mDtStart) result = true;

        return result;
    }

    @Override
    public GoogleEvent clone() {
        GoogleEvent event = new GoogleEvent();

        event.mId = this.mId;
        event.mTitle = new String(this.mTitle);
        event.mDtStart = this.mDtStart;
        event.mDtEnd = this.mDtEnd;
        event.mDescription = new String(this.mDescription);
        event.mCalendarId = this.mCalendarId;
        event.mCustomAppPackage = new String(this.mCustomAppPackage);
        event.mEventLocation = new String(this.mEventLocation);

        return event;
    }
}
