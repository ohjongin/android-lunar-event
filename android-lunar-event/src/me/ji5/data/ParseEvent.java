package me.ji5.data;

import com.parse.ParseObject;

/**
 * Created by ohjongin on 14. 1. 20.
 */
public class ParseEvent extends ParseObject {
    public ParseEvent(GoogleEvent event) {
        super();
    }


    public GoogleEvent getGoogleEvent() {
        GoogleEvent event = new GoogleEvent();
        event.mId = getLong("event_id");
        event.mCalendarId = getLong("calendar_id");
        event.mTitle = getString("title");
        event.mDescription = getString("description");
        event.mDtStart = getDate("dtstart").getTime();
        event.mDtEnd = getDate("dtend").getTime();
        event.mEventLocation = getString("location");

        return event;
    }
}
