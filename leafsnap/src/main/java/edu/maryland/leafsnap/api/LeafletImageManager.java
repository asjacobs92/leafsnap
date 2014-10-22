package edu.maryland.leafsnap.api;

import android.content.Context;
import android.os.AsyncTask;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;

import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.LeafletUrl;

/**
 * TODO
 * Created by Arthur Jacobs on 09/07/2014.
 */
public class LeafletImageManager {

    private Context mContext;
    private DatabaseHelper mDbHelper;

    public LeafletImageManager(Context context) {
        setContext(context);
    }

    public void consolidateDatabase() {
        new ProgressTask().execute();
    }

    private Context getContext() {
        return mContext;
    }

    private void setContext(Context mContext) {
        this.mContext = mContext;
    }

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
        }
        return mDbHelper;
    }

    public void close() {
        if (mDbHelper != null) {
            OpenHelperManager.releaseHelper();
            mDbHelper = null;
        }
    }

    private class ProgressTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            ArrayList<LeafletUrl> leafletUrls;
            try {
                leafletUrls = (ArrayList<LeafletUrl>) getDbHelper().getLeafletUrlDao().queryForAll();
                for (LeafletUrl leafletUrl : leafletUrls) {
                    LeafletImageLoader imageLoader = new LeafletImageLoader(getContext(), leafletUrl, true);
                    imageLoader.loadImage();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
