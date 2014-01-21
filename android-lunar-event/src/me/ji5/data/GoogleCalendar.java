package me.ji5.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;

/**
 * Created by ohjongin on 14. 1. 2.
 */
public class GoogleCalendar implements Parcelable {
    public long mId;
    public String mName;
    public String mDisplayName;
    public int mColor;
    public boolean mSelected;

    public static String[] CALENDARS_PROJECTION = {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.VISIBLE };

    public GoogleCalendar() {

    }

    public static GoogleCalendar getInstance(Cursor cursor) {
        GoogleCalendar cal = new GoogleCalendar();

        try {
            cal.mId = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID));
            cal.mName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.NAME));
            cal.mDisplayName = cursor.getString(1);
            // This is actually a better pattern:
            cal.mColor = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR));
            cal.mSelected = !cursor.getString(3).equals("0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cal;
    }

    public String toString() {
        return mDisplayName;
    }

    public String toString(boolean debug) {
        if (!debug) return toString();

        StringBuilder sb = new StringBuilder();
        sb.append("id: " + mId + ", ");
        sb.append("name: " + mName + ", ");
        sb.append("display name: " + mDisplayName + ", ");
        sb.append("color: " + String.format("#%06X", 0xFFFFFF & mColor) + ", ");
        sb.append("selected: " + mSelected);

        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeString(this.mName);
        dest.writeString(this.mDisplayName);
        dest.writeInt(this.mColor);
        dest.writeByte(mSelected ? (byte) 1 : (byte) 0);
        dest.writeStringArray(this.CALENDARS_PROJECTION);
    }

    private GoogleCalendar(Parcel in) {
        this.mId = in.readLong();
        this.mName = in.readString();
        this.mDisplayName = in.readString();
        this.mColor = in.readInt();
        this.mSelected = in.readByte() != 0;
        this.CALENDARS_PROJECTION = in.createStringArray();
    }

    public static Parcelable.Creator<GoogleCalendar> CREATOR = new Parcelable.Creator<GoogleCalendar>() {
        public GoogleCalendar createFromParcel(Parcel source) {
            return new GoogleCalendar(source);
        }

        public GoogleCalendar[] newArray(int size) {
            return new GoogleCalendar[size];
        }
    };
}
