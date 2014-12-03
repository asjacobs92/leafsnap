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

import java.io.File;
import java.sql.SQLException;

import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.model.LeafletUrl;
import edu.maryland.leafsnap.model.Species;
import edu.maryland.leafsnap.util.MediaUtils;
import edu.maryland.leafsnap.util.SessionManager;

/**
 * TODO
 * Created by asjacobs on 10/28/14.
 */
public class LeafletPhotoUploader {

    private static final String LOG_TAG = "PHOTO_UPLOADER";
    private final static String IMAGE_UPLOAD_DEFAULT_FAIL_MSG = "Operation failed," +
            " most likely due to lack of connectivity.";
    public static final String CONTENT_TYPE = "application/octet-stream";

    private Context mContext;
    private boolean mFinished;
    private boolean mSuccessful;
    private boolean mGotError;
    private boolean mGotResults;
    private boolean mGotSegmented;
    private String mResponseMessage;
    private DatabaseHelper mDbHelper;
    private CollectedLeaf mCollectedLeaf;
    private SessionManager mSessionManager;

    public LeafletPhotoUploader(Context context, CollectedLeaf collectedLeaf) {
        setFinished(false);
        setSuccessful(false);
        mContext = context;
        mCollectedLeaf = collectedLeaf;
    }

    public void labelCollectedLeaf(final Species species) {
        RequestParams params = new RequestParams();
        params.put("fmt", "json");
        params.put("key", "sciname");
        if (species != null) {
            params.put("value", species.getScientificName());
        } else {
            params.put("value", "none");
        }
        params.put("user", getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME));
        params.put("passwd", getSessionManager().getCurrentUser().get(SessionManager.KEY_PASSWORD));
        LeafletRestClient.post("/" + mCollectedLeaf.getLeafID() + "/label/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if(response.getString("status").contentEquals("labeled")) {
                        mCollectedLeaf.setSelectedSpeciesRel(species);
                        getDbHelper().getCollectedLeafDao().update(mCollectedLeaf);
                        setSuccessful(true);
                    } else {
                        setSuccessful(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setFinished(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                setFinished(true);
                setSuccessful(false);
                setResponseMessage(IMAGE_UPLOAD_DEFAULT_FAIL_MSG);
                Log.d(LOG_TAG, "Collected leaf labeling failed. Error: " + errorResponse);
            }
        });
    }

    public void deleteCollectedLeaf() {
        RequestParams params = new RequestParams();
        params.put("fmt", "json");
        params.put("user", getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME));
        params.put("passwd", getSessionManager().getCurrentUser().get(SessionManager.KEY_PASSWORD));
        LeafletRestClient.post("/" + mCollectedLeaf.getLeafID() + "/delete/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if(response.getString("status").contentEquals("deleted")) {
                        getDbHelper().getCollectedLeafDao().delete(mCollectedLeaf);
                        setSuccessful(true);
                    } else {
                        setSuccessful(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setFinished(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                setFinished(true);
                setSuccessful(false);
                setResponseMessage(IMAGE_UPLOAD_DEFAULT_FAIL_MSG);
                Log.d(LOG_TAG, "Collected leaf deletion failed. Error: " + errorResponse);
            }
        });
    }

    public void uploadImage() {
        if (MediaUtils.isExternalStorageReadable()) {
            File imageFile = new File(mCollectedLeaf.getOriginalImageURL().getRawURL());
            try {
                RequestParams params = new RequestParams();
                params.put("fmt", "json");
                params.put("user", getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME));
                params.put("passwd", getSessionManager().getCurrentUser().get(SessionManager.KEY_PASSWORD));
                params.put("latitude", mCollectedLeaf.getLatitude());
                params.put("longitude", mCollectedLeaf.getLongitude());
                params.put("altitude", mCollectedLeaf.getAltitude());
                params.put("myfile", imageFile, CONTENT_TYPE);
                LeafletRestClient.post("/upload/", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            parseResult(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setFinished(true);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        setFinished(true);
                        setSuccessful(false);
                        setResponseMessage(IMAGE_UPLOAD_DEFAULT_FAIL_MSG);
                        Log.d(LOG_TAG, "Photo uploading failed. Error: " + errorResponse);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void parseResult(JSONObject response) throws JSONException, SQLException {
        if (mCollectedLeaf.getLeafID() == -1) {
            mCollectedLeaf.setLeafID(response.getLong("id"));
            pollForResults();
        }
    }

    private void pollForResults() {
        mGotError = false;
        mGotResults = false;
        mGotSegmented = false;

        while (!mGotResults && !mGotError) {
            RequestParams params = new RequestParams();
            params.put("fmt", "json");
            LeafletRestClient.get("/" + mCollectedLeaf.getLeafID() + "/", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        JSONArray progress = response.getJSONArray("progress");
                        if (progress.toString().contains("error")) {
                            setSuccessful(false);
                            mGotError = true;
                        }

                        if (!mGotSegmented && progress.toString().contains("segmented")) {
                            parseOriginalImageUrl();
                            parseSegmentedImageUrl();
                            mGotSegmented = true;
                        }

                        if (!mGotResults && progress.toString().contains("results")) {
                            LeafletRecognitionRequest recognitionRequest =
                                    new LeafletRecognitionRequest(mContext, mCollectedLeaf);
                            recognitionRequest.loadRecognitionResult();
                            setSuccessful(true);
                            mGotResults = true;
                        }

                        setResponseMessage(response.getString("msg"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {
                    mGotError = true;
                    setSuccessful(false);
                    setResponseMessage(IMAGE_UPLOAD_DEFAULT_FAIL_MSG);
                    Log.e(LOG_TAG, "Recognition request failed. Error: " + error.getMessage());
                }
            }, false);

            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!mGotError && !mGotResults) {
            setSuccessful(false);
            setResponseMessage(IMAGE_UPLOAD_DEFAULT_FAIL_MSG);
        }
    }

    private void parseSegmentedImageUrl() throws SQLException {
        LeafletUrl segmentedLeafletUrl = new LeafletUrl();
        segmentedLeafletUrl.setRawURL("/" + mCollectedLeaf.getLeafID() + "/segmented.png");
        mCollectedLeaf.setSegmentedImageURL(segmentedLeafletUrl);

        LeafletImageLoader imageLoader = new LeafletImageLoader(mContext, segmentedLeafletUrl);
        imageLoader.loadImage();
    }

    private void parseOriginalImageUrl() throws SQLException {
        LeafletUrl originalLeafletUrl = new LeafletUrl();
        originalLeafletUrl.setRawURL("/" + mCollectedLeaf.getLeafID() + "/original.jpg");
        mCollectedLeaf.setOriginalImageURL(originalLeafletUrl);

        LeafletImageLoader imageLoader = new LeafletImageLoader(mContext, originalLeafletUrl);
        imageLoader.loadImage();
    }

    public long getCollectedLeafId() {
        return mCollectedLeaf.getLeafID();
    }

    public boolean wasSuccessful() {
        return mSuccessful;
    }

    public void setSuccessful(boolean mSuccessful) {
        this.mSuccessful = mSuccessful;
    }

    public boolean isFinished() {
        return mFinished;
    }

    public void setFinished(boolean finished) {
        this.mFinished = finished;
    }

    public String getResponseMessage() {
        return mResponseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.mResponseMessage = responseMessage;
    }

    public SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(mContext);
        }
        return mSessionManager;
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
