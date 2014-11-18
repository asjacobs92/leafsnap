package edu.maryland.leafsnap.api;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.table.TableUtils;
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
import edu.maryland.leafsnap.util.SessionManager;


/**
 * TODO: comment this.
 * <p/>
 * Created by Arthur Jacobs on 03/07/2014.
 */
public class LeafletUserCollectionRequest {

    private Context mContext;
    private boolean mFinished;
    private DatabaseHelper mDbHelper;
    private SessionManager mSessionManager;

    public LeafletUserCollectionRequest(Context context) {
        setFinished(false);
        mContext = context;
    }

    public void updateUserCollectionSyncStatus() {
        deleteLocalCollection();
        RequestParams params = new RequestParams();
        String username = getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME);
        params.put("fmt", "json");
        LeafletRestClient.get("/users/" + username + "/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    parseResult(response);
                    setFinished(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                setFinished(true);
                throwable.printStackTrace();
            }
        }, false);
    }

    private void deleteLocalCollection() {
        try {
            TableUtils.clearTable(getDbHelper().getConnectionSource(), CollectedLeaf.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void parseResult(JSONObject result) throws JSONException, SQLException {
        JSONArray images = result.getJSONArray("images");
        for (int i = 0; i < images.length(); i++) {
            JSONObject oneImage = images.getJSONObject(i);
            CollectedLeaf collectedLeaf = new CollectedLeaf();

            collectedLeaf.setSyncStatus(CollectedLeaf.SyncStatus.SAME);
            collectedLeaf.setLeafID(oneImage.getLong("id"));
            collectedLeaf.setCollectedDate(new Date(oneImage.getLong("phototime") * 1000));
            collectedLeaf.setLastModified(new Date(oneImage.getLong("lastmodified") * 1000));

            collectedLeaf = parseCollectedLeafAltitude(collectedLeaf, oneImage);
            collectedLeaf = parseCollectedLeafLatitude(collectedLeaf, oneImage);
            collectedLeaf = parseCollectedLeafLongitude(collectedLeaf, oneImage);

            collectedLeaf = parseSelectedSpeciesRel(collectedLeaf, oneImage);
            collectedLeaf = parseOriginalImageUrl(collectedLeaf);
            collectedLeaf = parseSegmentedImageUrl(collectedLeaf);

            getDbHelper().getCollectedLeafDao().create(collectedLeaf);

            LeafletRecognitionRequest recognitionRequest =
                    new LeafletRecognitionRequest(mContext, collectedLeaf);
            recognitionRequest.loadRecognitionResult();
        }
    }

    private CollectedLeaf parseSelectedSpeciesRel(CollectedLeaf collectedLeaf, JSONObject oneImage) throws SQLException {
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

        LeafletImageLoader imageLoader = new LeafletImageLoader(mContext, segmentedLeafletUrl);
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

        LeafletImageLoader imageLoader = new LeafletImageLoader(mContext, originalLeafletUrl);
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

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class);
        }
        return mDbHelper;
    }

    private SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(mContext);
        }
        return mSessionManager;
    }

    public void close() {
        if (mDbHelper != null) {
            OpenHelperManager.releaseHelper();
            mDbHelper = null;
        }
    }

    public boolean isFinished() {
        return mFinished;
    }

    public void setFinished(boolean mFinished) {
        this.mFinished = mFinished;
    }
}
