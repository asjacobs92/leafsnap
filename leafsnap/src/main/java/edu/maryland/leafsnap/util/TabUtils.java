package edu.maryland.leafsnap.util;

import android.support.v7.app.ActionBar.Tab;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.activity.MainActivity;

/**
 * Utils class to handle resources of each {@link Tab} in the {@link MainActivity}, given the position of the
 * {@link Tab}.
 *
 * @author Arthur Jacobs
 */
public class TabUtils {

    public static final int TAB_COUNT = 5;

    private static final int[] tabsTitlesIds = {R.string.title_home_tab,
            R.string.title_browse_tab,
            R.string.title_collection_tab,
            R.string.title_options_tab,
            R.string.title_camera_tab};

    private static final int[] tabsIconsIds = {
            R.drawable.home_tab,
            R.drawable.browse_tab,
            R.drawable.collection_tab,
            R.drawable.options_tab,
            R.drawable.camera_tab};

    public static int getTabTitleId(int position) {
        return tabsTitlesIds[position];
    }

    public static int getTabIconId(int position) {
        return tabsIconsIds[position];
    }
}
