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

import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.CollectedLeaf;

/**
 * TODO: comment this.
 * <p/>
 * Created by Arthur Jacobs on 08/09/2014.
 */
public class LeafletRecognitionRequest {

    private static final String LOG_TAG = "RECOGNITION_REQUEST";

    private Context mContext;

    private boolean mSynchronous;

    private DatabaseHelper mDbHelper;

    private CollectedLeaf mCollectedLeaf;

    public LeafletRecognitionRequest(Context context, CollectedLeaf collectedLeaf) {
        setContext(context);
        setSynchronous(false);
        setCollectedLeaf(collectedLeaf);
    }

    public LeafletRecognitionRequest(Context context, CollectedLeaf collectedLeaf, boolean synchronous) {
        setContext(context);
        setSynchronous(synchronous);
        setCollectedLeaf(collectedLeaf);
    }

    public void loadRecognitionResult() {
        JsonHttpResponseHandler handler = getJsonHttpResponseHandler();

        RequestParams params = new RequestParams();
        params.put("fmt", "json");
        if (isSynchronous()) {
            LeafletSyncRestClient.get("/" + getCollectedLeaf().getLeafID() + "/results/", params, handler);
        } else {
            LeafletAsyncRestClient.get("/" + getCollectedLeaf().getLeafID() + "/results/", params, handler);
        }
    }

    private JsonHttpResponseHandler getJsonHttpResponseHandler() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    parseResult(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {
                Log.e(LOG_TAG, "Could not download image. Error: " + error.getMessage());
            }
        };
    }

    private void parseResult(JSONObject result) throws JSONException, SQLException {
        JSONArray matches = result.getJSONArray("matches");
        for (int i = 0; i < matches.length(); i++) {
            

        }
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public CollectedLeaf getCollectedLeaf() {
        return mCollectedLeaf;
    }

    public void setCollectedLeaf(CollectedLeaf mCollectedLeaf) {
        this.mCollectedLeaf = mCollectedLeaf;
    }

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
        }
        return mDbHelper;
    }

    public boolean isSynchronous() {
        return mSynchronous;
    }

    public void setSynchronous(boolean mSynchronous) {
        this.mSynchronous = mSynchronous;
    }
}
