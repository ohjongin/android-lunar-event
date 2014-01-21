package me.ji5.lunarevent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import me.ji5.data.GoogleCalendar;
import me.ji5.lunarevent.R;

/**
 * Created by ohjongin on 14. 1. 18.
 */
public class CalendarSpinnerAdapter extends ArrayAdapter<GoogleCalendar> {
    public CalendarSpinnerAdapter(Context context, int txtViewResourceId, ArrayList<GoogleCalendar> list) {
        super(context, txtViewResourceId, list);
    }

    @Override
    public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
        return getCustomView(position, cnvtView, prnt);
    }
    @Override
    public View getView(int pos, View cnvtView, ViewGroup prnt) {
        return getCustomView(pos, cnvtView, prnt);
    }
    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_spinner_cal_list, parent, false);
        View color = view.findViewById(R.id.view_cal_color);
        TextView cal_name = (TextView)view.findViewById(R.id.tv_cal_name);

        color.setBackgroundColor(getItem(position).mColor);
        cal_name.setText(getItem(position).mDisplayName);

        return view;
    }
}
