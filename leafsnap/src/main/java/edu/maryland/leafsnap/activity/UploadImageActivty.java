package edu.maryland.leafsnap.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.List;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.api.LeafletPhotoUploader;
import edu.maryland.leafsnap.api.LeafletUserCollectionRequest;
import edu.maryland.leafsnap.api.LeafletUserRegistrationRequest;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.model.LeafletUrl;
import edu.maryland.leafsnap.util.MediaUtils;
import edu.maryland.leafsnap.util.SessionManager;

public class UploadImageActivty extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    public static final String ARG_IMAGE_URI = "image_uri";

    private Uri mImageUri;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_upload_image);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(ARG_IMAGE_URI)) {
            mImageUri = getIntent().getParcelableExtra(ARG_IMAGE_URI);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                if (bitmap != null) {
                    ImageView imageUpload = (ImageView) findViewById(R.id.image_upload);
                    imageUpload.setImageBitmap(bitmap);

                    mLocationClient = new LocationClient(this, this, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        CollectedLeaf c = new CollectedLeaf();
        c.setOriginalImageURL(new LeafletUrl(MediaUtils.getRealPathFromURI(this, mImageUri)));

        // Get the current location
        try {
            Location currentLocation = mLocationClient.getLastLocation();
            c.setLatitude(String.valueOf(currentLocation.getLatitude()));
            c.setLongitude(String.valueOf(currentLocation.getLongitude()));
            c.setAltitude(String.valueOf(currentLocation.getAltitude()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        new UploadImageTask(this).execute(c);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        CollectedLeaf c = new CollectedLeaf();
        c.setOriginalImageURL(new LeafletUrl(MediaUtils.getRealPathFromURI(this, mImageUri)));
        new UploadImageTask(this).execute(c);
    }

    private class UploadImageTask extends AsyncTask<CollectedLeaf, Void, CollectedLeaf> {

        private Context mContext;
        private DatabaseHelper mDbHelper;
        private SessionManager mSessionManager;

        public UploadImageTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected CollectedLeaf doInBackground(CollectedLeaf... leaf) {

            if (!getSessionManager().isLoggedIn()) {
                LeafletUserRegistrationRequest mUserRequest =
                        new LeafletUserRegistrationRequest(getBaseContext());
                mUserRequest.registerAccount(null, null);

                while (!mUserRequest.isFinished()) {
                    SystemClock.sleep(100);
                }

                if (mUserRequest.wasSuccessful()) {
                    LeafletPhotoUploader photoUploader = uploadPhoto(leaf[0]);
                    if (photoUploader.wasSuccessful()) {
                        return getUploadedCollectedLeaf(photoUploader.getCollectedLeafId());
                    }
                }
                return null;
            } else {
                LeafletPhotoUploader photoUploader = uploadPhoto(leaf[0]);
                if (photoUploader.wasSuccessful()) {
                    return getUploadedCollectedLeaf(photoUploader.getCollectedLeafId());
                }
            }

            return null;
        }

        private LeafletPhotoUploader uploadPhoto(CollectedLeaf collectedLeaf) {
            final LeafletPhotoUploader photoUploader =
                    new LeafletPhotoUploader(mContext, collectedLeaf);
            photoUploader.uploadImage();
            while (!photoUploader.isFinished()) {
                SystemClock.sleep(100);
            }
            photoUploader.close();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView accountStatus = (TextView) findViewById(R.id.upload_status);
                    accountStatus.setText(photoUploader.getResponseMessage());
                }
            });

            return photoUploader;
        }

        public CollectedLeaf getUploadedCollectedLeaf(long leafID) {
            LeafletUserCollectionRequest collectionRequest =
                    new LeafletUserCollectionRequest(mContext);
            collectionRequest.updateUserCollectionSyncStatus();

            while (!collectionRequest.isFinished()) {
                SystemClock.sleep(100);
            }

            try {
                List<CollectedLeaf> results = getDbHelper().getCollectedLeafDao().queryForEq("leafID", leafID);
                if (!results.isEmpty()) {
                    return results.get(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(CollectedLeaf leaf) {
            setSupportProgressBarIndeterminateVisibility(false);

            if (leaf != null) {
                Intent intent = new Intent(mContext, CollectedLeafActivity.class);
                Bundle args = new Bundle();
                args.putSerializable(CollectedLeafActivity.ARG_COLLECTED_LEAF, leaf);
                intent.putExtras(args);
                mContext.startActivity(intent);
                finish();
            }

            if (mDbHelper != null) {
                OpenHelperManager.releaseHelper();
                mDbHelper = null;
            }
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
    }
}
