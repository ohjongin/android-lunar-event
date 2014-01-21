package me.ji5.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.ArrayList;

import me.ji5.data.GoogleCalendar;
import me.ji5.data.GoogleEvent;

/**
 * Created by ohjongin on 14. 1. 2.
 */
public class CalendarContentResolver {
    public static final Uri CALENDAR_URI = CalendarContract.Calendars.CONTENT_URI;
    public static final Uri EVENT_URI = CalendarContract.Events.CONTENT_URI;

    ContentResolver contentResolver;

    public CalendarContentResolver(Context context) {
        contentResolver = context.getContentResolver();
    }    
    
    public ArrayList<GoogleEvent> getEventList(long start, long end) {
        ArrayList<GoogleEvent> eventList = new ArrayList<GoogleEvent>();

        String selection = "((" + CalendarContract.Events.DTSTART + " >= " + start + ") AND (" + CalendarContract.Events.DTEND + " <= " + end + "))";

        Cursor cursor = contentResolver.query(EVENT_URI, GoogleEvent.EVENT_PROJECTION, selection, null, CalendarContract.Events.DTSTART + " ASC");

        try {
            eventList.clear();
            while (cursor.moveToNext()) {
                eventList.add(GoogleEvent.getInstance(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventList;
    }

    public ArrayList<GoogleCalendar> getCalendarList() {
        ArrayList<GoogleCalendar> calendarList = new ArrayList<GoogleCalendar>();

        // Fetch a list of all calendars sync'd with the device and their display names
        Cursor cursor = contentResolver.query(CALENDAR_URI, GoogleCalendar.CALENDARS_PROJECTION, null, null, null);

        try {
            calendarList.clear();
            while (cursor.moveToNext()) {
                calendarList.add(GoogleCalendar.getInstance(cursor));
            }
        } catch (AssertionError ex) {
            ex.printStackTrace();
        }
        return calendarList;
    }

    public long addEvent(GoogleEvent ge) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, ge.mDtStart);
        values.put(CalendarContract.Events.DTEND, ge.mDtEnd);
        values.put(CalendarContract.Events.TITLE, ge.mTitle);
        values.put(CalendarContract.Events.DESCRIPTION, ge.mDescription);
        values.put(CalendarContract.Events.CALENDAR_ID, ge.mCalendarId);
        // values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles");
        Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static void testGetEventList(Context context) {
        CalendarContentResolver cr = new CalendarContentResolver(context);
        ArrayList<GoogleEvent> event_list = cr.getEventList(DateTimeUtil.getTimeinMillis(2013, 1, 1), DateTimeUtil.getTimeinMillis(2013, 12, 31));
        for(GoogleEvent GoogleEvent : event_list) {
            Log.e("[" + GoogleEvent.mCalendarId + "] " + GoogleEvent.mTitle + ", " + DateTimeUtil.getDateTimeString(GoogleEvent.mDtStart) + " ~ " + DateTimeUtil.getDateTimeString(GoogleEvent.mDtEnd));
        }
    }

    public static void testGetCalendarList(Context context) {
        CalendarContentResolver cr = new CalendarContentResolver(context);
        ArrayList<GoogleCalendar> calendarList = cr.getCalendarList();
        for(GoogleCalendar cal : calendarList) {
            Log.e("[" + cal.mId + "] " + cal.mName + ", " + cal.mDisplayName);
        }
    }
}
