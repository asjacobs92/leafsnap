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
import java.util.Date;
import java.util.List;

import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.model.LeafletUrl;
import edu.maryland.leafsnap.model.Species;


/**
 * TODO: comment this.
 * <p/>
 * Created by Arthur Jacobs on 03/07/2014.
 */
public class LeafletUserCollectionRequest {

    private boolean mFinished;

    private Context mContext;
    private DatabaseHelper mDbHelper;

    public LeafletUserCollectionRequest(Context context) {
        setContext(context);
    }

    public void updateUserCollectionSyncStatus(String username) {
        RequestParams params = new RequestParams();
        params.put("fmt", "json");
        LeafletAsyncRestClient.get("/users/" + username + "/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    parseResult(response);
                    setFinished(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                setFinished(true);
                throwable.printStackTrace();
            }
        });
    }

    private void parseResult(JSONObject result) throws JSONException, SQLException {
        Log.d("TAAAAG", result.toString());
        JSONArray images = result.getJSONArray("images");
        for (int i = 0; i < images.length(); i++) {
            JSONObject oneImage = images.getJSONObject(i);
            CollectedLeaf collectedLeaf = new CollectedLeaf();

            collectedLeaf.setSyncStatus(CollectedLeaf.SyncStatus.SAME);
            collectedLeaf.setLeafID(oneImage.getLong("id"));
            collectedLeaf.setCollectedDate(new Date(oneImage.getLong("phototime")));
            collectedLeaf.setLastModified(new Date(oneImage.getLong("lastmodified")));

            collectedLeaf = parseCollectedLeafAltitude(collectedLeaf, oneImage);
            collectedLeaf = parseCollectedLeafLatitude(collectedLeaf, oneImage);
            collectedLeaf = parseCollectedLeafLongitude(collectedLeaf, oneImage);

            collectedLeaf = parseSelectedSpeciesRel(oneImage, collectedLeaf);
            collectedLeaf = parseOriginalImageUrl(collectedLeaf);
            collectedLeaf = parseSegmentedImageUrl(collectedLeaf);

            getDbHelper().getCollectedLeafDao().create(collectedLeaf);
        }

    }

    private CollectedLeaf parseSelectedSpeciesRel(JSONObject oneImage, CollectedLeaf collectedLeaf) throws SQLException {
        try {
            String scientificName = oneImage.getString("sciname");
            List<Species> speciesList = getDbHelper().getSpeciesDao().queryForEq("scientificName", scientificName);
            if (speciesList != null && !speciesList.isEmpty()) {
                collectedLeaf.setSelectedSpeciesRel(speciesList.get(0));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return collectedLeaf;
    }

    private CollectedLeaf parseSegmentedImageUrl(CollectedLeaf collectedLeaf) throws SQLException {
        LeafletUrl segmentedLeafletUrl = new LeafletUrl();
        segmentedLeafletUrl.setType("SegmentedImage");
        segmentedLeafletUrl.setDataSource("SegmentedImage");
        segmentedLeafletUrl.setRawURL("/" + collectedLeaf.getLeafID() + "/segmented.png");
        segmentedLeafletUrl.setThumbnailLocation("Server");
        segmentedLeafletUrl.setHiResImageLocation("Server");
        getDbHelper().getLeafletUrlDao().create(segmentedLeafletUrl);
        collectedLeaf.setSegmentedImageURL(segmentedLeafletUrl);

        LeafletImageLoader imageLoader = new LeafletImageLoader(getContext(), segmentedLeafletUrl);
        imageLoader.loadImage();

        return collectedLeaf;
    }

    private CollectedLeaf parseOriginalImageUrl(CollectedLeaf collectedLeaf) throws SQLException {
        LeafletUrl originalLeafletUrl = new LeafletUrl();
        originalLeafletUrl.setType("OriginalImage");
        originalLeafletUrl.setDataSource("OriginalImage");
        originalLeafletUrl.setRawURL("/" + collectedLeaf.getLeafID() + "/original.jpg");
        originalLeafletUrl.setThumbnailLocation("Server");
        originalLeafletUrl.setHiResImageLocation("Server");
        getDbHelper().getLeafletUrlDao().create(originalLeafletUrl);
        collectedLeaf.setOriginalImageURL(originalLeafletUrl);

        LeafletImageLoader imageLoader = new LeafletImageLoader(getContext(), originalLeafletUrl);
        imageLoader.loadImage();

        return collectedLeaf;
    }

    private CollectedLeaf parseCollectedLeafAltitude(CollectedLeaf collectedLeaf, JSONObject oneImage) {
        try {
            collectedLeaf.setAltitude(oneImage.getString("altitude"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return collectedLeaf;
    }

    private CollectedLeaf parseCollectedLeafLongitude(CollectedLeaf collectedLeaf, JSONObject oneImage) {
        try {
            collectedLeaf.setLongitude(oneImage.getString("longitude"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return collectedLeaf;
    }

    private CollectedLeaf parseCollectedLeafLatitude(CollectedLeaf collectedLeaf, JSONObject oneImage) {
        try {
            collectedLeaf.setLatitude(oneImage.getString("latitude"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return collectedLeaf;
    }

    private Context getContext() {
        return mContext;
    }

    private void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public boolean isFinished() {
        return mFinished;
    }

    public void setFinished(boolean finished) {
        this.mFinished = finished;
    }

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
        }
        return mDbHelper;
    }
}
