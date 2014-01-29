package me.ji5.lunarevent.fragment;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;

import me.ji5.data.GoogleEvent;
import me.ji5.lunarevent.R;
import me.ji5.lunarevent.adapter.ScheduleListAdapter;
import me.ji5.utils.CalendarContentResolver;
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

            CalendarContentResolver ccr = new CalendarContentResolver(getBaseContext());

            mEvent.calcDate();
            long start = (new Date()).getTime();
            long end = mEvent.mComingBirthLunar + DateUtils.YEAR_IN_MILLIS * 5;
            String selection = "((" + CalendarContract.Events.DTSTART + " >= " + start + ") AND (" + CalendarContract.Events.DTEND + " <= " + end + ") AND (" + CalendarContract.Events.TITLE + "='"  + mEvent.mTitle.trim() + "'))";

            eventList = ccr.getEventList(selection);
            if (DEBUG_LOG) Log.e("event.mComingBirthLunar: " + MiscUtil.getDateString(null, mEvent.mComingBirthLunar) + ", " + mEvent.mComingBirthLunar);
        }

        setListAdapter(new ScheduleListAdapter(getBaseContext(), R.layout.fragment_event_list_row, new ArrayList<GoogleEvent>(), mEvent));
        getListAdapter().addAll(eventList);

        registerForContextMenu(this.getListView());

        if (getListAdapter().getCount() < 1) {
            getView().findViewById(R.id.progressbar).setVisibility(View.GONE);
        }
        getListAdapter().notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
