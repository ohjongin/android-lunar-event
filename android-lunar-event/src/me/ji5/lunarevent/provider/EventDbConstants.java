package me.ji5.lunarevent.provider;

import android.provider.BaseColumns;
import android.provider.CalendarContract;

public interface EventDbConstants {
    public static final String DATABASE_NAME = "event.db";
    public static final int DATABASE_VERSION = 1;
    
    public static final String TABLE_EVENT = "t_event";

    /* COMMON FIELD */
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_UPDATED_AT = "updated_at";
    public static final String FIELD_BASECOLMUNS_ID = BaseColumns._ID;
    public static final String FIELD_SERIAL_NO = "serial_no";
    
    /* EVENT */
    public static final String FIELD_EVENT_ID = "event_id";
    public static final String FIELD_EVENT_TITLE = CalendarContract.Events.TITLE;
    public static final String FIELD_EVENT_DESCRIPTION = CalendarContract.Events.DESCRIPTION;
    public static final String FIELD_EVENT_START = CalendarContract.Events.DTSTART;
    public static final String FIELD_EVENT_END = CalendarContract.Events.DTEND;
    public static final String FIELD_EVENT_LOCATION = CalendarContract.Events.EVENT_LOCATION;
    public static final String FIELD_EVENT_CALENDAR_ID = CalendarContract.Events.CALENDAR_ID;
    public static final String FIELD_EVENT_ALLDAY = CalendarContract.Events.ALL_DAY;
    public static final String FIELD_EVENT_TIMEZONE = CalendarContract.Events.EVENT_TIMEZONE;
    public static final String FIELD_EVENT_CUSTOM_APP_PACKAGE = CalendarContract.Events.CUSTOM_APP_PACKAGE;
    public static final String FIELD_EVENT_HAS_ALARM = CalendarContract.Events.HAS_ALARM;
}
