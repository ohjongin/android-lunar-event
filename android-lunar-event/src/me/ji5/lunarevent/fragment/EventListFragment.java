package me.ji5.lunarevent.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
    protected Handler mHandler = new Handler();
    protected int mRetryCount = 0;
    protected int mSelectedPosition = -1;

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

        mHandler.postDelayed(mQueryRunnable, ParseUtil.isAuthenticated() ? 10 : 200);
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

        registerForContextMenu(this.getListView());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
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
                onAddNew(event);
                consumed = true;
                break;

            case R.id.action_delete:
                mSelectedPosition = info.position;
                event = getListAdapter().getItem(info.position);
                getListAdapter().remove(event);
                sort();
                ParseObject po = ParseUtil.getParseObject(event);
                po.deleteInBackground();
                Toast.makeText(getBaseContext(), "이벤트가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
                onAddNew(null);
                break;
        }
    }

    protected void onAddNew(GoogleEvent event) {
        Intent intent = new Intent(getBaseContext(), NewEventActivity.class);
        if (event != null) {
            intent.putExtra("event", event);
        }
        startActivityForResult(intent, 0);
    }
    
    protected void onActionSort() {
        CharSequence[] array = {"제목순", "생일순", "다가오는 생일순"};

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

    protected Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            if (DEBUG_LOG) Log.d("Runnable count: " + mRetryCount);
            if (mRetryCount > 10) return;

            if (!ParseUtil.isAuthenticated()) {
                mRetryCount++;
                mHandler.postDelayed(mQueryRunnable, 100);
                return;
            }

            if (DEBUG_LOG) Log.d("ParseUtil.isAuthenticated() return TRUE!!, " + mRetryCount);

            ParseQuery<ParseObject> query = ParseQuery.getQuery(GoogleEvent.PARSE_CLASSNAME);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> eventList, ParseException e) {
                    if (e == null) {
                        for(ParseObject po : eventList) {
                            getListAdapter().add(ParseUtil.getGoogleEvent(po));
                        }
                        sort();
                    } else {
                        Log.e("score", "Error: " + e.getMessage());
                    }
                    getView().findViewById(R.id.progressbar).setVisibility(View.GONE);
                    getView().findViewById(R.id.btn_add_new).setVisibility(View.VISIBLE);
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
