package me.ji5.lunarevent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.ji5.data.GoogleEvent;
import me.ji5.lunarevent.R;
import me.ji5.utils.IcuCalendarUtil;
import me.ji5.utils.Log;
import me.ji5.utils.MiscUtil;

/**
 * Created by ohjongin on 14. 1. 20.
 */
public class EventListAdapter extends ArrayAdapter<GoogleEvent> {
    protected final static boolean DEBUG_LOG = false;
    protected ArrayList<GoogleEvent> mEventList = new ArrayList<GoogleEvent>();
    protected int mLayoutResId;

    public EventListAdapter(Context context, int resource, List<GoogleEvent> objects) {
        super(context, resource, objects);

        mLayoutResId = resource;
        
        if (objects != null && objects.size() > 0) {
            addAll(objects);
        } else {
            Log.e("Data array is NULL!!");
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
            if (DEBUG_LOG) Log.e("GoogleEvent is NULL!!");
            return convertView;
        }

        // 올해 음력 생일 계산을 위한 오늘 날짜
        final Calendar cal_today = Calendar.getInstance();
        cal_today.setTime(new Date());

        // 양력 생일
        Calendar cal_birth = Calendar.getInstance();
        cal_birth.setTimeInMillis(event.mDtStart);

        // 양력 생일로부터 음력 생일과 금년도 음력 생일 날짜를 계산
        Calendar cal_birth_lunar = IcuCalendarUtil.getLunarCalendar(cal_birth);

        // 올해 음력 생일
        Calendar cal_this_lunar_birth = IcuCalendarUtil.getCalendarFromLunar(cal_today.get(Calendar.YEAR), cal_birth_lunar.get(Calendar.MONTH) + 1, cal_birth_lunar.get(Calendar.DAY_OF_MONTH));

        viewHolder.tv_title.setText(event.mTitle);
        viewHolder.tv_title_sub.setText("(만" + MiscUtil.getInternationalAge(cal_birth.get(Calendar.YEAR), cal_birth.get(Calendar.MONTH) + 1, cal_birth.get(Calendar.DAY_OF_MONTH)) + "세)");
        viewHolder.tv_subtitle.setText(MiscUtil.getDateString(null, cal_this_lunar_birth.getTimeInMillis()));
        viewHolder.tv_desc.setText(MiscUtil.getDateString("(음력) yyyy년 M월 d일", cal_birth_lunar.getTimeInMillis()));
        viewHolder.tv_timestamp.setText(MiscUtil.getDateString(null, event.mDtStart));

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

        return viewHolder;
    }

    protected static final class ViewHolder {
        public TextView tv_title;
        public TextView tv_title_sub;
        public TextView tv_subtitle;
        public TextView tv_desc;
        public TextView tv_timestamp;
    }
}
