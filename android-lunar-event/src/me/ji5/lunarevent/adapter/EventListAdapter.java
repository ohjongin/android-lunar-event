package me.ji5.lunarevent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import me.ji5.data.GoogleCalendar;
import me.ji5.data.GoogleEvent;
import me.ji5.lunarevent.R;
import me.ji5.utils.CalendarContentResolver;
import me.ji5.utils.Log;
import me.ji5.utils.MiscUtil;

/**
 * Created by ohjongin on 14. 1. 20.
 */
public class EventListAdapter extends ArrayAdapter<GoogleEvent> {
    protected final static boolean DEBUG_LOG = false;
    protected HashMap<Long, GoogleCalendar> mCalendarMap = new HashMap<Long, GoogleCalendar>();
    protected CalendarContentResolver mCalResolver;
    protected int mLayoutResId;

    public EventListAdapter(Context context, int resource, List<GoogleEvent> objects) {
        super(context, resource, objects);

        mLayoutResId = resource;

        mCalResolver = new CalendarContentResolver(context);
        mCalendarMap = mCalResolver.getCalendarHashMap();

        
        if (objects != null && objects.size() > 0) {
            clear();
            addAll(objects);
        } else {
            if (DEBUG_LOG) Log.e("Data array is NULL!!");
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mLayoutResId, null);
            viewHolder = createViewHolder(convertView, position);
            convertView.setTag(viewHolder);
        } else {
            if (convertView.getTag() == null) {
                viewHolder = createViewHolder(convertView, position);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
        }

        GoogleEvent event = getItem(position);
        if (event == null || viewHolder == null) {
            Log.e("GoogleEvent is NULL!!");
            return convertView;
        }

        viewHolder.tv_title.setText(event.mTitle);
        if (event.mTitle.contains("생일") || event.mTitle.contains("생신") || event.mTitle.contains("탄신") || event.mTitle.contains("태어")) {
            viewHolder.tv_title_sub.setText("(만" + MiscUtil.getInternationalAge(event.mDtStart) + "세)");
        } else {
            viewHolder.tv_title_sub.setText("(만" + MiscUtil.getInternationalAge(event.mDtStart) + "년)");
        }
        viewHolder.tv_subtitle.setText(MiscUtil.getDayDurationString(MiscUtil.getDayDuration(event.mComingBirthLunar, System.currentTimeMillis())));
        viewHolder.tv_desc.setText(MiscUtil.getDateString(null, event.mComingBirthLunar));
        viewHolder.tv_timestamp.setText(MiscUtil.getDateString("(음력) yyyy년 M월 d일", event.mDtStartLunar));
        viewHolder.v_cal_color_bar.setBackgroundColor(mCalendarMap.get(event.mCalendarId).mColor);

        return convertView;
    }

    public void sort(int sort_type) {
        switch (sort_type) {
            case 0:
                sort(GoogleEvent.compareTitle);
                break;
            case 1:
                sort(GoogleEvent.compareBirth);
                break;
            case 2:
                sort(GoogleEvent.compareRecent);
                break;
        }
        notifyDataSetChanged();
    }

    protected ViewHolder createViewHolder(View convertView, int position) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        viewHolder.tv_title_sub = (TextView) convertView.findViewById(R.id.tv_title_sub);
        viewHolder.tv_subtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
        viewHolder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
        viewHolder.tv_timestamp = (TextView) convertView.findViewById(R.id.tv_timestamp);
        viewHolder.v_cal_color_bar = (View) convertView.findViewById(R.id.cal_color_bar);

        return viewHolder;
    }

    protected static final class ViewHolder {
        public TextView tv_title;
        public TextView tv_title_sub;
        public TextView tv_subtitle;
        public TextView tv_desc;
        public TextView tv_timestamp;
        public View v_cal_color_bar;
    }
}
