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
import edu.maryland.leafsnap.util.MediaUtils;

/**
 * TODO
 * Created by Arthur Jacobs on 16/07/2014.
 */
public class LeafletImageLoader {

    private static final String LOG_TAG = "IMAGE_LOADER";

    private Context mContext;

    private boolean mSynchronous;

    private LeafletUrl mLeafletUrl;

    public LeafletImageLoader(Context context, LeafletUrl leafletUrl) {
        mContext = context;
        mLeafletUrl = leafletUrl;
        mSynchronous = false;
    }

    public LeafletImageLoader(Context context, LeafletUrl leafletUrl, boolean synchronous) {
        mContext = context;
        mLeafletUrl = leafletUrl;
        mSynchronous = synchronous;
    }

    public void loadImage() {
        BinaryHttpResponseHandler handler = getBinaryHttpResponseHandler();

        if (mSynchronous) {
            LeafletSyncRestClient.get(mLeafletUrl.getRawURL(), null, handler);
        } else {
            LeafletAsyncRestClient.get(mLeafletUrl.getRawURL(), null, handler);
        }
    }

    private BinaryHttpResponseHandler getBinaryHttpResponseHandler() {
        return new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                Bitmap image = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);
                String imagePath = mLeafletUrl.getRawURL().split("\\?")[0]; // In case it's a cropped image.
                MediaUtils.saveImageToExternalStorage(
                        mContext,
                        Bitmap.createScaledBitmap(image, image.getWidth() / 2, image.getHeight() / 2, false),
                        imagePath);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                Log.e(LOG_TAG, "Could not download image. Error: " + error.getMessage());
            }
        };
    }


}
