package edu.maryland.leafsnap.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.activity.SpeciesActivity;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.LeafletUrl;
import edu.maryland.leafsnap.model.Species;
import edu.maryland.leafsnap.util.MediaUtils;
import uk.co.senab.photoview.PhotoViewAttacher;

public class SpeciesImagesFragment extends Fragment {

    private boolean mFullscreen = false;

    private LinearLayout mImagePicker;

    private Species mSpecies;
    private PhotoViewAttacher mAttacher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle b = this.getArguments();
        if (b != null) {
            mSpecies = (Species) b.getSerializable(SpeciesActivity.ARG_SPECIES);
        }
        return inflater.inflate(R.layout.fragment_species_images, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mImagePicker = (LinearLayout) getActivity().findViewById(R.id.image_picker);
        new PopulateImagePickerTask().execute();

    }

    @Override
    public void onResume() {
        super.onResume();
        setImageDisplayFirstLeaflet();
    }

    private void setImageDisplayFirstLeaflet() {
        ImageView imageDisplay = (ImageView) getActivity().findViewById(R.id.image_display);
        LinearLayout imagePicker = (LinearLayout) getActivity().findViewById(R.id.image_picker);

        FrameLayout imageLayout = ((FrameLayout) imagePicker.getChildAt(0));
        if (imageLayout != null) {
            imageDisplay.setImageDrawable(((ImageView) imageLayout.getChildAt(0))
                    .getDrawable());
            if (mAttacher == null) {
                mAttacher = new PhotoViewAttacher(imageDisplay);
            } else {
                mAttacher.update();
            }
        }
    }

    private void initImageDisplay() {
        setImageDisplayFirstLeaflet();

        mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float v, float v2) {
                toggleFullscreen(!mFullscreen);
            }
        });
    }

    private void toggleFullscreen(boolean fullscreen) {
        mFullscreen = fullscreen;
        WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            ((ActionBarActivity) getActivity()).getSupportActionBar().hide();
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            ((ActionBarActivity) getActivity()).getSupportActionBar().show();
        }
        getActivity().getWindow().setAttributes(attrs);

        FrameLayout optionsBar = (FrameLayout) getActivity().findViewById(R.id.images_options_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            optionsBar.animate()
                .translationY(fullscreen ? optionsBar.getHeight() : 0)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        } else {
            optionsBar.setVisibility(fullscreen ? View.GONE : View.VISIBLE);
        }
    }

    private FrameLayout getSmallImageLayout(LeafletUrl leafletUrl, Drawable d) {
        final FrameLayout layout = new FrameLayout(getActivity());
        ImageView leafletImage = getLeafletTypeImageView(d);
        leafletImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ImageView imageDisplay = (ImageView) getActivity().findViewById(R.id.image_display);
                imageDisplay.setImageDrawable(((ImageView) view)
                        .getDrawable());
                mAttacher.update();
                return true;
            }
        });
        layout.addView(leafletImage, 0);
        layout.addView(getLeafletTypeTextView(leafletUrl), 1);
        return layout;
    }

    private ImageView getLeafletTypeImageView(Drawable d) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160,
                getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120,
                getResources().getDisplayMetrics());
        ImageView view = new ImageView(getActivity());
        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.setPadding(10, 10, 10, 10);
        view.setImageDrawable(d);
        return view;
    }

    private TextView getLeafletTypeTextView(LeafletUrl leafletUrl) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER | Gravity.TOP;
        TextView leafletType = new TextView(getActivity());
        leafletType.setText(leafletUrl.getType());
        leafletType.setTextColor(Color.WHITE);
        leafletType.setPadding(10, 10, 10, 10);
        leafletType.setLayoutParams(lp);
        return leafletType;
    }

    private class PopulateImagePickerTask extends AsyncTask<Void, Void, ArrayList<LeafletUrl>> {

        private DatabaseHelper mDbHelper;

        @Override
        protected ArrayList<LeafletUrl> doInBackground(Void... params) {
            ArrayList<LeafletUrl> leafletUrls = null;
            try {
                leafletUrls = (ArrayList<LeafletUrl>)
                        getDbHelper().getLeafletUrlDao().queryForEq("associatedSpecies_id",
                                mSpecies.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return leafletUrls;
        }

        @Override
        protected void onPostExecute(ArrayList<LeafletUrl> result) {
            if (!result.isEmpty()) {
                for (LeafletUrl leafletUrl : result) {
                    Drawable d = MediaUtils.getDrawableFromAssets(getActivity(),
                            leafletUrl.getRawURL().replace("/species", "species"));
                    if (d != null) {
                        mImagePicker.addView(getSmallImageLayout(leafletUrl, d));
                    }
                }
                initImageDisplay();
            }

            if (mDbHelper != null) {
                OpenHelperManager.releaseHelper();
                mDbHelper = null;
            }
        }

        private DatabaseHelper getDbHelper() {
            if (mDbHelper == null) {
                mDbHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
            }
            return mDbHelper;
        }
    }


}
