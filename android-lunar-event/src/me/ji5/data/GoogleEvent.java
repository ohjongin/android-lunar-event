package me.ji5.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public static String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION};

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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id:" + mId);
        sb.append(", title:" + mTitle);
        sb.append(", calendar_id:" + mCalendarId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 (EEE)", Locale.KOREA);
        sb.append(", dtstart:" + sdf.format(new Date(mDtStart)));
        sb.append(", dtend:" + sdf.format(new Date(mDtEnd)));

        return sb.toString();
    }
}
