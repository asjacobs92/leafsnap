package edu.maryland.leafsnap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.adapter.SectionsPagerAdapter;
import edu.maryland.leafsnap.fragment.BrowseFragment;
import edu.maryland.leafsnap.fragment.CollectionFragment;
import edu.maryland.leafsnap.fragment.HomeFragment;
import edu.maryland.leafsnap.fragment.OptionsFragment;
import edu.maryland.leafsnap.util.MediaUtils;
import edu.maryland.leafsnap.util.TabUtils;

/**
 * Main activity of application, containing the {@link ActionBar}, the {@link Tab}s and the {@link ViewPager}.
 *
 * @author Arthur Jacobs
 */
public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private static final Fragment[] mFragments = {
            new HomeFragment(),
            new BrowseFragment(),
            new CollectionFragment(),
            new OptionsFragment()
    };

    private ArrayList<Tab> mTabs;

    private ViewPager mViewPager;
    private ActionBar mActionBar;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TabUtils.setUpdateCurrentTab(true);
        mActionBar.selectTab(mTabs.get(TabUtils.getCurrentTabPosition()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        TabUtils.setUpdateCurrentTab(false);
    }

    private void setupActionBar() {
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        setupPageViewer();

        mTabs = new ArrayList<Tab>();
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            Tab t = mActionBar.newTab().setText(TabUtils.getTabTitleId(i)).setTabListener(this);
            mTabs.add(t);
            mActionBar.addTab(t);
        }
    }

    private void setupPageViewer() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), mFragments);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem actionSnapIt = menu.findItem(R.id.action_snap_it);
        MenuItemCompat.getActionView(actionSnapIt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(actionSnapIt);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_snap_it:
                if (MediaUtils.isExternalStorageWritable()) {
                    Intent intent = new Intent(this, CameraActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "External Storage not available.",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        TabUtils.setCurrentTabPosition(tab.getPosition());
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
}
