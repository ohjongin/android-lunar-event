package me.ji5.lunarevent.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.ji5.data.GoogleEvent;
import me.ji5.lunarevent.NewEventActivity;
import me.ji5.lunarevent.R;
import me.ji5.lunarevent.ScheduleListActivity;
import me.ji5.lunarevent.adapter.EventListAdapter;
import me.ji5.lunarevent.provider.EventProvider;
import me.ji5.utils.CalendarContentResolver;
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
    protected final static boolean DEBUG_LOG = false;
    protected OnFragmentInteractionListener mListener;
    protected int mRetryCount = 0;
    protected int mSelectedPosition = -1;
    protected ArrayList<GoogleEvent> mEventList = new ArrayList<GoogleEvent>();
    protected Animation animSlideDownIn, animSlideUpOut;
    protected HashMap<String, ArrayList<GoogleEvent>> mEventMapCache = new HashMap<String, ArrayList<GoogleEvent>>();

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
        setListAdapter(new EventListAdapter(getBaseContext(), R.layout.fragment_event_list_row, new ArrayList<GoogleEvent>()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        view.findViewById(R.id.btn_add_new).setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHandler.postDelayed(mQueryRunnable, ParseUtil.isAuthenticated() ? 10 : 200);

        registerForContextMenu(this.getListView());

        ContentResolver cr = getBaseContext().getContentResolver();
        Cursor c = cr.query(EventProvider.EVENT_URI, null, null, null, null);

        if (c == null || c.getCount() < 1) {
            return;
        } else {
            getView().findViewById(R.id.progressbar).setVisibility(View.GONE);
            getView().findViewById(R.id.btn_add_new).setVisibility(View.VISIBLE);

            c.moveToFirst();
        }

        getListAdapter().clear();
        do {
            GoogleEvent event = GoogleEvent.getInstance(c);
            boolean found = existInCache(event);
            if (!found) {
                getListAdapter().add(event);
                addToCache(event);
            }
            if (DEBUG_LOG) Log.e(event.toString());
        } while (c.moveToNext());

        sort();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        final GoogleEvent event = getListAdapter().getItem(position);
        CalendarContentResolver ccr = new CalendarContentResolver(getBaseContext());

        long start = event.mComingBirthLunar - DateUtils.DAY_IN_MILLIS;
        long end = event.mComingBirthLunar + DateUtils.YEAR_IN_MILLIS * 10;
        String selection = "((" + CalendarContract.Events.DTSTART + " >= " + start + ") AND (" + CalendarContract.Events.DTEND + " <= " + end + ") AND (" + CalendarContract.Events.TITLE + "='"  + event.mTitle.trim() + "'))";

        ArrayList<GoogleEvent> eventList = ccr.getEventList(selection);
        if (eventList == null || eventList.size() < 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
            builder.setTitle(getBaseContext().getString(R.string.dlg_title_ask_add))
                    .setMessage(getBaseContext().getString(R.string.dlg_msg_no_registered_event))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            event.addToCalendar(getBaseContext());
                            dialog.dismiss();

                            Intent intent = new Intent(getBaseContext(), ScheduleListActivity.class);
                            intent.putExtra("event", event);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            Intent intent = new Intent(getBaseContext(), ScheduleListActivity.class);
            intent.putExtra("event", getListAdapter().getItem(position));
            startActivity(intent);
        }
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
                onAddNew(null);
                consumed = true;
                break;
            case R.id.action_sort:
                onActionSort();
                consumed = true;
                break;
            default:
                break;
        }

        return consumed;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.event_list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean consumed = false;
        GoogleEvent event = null;

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_edit:
                mSelectedPosition = info.position;
                event = getListAdapter().getItem(info.position);
                event.findEventId(getBaseContext());
                onAddNew(event);
                consumed = true;
                break;

            case R.id.action_addto_calendar:
                event = getListAdapter().getItem(info.position);
                GoogleEvent.addToCalendar(getBaseContext(), event);
                consumed = true;
                break;

            case R.id.action_delete:
                mSelectedPosition = info.position;
                event = getListAdapter().getItem(info.position);
                getListAdapter().remove(event);
                sort();
                ParseObject po = ParseUtil.getParseObject(event);
                po.deleteInBackground();
                Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();
                consumed = true;
                break;

            default:
                super.onContextItemSelected(item);
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
                if (DEBUG_LOG) Log.d("onActivityResult():");
                if (data != null && data.hasExtra("event")) {
                    final GoogleEvent event = data.getParcelableExtra("event");
                    if (DEBUG_LOG) Log.d("Event :" + event.toString());
                    if (TextUtils.isEmpty(event.mParseObjectId)) {
                        getListAdapter().add(event);
                        sort();

                        final ParseObject po = ParseUtil.getParseObject(event);
                        po.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    if (DEBUG_LOG) Log.d("saveInBackground success: " + po.get("title"));
                                    int position = getListAdapter().getPosition(event);
                                    if (position >= 0) {
                                        getListAdapter().remove(event);
                                        event.mParseObjectId = po.getObjectId();
                                        getListAdapter().insert(event, position);
                                        sort();
                                    }
                                } else {
                                    Log.e(e.getMessage());
                                }
                            }
                        });
                    } else {
                        final ParseObject po = ParseUtil.getParseObject(event);
                        po.setObjectId(event.mParseObjectId);
                        po.saveInBackground();
                        if (mSelectedPosition >= 0 && mSelectedPosition < getListAdapter().getCount()) {
                            getListAdapter().remove(getListAdapter().getItem(mSelectedPosition));
                            getListAdapter().insert(event, mSelectedPosition);
                        }
                        sort();
                    }
                }
                break;
        }
        mSelectedPosition = -1;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

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
                onAddNew(null);
                break;
        }
    }

    protected void addToCache(GoogleEvent event) {
        if (event == null) return;

        ArrayList<GoogleEvent> event_list = mEventMapCache.get(event.mTitle);
        if (event_list != null) {
            boolean found = false;
            for(GoogleEvent ge : event_list) {
                if (ge.equals(event)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                event_list.add(event);
            }
        } else {
            event_list = new ArrayList<GoogleEvent>();
            event_list.add(event);
            mEventMapCache.put(event.mTitle, event_list);

        }
    }

    protected boolean existInCache(GoogleEvent event) {
        boolean exist = false;

        if (event == null) return false;

        ArrayList<GoogleEvent> event_list = mEventMapCache.get(event.mTitle);
        if (event_list != null) {
            for(GoogleEvent ge : event_list) {
                if (ge.equals(event)) {
                    exist = true;
                    break;
                }
            }
        }

        return exist;
    }

    protected void onAddNew(GoogleEvent event) {
        Intent intent = new Intent(getBaseContext(), NewEventActivity.class);
        if (event != null) {
            intent.putExtra("event", event);
        }
        startActivityForResult(intent, 0);
    }
    
    protected void onActionSort() {
        CharSequence[] array = {getString(R.string.sort_by_title), getString(R.string.sort_by_birth), getString(R.string.sort_by_recent)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
        builder.setTitle("정렬")
                .setSingleChoiceItems(array, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int index = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        getListAdapter().sort(index);
                        getActivity().getSharedPreferences("pref",0).edit().putInt("sort_type", index).commit();
                    }
                }).create().show();
    }

    protected void onUpdated() {
        ArrayList<GoogleEvent> event_list = (ArrayList<GoogleEvent>)mEventList.clone();
        for(GoogleEvent ge : event_list) {
            if (existInCache(ge)) {
                mEventList.remove(ge);
            } else {
                Uri uri = ge.insert(getBaseContext(), EventProvider.EVENT_URI);
                if (DEBUG_LOG) Log.d("Event inserted: " + uri.toString());
            }
        }

        if (mEventList.size() < 1) {
            return;
        }

        getView().findViewById(R.id.progressbar).setVisibility(View.GONE);
        getView().findViewById(R.id.btn_add_new).setVisibility(View.VISIBLE);

        animSlideDownIn = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_down_in);
        animSlideUpOut = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_up_out);

        animSlideDownIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animSlideUpOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getView().findViewById(R.id.layout_top_ticker).setVisibility(View.GONE);
                getListAdapter().clear();
                getListAdapter().addAll(mEventList);
                sort();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (getListAdapter().getCount() > 0) {
            getView().findViewById(R.id.layout_top_ticker).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.layout_top_ticker).startAnimation(animSlideDownIn);
            getView().findViewById(R.id.layout_top_ticker).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getView().findViewById(R.id.layout_top_ticker).startAnimation(animSlideUpOut);
                }
            });
        } else {
            getView().findViewById(R.id.layout_top_ticker).setVisibility(View.GONE);
            getListAdapter().clear();
            getListAdapter().addAll(mEventList);
            sort();
        }
    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    onUpdated();
                    break;
            }
        }
    };

    protected Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            if (DEBUG_LOG) Log.d("Runnable count: " + mRetryCount);
            if (mRetryCount > 20) {
                ParseUtil.loginParse(getBaseContext());
                return;
            } else if (mRetryCount > 30) {
                Toast.makeText(getBaseContext(), "사용자 인증에 실패했습니다.", Toast.LENGTH_LONG).show();
                getActivity().finish();
                return;
            }

            if (!ParseUtil.isAuthenticated()) {
                mRetryCount++;
                mHandler.postDelayed(mQueryRunnable, (mRetryCount < 5) ? 100 : 200);
                return;
            }

            if (DEBUG_LOG) Log.d("ParseUtil.isAuthenticated() return TRUE!!, " + mRetryCount);

            ParseQuery<ParseObject> query = ParseQuery.getQuery(GoogleEvent.PARSE_CLASSNAME);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> eventList, ParseException e) {
                    if (e == null) {
                        for(ParseObject po : eventList) {
                            GoogleEvent event = ParseUtil.getGoogleEvent(po);
                            mEventList.add(event);
                        }

                        mHandler.obtainMessage(0).sendToTarget();
                    } else {
                        Log.e("Error: " + e.getMessage());
                    }
                }
            });
        }
    };

    protected void sort() {
        int sort_type = getActivity().getSharedPreferences("pref",0).getInt("sort_type", 0);
        getListAdapter().sort(sort_type);
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
