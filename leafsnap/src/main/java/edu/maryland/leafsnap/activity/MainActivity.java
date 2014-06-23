package edu.maryland.leafsnap.activity;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.adapter.SectionsPagerAdapter;
import edu.maryland.leafsnap.data.LeafletDatabaseContentRequest;
import edu.maryland.leafsnap.fragment.BrowseFragment;
import edu.maryland.leafsnap.fragment.CameraFragment;
import edu.maryland.leafsnap.fragment.CollectionFragment;
import edu.maryland.leafsnap.fragment.HomeFragment;
import edu.maryland.leafsnap.fragment.OptionsFragment;
import edu.maryland.leafsnap.util.TabUtils;

/**
 * Main activity of application, containing the {@link ActionBar}, the {@link Tab}s and the {@link ViewPager}.
 *
 * @author Arthur Jacobs
 */
public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * Context of the {@link MainActivity}, and therefore, of the Application. This is static and accessible from
     * anywhere in the code. However, must be set OnCreate to avoid NullPointerExecptions.
     */
    private static Context mContext;
    private final Fragment[] mFragments = {new HomeFragment(), new BrowseFragment(), new CollectionFragment(),
            new OptionsFragment(), new CameraFragment()};
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * Developer option to populate database.
     */
    private boolean populateDatabase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (populateDatabase) {
            LeafletDatabaseContentRequest dbRequest = new LeafletDatabaseContentRequest(this);
            dbRequest.fetchWholeDatabaseFromServer();
        }

        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        setupPageViewer();

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setCustomView(getTabCustomView(i)).setTabListener(this));
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

    private View getTabCustomView(int i) {
        View tabView = getLayoutInflater().inflate(R.layout.actionbar_tab, null);
        TextView tabText = (TextView) tabView.findViewById(R.id.tab_text);
        if (tabText != null) {
            tabText.setText(TabUtils.getTabTitleId(i));
            tabText.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(TabUtils.getTabIconId(i)), null, null);
        }
        return tabView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem actionHelp = menu.findItem(R.id.action_help);
        actionHelp.getIcon().setColorFilter(getResources().getColor(R.color.leafsnap_green), PorterDuff.Mode.MULTIPLY);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (item.getItemId() == R.id.action_help) && super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        View tabView = tab.getCustomView();
        TextView tabText = (TextView) tabView.findViewById(R.id.tab_text);
        tabText.setTextColor(getResources().getColor(R.color.leafsnap_green));
        Drawable tabIcon = getResources().getDrawable(TabUtils.getTabIconId(tab.getPosition()));
        tabIcon.setColorFilter(getResources().getColor(R.color.leafsnap_green), PorterDuff.Mode.MULTIPLY);
        tabText.setCompoundDrawablesWithIntrinsicBounds(null, tabIcon, null, null);

        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        View tabView = tab.getCustomView();
        TextView tabText = (TextView) tabView.findViewById(R.id.tab_text);
        tabText.setTextColor(getResources().getColor(R.color.leafsnap_grey));
        Drawable tabIcon = getResources().getDrawable(TabUtils.getTabIconId(tab.getPosition()));
        tabIcon.setColorFilter(getResources().getColor(R.color.leafsnap_grey), PorterDuff.Mode.MULTIPLY);
        tabText.setCompoundDrawablesWithIntrinsicBounds(null, tabIcon, null, null);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // TODO
    }
}
