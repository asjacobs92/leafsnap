package edu.maryland.leafsnap.api;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;

import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.model.RankedSpecies;
import edu.maryland.leafsnap.model.Species;

/**
 * TODO: comment this.
 * <p/>
 * Created by Arthur Jacobs on 08/09/2014.
 */
public class LeafletRecognitionRequest {

    private static final String LOG_TAG = "RECOGNITION_REQUEST";

    private Context mContext;
    private DatabaseHelper mDbHelper;
    private CollectedLeaf mCollectedLeaf;

    public LeafletRecognitionRequest(Context context, CollectedLeaf collectedLeaf) {
        mContext = context;
        mCollectedLeaf = collectedLeaf;
    }

    public void loadRecognitionResult() {
        RequestParams params = new RequestParams();
        params.put("fmt", "json");
        LeafletRestClient.get("/" + mCollectedLeaf.getLeafID() + "/results/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    parseResult(response);
                    mCollectedLeaf.setUploaded(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {
                Log.e(LOG_TAG, "Recognition request failed. Error: " + error.getMessage());
            }
        }, false);
    }

    private void parseResult(JSONObject result) throws JSONException, SQLException {
        JSONArray matches = result.getJSONArray("matches");
        if (matches.length() != 0) {
            for (int i = 0; i < matches.length(); i++) {
                JSONObject oneMatch = matches.getJSONObject(i);

                ArrayList<Species> queryResults = (ArrayList<Species>) getDbHelper().
                        getSpeciesDao().queryForEq("scientificName", oneMatch.get("sciname"));
                if (!queryResults.isEmpty()) {
                    RankedSpecies rankedSpecies = new RankedSpecies(i+1, queryResults.get(0));
                    rankedSpecies.setAssociatedCollection(mCollectedLeaf);
                    getDbHelper().getRankedSpeciesDao().create(rankedSpecies);
                }
            }
        } else {
            Log.e(LOG_TAG, "Recognition request failed. Segmentation failed.");
        }
    }

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class);
        }
        return mDbHelper;
    }

    public void close() {
        if (mDbHelper != null) {
            OpenHelperManager.releaseHelper();
            mDbHelper = null;
        }
    }
}
