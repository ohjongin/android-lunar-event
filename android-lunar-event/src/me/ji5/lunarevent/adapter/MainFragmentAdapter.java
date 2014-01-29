package me.ji5.lunarevent.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.ji5.lunarevent.fragment.EventListFragment;
import me.ji5.utils.Log;

/**
 * Created by ohjongin on 14. 1. 10.
 */
public class MainFragmentAdapter extends FragmentPagerAdapter {
    protected Fragment mFragments[] = new Fragment[1];

    public MainFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }

    @Override
    public Fragment getItem(int position) {
        if (position < 0 || position >= mFragments.length) {
            Log.e("Invalid index(" + position + ")!!!");
            return null;
        }

        Fragment fragment = mFragments[position];
        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = EventListFragment.newInstance();
                    break;
                default:
                    break;
            }
            mFragments[position] = fragment;
        }

        return fragment;
    }

    public Fragment getFragment(int position) {
        return (position >= 0 && position < mFragments.length) ? mFragments[position] : null;
    }
}
