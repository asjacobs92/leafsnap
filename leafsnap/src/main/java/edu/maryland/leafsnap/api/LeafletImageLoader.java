package edu.maryland.leafsnap.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.loopj.android.http.BinaryHttpResponseHandler;

import org.apache.http.Header;

import edu.maryland.leafsnap.model.LeafletUrl;
import edu.maryland.leafsnap.util.MediaUtils;

/**
 * TODO
 * Created by Arthur Jacobs on 16/07/2014.
 */
public class LeafletImageLoader {

    private static final String LOG_TAG = "IMAGE_LOADER";

    private Context mContext;

    private LeafletUrl mLeafletUrl;

    public LeafletImageLoader(Context context, LeafletUrl leafletUrl) {
        mContext = context;
        mLeafletUrl = leafletUrl;
    }

    public void loadImage() {
        LeafletRestClient.get(mLeafletUrl.getRawURL(), null, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                Bitmap image = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);
                String imagePath = mLeafletUrl.getRawURL().split("\\?")[0]; // In case it's a cropped image.
                MediaUtils.saveBitmapToExternalStorage(
                        mContext,
                        Bitmap.createScaledBitmap(image, image.getWidth() / 2, image.getHeight() / 2, false),
                        imagePath);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                Log.e(LOG_TAG, "Could not download image. Error: " + error.getMessage());
            }
        }, true);
    }
}
