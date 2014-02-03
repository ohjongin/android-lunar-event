package me.ji5.lunarevent.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import me.ji5.data.GoogleEvent;
import me.ji5.lunarevent.R;
import me.ji5.lunarevent.adapter.ScheduleListAdapter;
import me.ji5.utils.Log;
import me.ji5.utils.MiscUtil;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ScheduleListFragment extends ListFragment {
    protected final static boolean DEBUG_LOG = false;
    protected final static int CALENDAR_RANGE_YEAR = 20; // 구글캘린더 검색 범위

    protected OnFragmentInteractionListener mListener;
    protected GoogleEvent mEvent = new GoogleEvent();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScheduleListFragment.
     */
    public static ScheduleListFragment newInstance() {
        ScheduleListFragment fragment = new ScheduleListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public ScheduleListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()  + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getActivity().getIntent() != null && getActivity().getIntent().hasExtra("event")) {
            mEvent = getActivity().getIntent().getParcelableExtra("event");
            mEvent.calcDate();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<GoogleEvent> eventList = new ArrayList<GoogleEvent>();
        if (mEvent != null) {
            getActivity().setTitle(mEvent.mTitle);
            if (DEBUG_LOG) Log.e("event.mComingBirthLunar: " + MiscUtil.getDateString(null, mEvent.mComingBirthLunar) + ", " + mEvent.mComingBirthLunar);
        }

        Collections.sort(eventList, GoogleEvent.compareBirth);

        Calendar cal_today = Calendar.getInstance();
        cal_today.setTime(new Date());

        int year = cal_today.get(Calendar.YEAR);
        int i = 0;
        do {
            GoogleEvent event = mEvent.clone();
            event.calcDate(year + i);
            if (DEBUG_LOG) Log.e("[" + i + "]: " + event.toString());

            if (event.mComingBirthLunar < System.currentTimeMillis()) {
                i++;
                continue;
            }

            event.mId = event.findEventId(getBaseContext());
            eventList.add(event);
            i++;
        } while (i < CALENDAR_RANGE_YEAR);

        setListAdapter(new ScheduleListAdapter(getBaseContext(), R.layout.fragment_event_list_row, eventList));

        registerForContextMenu(this.getListView());

        if (getListAdapter().getCount() < 1) {
            getView().findViewById(R.id.progressbar).setVisibility(View.GONE);
        }
        getListAdapter().notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        for(int i = 0; i < getListAdapter().getCount(); i++) {
            getListAdapter().getItem(i).findEventId(getBaseContext());
        }
        getListAdapter().notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView listView, View view, final int position, long id) {
        final GoogleEvent event = getListAdapter().getItem(position);
        long event_id = event.findEventId(getBaseContext());

        if (event_id < 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
            builder.setTitle(getBaseContext().getString(R.string.dlg_title_ask_add))
                    .setMessage(getBaseContext().getString(R.string.dlg_msg_no_registered_event))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            event.mId = event.addToCalendar(getBaseContext());
                            getListAdapter().notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // Android 2.2+
            intent.setData(Uri.parse("content://com.android.calendar/events/" + String.valueOf(event_id)));
            // Android 2.1 and below.
            // intent.setData(Uri.parse("content://calendar/events/" + String.valueOf(calendarEventID)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public ScheduleListAdapter getListAdapter() {
        return (ScheduleListAdapter)super.getListAdapter();
    }

    protected Context getBaseContext() {
        return getActivity();
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
        public void onFragmentInteraction(Uri uri);
    }

}
