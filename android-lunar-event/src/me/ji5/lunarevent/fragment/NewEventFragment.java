package me.ji5.lunarevent.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.datetimepicker.date.DatePickerDialog;
import com.ibm.icu.util.ChineseCalendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.ji5.data.GoogleCalendar;
import me.ji5.data.GoogleEvent;
import me.ji5.data.LunarCalendar;
import me.ji5.lunarevent.R;
import me.ji5.lunarevent.adapter.CalendarSpinnerAdapter;
import me.ji5.utils.CalendarContentResolver;
import me.ji5.utils.Log;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewEventFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class NewEventFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    protected final static boolean DEBUG_LOG = false;

    protected OnFragmentInteractionListener mListener;
    protected CalendarContentResolver mCalResolver;
    protected ArrayList<GoogleCalendar> mCalendarList;
    protected GoogleEvent mEvent = new GoogleEvent();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewEventFragment.
     */
    public static NewEventFragment newInstance() {
        NewEventFragment fragment = new NewEventFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NewEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        setHasOptionsMenu(true);

        mCalResolver = new CalendarContentResolver(getBaseContext());
        mCalendarList = mCalResolver.getCalendarList();
        for(GoogleCalendar cal : mCalendarList) {
            Log.d(cal.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_event, container, false);

        CalendarSpinnerAdapter adapter = new CalendarSpinnerAdapter(getBaseContext(), R.layout.layout_spinner_cal_list, mCalendarList);
        //스피너 속성
        Spinner sp = (Spinner) view.findViewById(R.id.spinner_cal_list);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(this);

        if (mCalendarList.size() > 0) {
            mEvent.mCalendarId = mCalendarList.get(0).mId;
        }

        view.findViewById(R.id.et_date).setOnClickListener(this);
        view.findViewById(R.id.btn_done).setOnClickListener(this);

        Calendar cal_birth = Calendar.getInstance();
        cal_birth.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 (EEE)", Locale.KOREA);
        TextView et_date = (TextView)view.findViewById(R.id.et_date);
        et_date.setText(sdf.format(new Date(cal_birth.getTimeInMillis())));

        mEvent.mDtStart = mEvent.mDtEnd = cal_birth.getTimeInMillis();

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed = false;

        switch (item.getItemId()) {
            case R.id.action_settings:
                // Not implemented here
                break;
            case R.id.action_add:

                consumed = true;
                break;
            default:
                break;
        }

        return consumed;
    }

    public void onActionDone(Uri uri, Intent intent) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri, intent);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected Context getBaseContext() {
        return getActivity();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        GoogleCalendar cal = (GoogleCalendar)parent.getItemAtPosition(position);
        View root = getView();
        root.findViewById(R.id.view_cal_color).setBackgroundColor(cal.mColor);
        mEvent.mCalendarId = cal.mId;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.et_date:
                Vibrator vib = (Vibrator)getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(20);
                chooseDate();
                break;
            case R.id.btn_done:
                mEvent.mTitle = ((TextView)getView().findViewById(R.id.et_event_name)).getText().toString();

                Intent intent = new Intent();
                intent.putExtra("event", mEvent);
                onActionDone(Uri.parse("action://done"), intent);
                break;
        }
    }

    protected void chooseDate() {
        final Calendar calDefault = Calendar.getInstance();
        calDefault.setTimeInMillis(mEvent.mDtStart);
        DatePickerDialog dlg = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
                LunarCalendar lc = new LunarCalendar();
                ChineseCalendar cc = lc.toLunar(year, monthOfYear + 1, dayOfMonth);

                int lunar_year = LunarCalendar.getYear(cc);
                int lunar_month = LunarCalendar.getMonth(cc);
                int lunar_day = LunarCalendar.getDay(cc);

                final Calendar cal_today = Calendar.getInstance();
                cal_today.setTime(new Date());
                com.ibm.icu.util.Calendar this_year = lc.fromLunar(cal_today.get(Calendar.YEAR), lunar_month, lunar_day);
                
                if (DEBUG_LOG) Log.e("양력: " + year, "년 ", monthOfYear + 1, "월 ", dayOfMonth, "일");
                if (DEBUG_LOG) Log.e("음력: " + lunar_year + "년 " + lunar_month + "월 " + lunar_day + "일");
                if (DEBUG_LOG) Log.e("올해 음력 생일: " + LunarCalendar.getYear(this_year)+ "년 "
                        + LunarCalendar.getMonth(this_year) + "월 "
                        + LunarCalendar.getDay(this_year) + "일");

                Calendar cal_birth = Calendar.getInstance();
                cal_birth.set(year, monthOfYear, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 (EEE)", Locale.KOREA);
                TextView et_date = (TextView)getView().findViewById(R.id.et_date);
                et_date.setText(sdf.format(new Date(cal_birth.getTimeInMillis())));

                mEvent.mDtStart = mEvent.mDtEnd = cal_birth.getTimeInMillis();

                Calendar cal_lunar = Calendar.getInstance();
                cal_lunar.set(lunar_year, lunar_month - 1, lunar_day);
                SimpleDateFormat sdf_lunar = new SimpleDateFormat("yyyy년 M월 d일 (음력)", Locale.KOREA);
                TextView tv_lunar_date = (TextView)getView().findViewById(R.id.tv_lunar_date);
                tv_lunar_date.setText(sdf_lunar.format(new Date(cal_lunar.getTimeInMillis())));
                tv_lunar_date.setVisibility(View.VISIBLE);
            }
        }, calDefault.get(Calendar.YEAR), calDefault.get(Calendar.MONTH), calDefault.get(Calendar.DAY_OF_MONTH));
        dlg.show(getActivity().getFragmentManager(), "datepickerdlg");

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri, Intent intent);
    }

}
