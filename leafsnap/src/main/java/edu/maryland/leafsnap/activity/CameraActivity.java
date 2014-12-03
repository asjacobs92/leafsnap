package edu.maryland.leafsnap.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.util.CameraPreview;
import edu.maryland.leafsnap.util.MediaUtils;

public class CameraActivity extends Activity {

    private static final String TAG = "CAMERA_ACTIVITY";
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = MediaUtils.getPublicDirectoryOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions.");
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap oldBitmap = bitmap;
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            oldBitmap.recycle();

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent i = new Intent(getBaseContext(), UploadImageActivty.class);
            i.putExtra(UploadImageActivty.ARG_IMAGE_URI, Uri.fromFile(pictureFile));
            startActivity(i);
            finish();
        }
    };
    private static final int IMAGE_PICKER_SELECT = 999;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        if (mCamera != null) {
            MediaUtils.setCameraDisplayOrientation(this, 0, mCamera);

            CameraPreview cPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(cPreview);
        } else {
            Log.d(TAG, "Camera is unavailable. Something is wrong.");
        }
    }

    public void onCaptureButtonClick(View v) {
        mCamera.takePicture(null, null, mPicture);
    }

    public void onCancelButtonClick(View v) {
        finish();
    }

    public void onGalleryButtonClick(View v) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, IMAGE_PICKER_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_PICKER_SELECT && resultCode != 0) {
            Uri bitmapUri = data.getData();
            Intent i = new Intent(getBaseContext(), UploadImageActivty.class);
            i.putExtra(UploadImageActivty.ARG_IMAGE_URI, bitmapUri);
            startActivity(i);
        }
        finish();
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Log.d(TAG, "Camera was in use or does not exist. " + e.getMessage());
        }
        return c;
    }
}
