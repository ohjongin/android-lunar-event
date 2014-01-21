package me.ji5.lunarevent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import me.ji5.lunarevent.fragment.NewEventFragment;

/**
 * Created by ohjongin on 14. 1. 17.
 */
public class NewEventActivity extends FragmentActivity implements NewEventFragment.OnFragmentInteractionListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
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

    @Override
    public void onFragmentInteraction(Uri uri, Intent intent) {
        setResult(0, intent);
        finish();
    }
}
