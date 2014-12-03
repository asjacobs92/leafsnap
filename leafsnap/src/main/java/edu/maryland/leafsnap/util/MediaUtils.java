package edu.maryland.leafsnap.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: Comment this.
 * Created by asjacobs on 10/9/14.
 */
public class MediaUtils {

    private static final String TAG = "MEDIA_UTILS";

    private static final long MINIMUM_FREE_SPACE_BYTES = 104857600;

    private static final String IMAGE_PATTERN = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public static Drawable getDrawableFromAssets(Context c, String uri) {
        Drawable d = null;
        try {
            InputStream ims = c.getAssets().open(uri);
            d = Drawable.createFromStream(ims, null);
            ims.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static Drawable getDrawableFromExternalStorage(Context c, String uri) {
        Drawable d = null;
        try {
            if (isExternalStorageReadable()) {
                File imageFile = new File(c.getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES), uri);
                InputStream ims = new FileInputStream(imageFile);
                d = Drawable.createFromStream(ims, null);
                ims.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static String getRealPathFromURI(Context c, Uri contentUri) {
        Pattern pattern = Pattern.compile(IMAGE_PATTERN);
        Matcher matcher = pattern.matcher(contentUri.getLastPathSegment());
        if (matcher.matches()) {
            return contentUri.getPath();
        }
        // can post image
        String [] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = c.getContentResolver().query(contentUri,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    public static void saveBitmapToExternalStorage(Context c, Bitmap bitmap, String bitmapPath) {
        if (MediaUtils.isExternalStorageWritable()) {
            File imageFile = new File(c.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), bitmapPath);
            if (!imageFile.getParentFile().exists()) {
                if (!imageFile.getParentFile().mkdirs()) {
                    Log.e(TAG, "Could not create species directory.");
                }
            }

            if (imageFile.getParentFile().getFreeSpace() > MINIMUM_FREE_SPACE_BYTES) {
                OutputStream fileOutputStream;
                try {
                    if (imageFile.createNewFile()) {
                        fileOutputStream = new FileOutputStream(imageFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File getPublicDirectoryOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Leafsnap");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Failed to create directory.");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
