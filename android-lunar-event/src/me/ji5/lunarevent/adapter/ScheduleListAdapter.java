package me.ji5.lunarevent.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import me.ji5.data.GoogleEvent;
import me.ji5.lunarevent.R;
import me.ji5.utils.CalendarContentResolver;
import me.ji5.utils.Log;
import me.ji5.utils.MiscUtil;

/**
 * Created by ohjongin on 14. 1. 27.
 */
public class ScheduleListAdapter extends ArrayAdapter<GoogleEvent> {
    protected final static boolean DEBUG_LOG = false;
    protected CalendarContentResolver mCalResolver;
    protected int mLayoutResId;

    public ScheduleListAdapter(Context context, int resource, List<GoogleEvent> objects) {
        super(context, resource, objects);

        mLayoutResId = resource;
        mCalResolver = new CalendarContentResolver(context);
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
            if (DEBUG_LOG) Log.e("GoogleEvent is NULL!!");
            return convertView;
        }

        viewHolder.tv_title.setText(MiscUtil.getDateString(null, event.mComingBirthLunar));
        viewHolder.tv_title_sub.setText("");
        viewHolder.tv_desc.setText("만 " + MiscUtil.getInternationalAge(event.mDtStartLunar, event.mComingBirthLunar) + "세");
        viewHolder.tv_timestamp.setText(MiscUtil.getDayDurationString(MiscUtil.getDayDuration(event.mComingBirthLunar, System.currentTimeMillis())));

        if (event.mId < 0) {
            viewHolder.tv_title.setTextColor(Color.LTGRAY);
            viewHolder.tv_desc.setTextColor(Color.LTGRAY);
            viewHolder.tv_timestamp.setTextColor(Color.LTGRAY);
        } else {
            viewHolder.tv_title.setTextColor(Color.BLACK);
            viewHolder.tv_desc.setTextColor(Color.BLACK);
            viewHolder.tv_timestamp.setTextColor(Color.BLACK);

        }

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
