package edu.maryland.leafsnap.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBar.Tab;

import edu.maryland.leafsnap.util.TabUtils;

/**
 * {@link PagerAdapter} that returns a different {@link Fragment} for the {@link edu.maryland.leafsnap.activity.MainActivity} to render, depending on
 * which {@link Tab} is selected.
 *
 * @author Arthur Jacobs
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] mFragments;
    private Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm, Fragment[] fragments) {
        super(fm);
        this.mContext = context;
        this.mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return getPageFragment(position);
    }

    @Override
    public int getCount() {
        return TabUtils.TAB_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TabUtils.getTabTitleId(position));
    }

    public Fragment getPageFragment(int position) {
        return mFragments[position];
    }
}
