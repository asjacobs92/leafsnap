package edu.maryland.leafsnap.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.loopj.android.http.BinaryHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.maryland.leafsnap.model.LeafletUrl;

/**
 * TODO
 * Created by Arthur Jacobs on 16/07/2014.
 */
public class LeafletImageLoader {

    private static final String LOG_TAG = "IMAGE_LOADER";

    private static final String IMAGES_DIR = "images/";

    private static final long MINIMUM_FREE_SPACE_BYTES = 104857600;

    private boolean synchronous;
    private Context mContext;
    private LeafletUrl mLeafletUrl;

    public LeafletImageLoader(Context context, LeafletUrl leafletUrl) {
        setContext(context);
        setLeafletUrl(leafletUrl);
        setSynchronous(false);
    }

    public LeafletImageLoader(Context context, LeafletUrl leafletUrl, boolean synchronous) {
        setContext(context);
        setLeafletUrl(leafletUrl);
        setSynchronous(synchronous);
    }

    public void loadImage() {
        BinaryHttpResponseHandler handler = new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                Bitmap image = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);
                String imagePath = getLeafletUrl().getRawURL().split("\\?")[0]; // In case it's a cropped image.
                saveImageToExternalStorage(
                        Bitmap.createScaledBitmap(image, image.getWidth() / 2, image.getHeight() / 2, false), imagePath);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                Log.e(LOG_TAG, "Could not download image. Error: " + error.getMessage());
            }
        };

        if (isSynchronous()) {
            LeafletSyncRestClient.get(getLeafletUrl().getRawURL(), null, handler);
        } else {
            LeafletAsyncRestClient.get(getLeafletUrl().getRawURL(), null, handler);
        }
    }

    private void saveImageToExternalStorage(Bitmap image, String imagePath) {
        if (isExternalStorageWritable()) {
            File imageFile = new File(getContext().getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), imagePath);
            if (!imageFile.getParentFile().exists()) {
                if (!imageFile.getParentFile().mkdirs()) {
                    Log.e(LOG_TAG, "Could not create species directory.");
                }
            }

            if (imageFile.getParentFile().getFreeSpace() > MINIMUM_FREE_SPACE_BYTES) {
                OutputStream fileOutputStream;
                try {
                    if (imageFile.createNewFile()) {
                        fileOutputStream = new FileOutputStream(imageFile);
                        image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    private Context getContext() {
        return mContext;
    }

    private void setContext(Context mContext) {
        this.mContext = mContext;
    }

    private LeafletUrl getLeafletUrl() {
        return mLeafletUrl;
    }

    private void setLeafletUrl(LeafletUrl leafletUrl) {
        this.mLeafletUrl = leafletUrl;
    }
}
