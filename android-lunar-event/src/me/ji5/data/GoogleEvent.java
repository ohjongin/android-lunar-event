package me.ji5.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import me.ji5.utils.IcuCalendarUtil;

/**
 * Created by ohjongin on 14. 1. 2.
 */
public class GoogleEvent implements Parcelable {
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
    public long mComingBirth;
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return event;
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

    public void calcDate() {
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
        coming_birth.set(cal_today.get(Calendar.YEAR), cal_birth_lunar.get(Calendar.MONTH), cal_birth_lunar.get(Calendar.DAY_OF_MONTH));
        // 올해 생일이 이미 지났다면, 내년 생일로 계산
        if (coming_birth.getTimeInMillis() < cal_today.getTimeInMillis()) {
            coming_birth.set(cal_today.get(Calendar.YEAR) + 1, cal_birth_lunar.get(Calendar.MONTH), cal_birth_lunar.get(Calendar.DAY_OF_MONTH));
        }

        // 올해 음력 생일
        Calendar cal_coming_birth_lunar = IcuCalendarUtil.getCalendarFromLunar(coming_birth.get(Calendar.YEAR), cal_birth_lunar.get(Calendar.MONTH) + 1, cal_birth_lunar.get(Calendar.DAY_OF_MONTH));

        mDtStartLunar = cal_birth_lunar.getTimeInMillis();
        mComingBirth = coming_birth.getTimeInMillis();
        mComingBirthLunar = cal_coming_birth_lunar.getTimeInMillis();
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
            return Long.valueOf(obj1.mComingBirth).compareTo(Long.valueOf(obj2.mComingBirth));
        }
    };

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id:" + mId);
        sb.append(", title:" + mTitle);
        sb.append(", calendar_id:" + mCalendarId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 (EEE)", Locale.KOREA);
        sb.append(", dtstart:" + sdf.format(new Date(mDtStart)) + ", " + mDtStart);
        sb.append(", dtend:" + sdf.format(new Date(mDtEnd)) + ", " + mDtEnd);
        sb.append(", app:" + mCustomAppPackage);
        sb.append(", mComingBirthLunar:" + mComingBirthLunar);

        return sb.toString();
    }
}
