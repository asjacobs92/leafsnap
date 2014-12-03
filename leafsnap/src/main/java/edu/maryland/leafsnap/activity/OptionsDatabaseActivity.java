package edu.maryland.leafsnap.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.api.LeafletDatabaseContentRequest;
import edu.maryland.leafsnap.data.DatabaseHelper;

public class OptionsDatabaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_options_database);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        new CountSpeciesTask().execute();
    }


    public void onUpdateSpeciesButtonClick(View v) {
        new UpdateDatabaseTask().execute();
    }

    public class CountSpeciesTask extends AsyncTask<Void, Void, Long> {

        private DatabaseHelper mDbHelper;

        @Override
        protected Long doInBackground(Void... params) {
            Long count = -1L;
            try {
                count = getDbHelper().getSpeciesDao().countOf();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return count;
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result >= 0) {
                TextView speciesCount = (TextView) findViewById(R.id.species_count);
                speciesCount.setText(String.valueOf(result));
            }

            if (mDbHelper != null) {
                OpenHelperManager.releaseHelper();
                mDbHelper = null;
            }
        }

        private DatabaseHelper getDbHelper() {
            if (mDbHelper == null) {
                mDbHelper = OpenHelperManager.getHelper(getBaseContext(), DatabaseHelper.class);
            }
            return mDbHelper;
        }
    }

    public class UpdateDatabaseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            LeafletDatabaseContentRequest databaseRequest =
                    new LeafletDatabaseContentRequest(getBaseContext());
            databaseRequest.fetchWholeDatabaseFromServer();
            while (!databaseRequest.isFinished()) {
                SystemClock.sleep(100);
            }

            databaseRequest.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setSupportProgressBarIndeterminateVisibility(false);
            new CountSpeciesTask().execute();
        }
    }
}
