package me.ji5.lunarevent;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import me.ji5.lunarevent.adapter.ScheduleFragmentAdapter;
import me.ji5.lunarevent.fragment.ScheduleListFragment;

public class ScheduleListActivity extends FragmentActivity implements ScheduleListFragment.OnFragmentInteractionListener {
    protected final static boolean DEBUG_LOG = false;

    protected ViewPager mPager;
    protected ScheduleFragmentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed = false;
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Do Activity menu item stuff here
                consumed = true;
                break;
            case R.id.action_add:
                // Not implemented here
                break;
            default:
                break;
        }

        return consumed;
    }

    protected void initViews() {
        mAdapter = new ScheduleFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(0);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
