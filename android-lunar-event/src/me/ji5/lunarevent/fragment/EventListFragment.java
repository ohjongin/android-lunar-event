package me.ji5.lunarevent.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import me.ji5.data.GoogleEvent;
import me.ji5.lunarevent.NewEventActivity;
import me.ji5.lunarevent.R;
import me.ji5.lunarevent.adapter.EventListAdapter;
import me.ji5.utils.Log;
import me.ji5.utils.ParseUtil;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EventListFragment extends ListFragment implements View.OnClickListener {
    protected final static boolean DEBUG_LOG = true;
    protected OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EventListFragment.
     */
    public static EventListFragment newInstance() {
        EventListFragment fragment = new EventListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public EventListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setListAdapter(new EventListAdapter(getBaseContext(), R.layout.fragment_event_list_row, new ArrayList<GoogleEvent>()));

        ParseQuery<ParseObject> query = ParseQuery.getQuery(GoogleEvent.PARSE_CLASSNAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> eventList, ParseException e) {
                if (e == null) {
                    for(ParseObject po : eventList) {
                        getListAdapter().add(ParseUtil.getGoogleEvent(po));
                    }
                } else {
                    Log.e("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        view.findViewById(R.id.btn_add_new).setOnClickListener(this);

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
                onAddNew();
                consumed = true;
                break;
            default:
                break;
        }

        return consumed;
    }

    /**
     * This method is called when the sending activity has finished, with the
     * result it supplied.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 0:
                if (DEBUG_LOG) Log.e("onActivityResult():");
                if (data != null && data.hasExtra("event")) {
                    GoogleEvent event = data.getParcelableExtra("event");
                    if (DEBUG_LOG)  Log.e("  event :" + event.toString());
                    getListAdapter().add(event);
                    getListAdapter().notifyDataSetChanged();

                    final ParseObject po = ParseUtil.getParseObject(event);
                    po.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                if (DEBUG_LOG) Log.d("saveInBackground success: " + po.get("title"));
                            } else {
                                Log.e(e.getMessage());
                            }
                        }
                    });
                }
                break;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private OnDateSetListener mDateSetListener = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog dlg, int year, int monthOfYear, int dayOfMonth) {

        }
    };

    @Override
    public EventListAdapter getListAdapter() {
        return (EventListAdapter)super.getListAdapter();
    }
 
    protected Context getBaseContext() {
        return getActivity();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_new:
                onAddNew();
                break;
        }
    }

    protected void onAddNew() {
        Intent intent = new Intent(getBaseContext(), NewEventActivity.class);
        startActivityForResult(intent, 0);
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
