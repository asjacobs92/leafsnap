package edu.maryland.leafsnap.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.model.CollectedLeaf;

public class CollectedLeafActivity extends ActionBarActivity {

    public static final String ARG_COLLECTED_LEAF = "collected_leaf";

    private CollectedLeaf mCollectedLeaf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collected_leaf);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            mCollectedLeaf = (CollectedLeaf) b.getSerializable(ARG_COLLECTED_LEAF);
            ImageView originalImage = (ImageView) findViewById(R.id.collected_leaf_display);
            originalImage.setImageDrawable(getDrawableFromUrl(mCollectedLeaf.getOriginalImageURL().getRawURL()));
            ImageView segmentedImage = (ImageView) findViewById(R.id.segmented_leaf_display);
            segmentedImage.setImageDrawable(getDrawableFromUrl(mCollectedLeaf.getSegmentedImageURL().getRawURL()));
        }
    }

    private Drawable getDrawableFromUrl(String url) {
        InputStream ims = null;
        try {
            if (isExternalStorageReadable()) {
                File imageFile = new File(getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES), url);
                ims = new FileInputStream(imageFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Drawable.createFromStream(ims, null);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
