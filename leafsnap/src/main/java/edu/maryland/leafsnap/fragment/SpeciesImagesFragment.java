package edu.maryland.leafsnap.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.activity.SpeciesAcitivity;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.LeafletUrl;
import edu.maryland.leafsnap.model.Species;
import uk.co.senab.photoview.PhotoViewAttacher;

public class SpeciesImagesFragment extends Fragment {

    private boolean fullscreen = false;

    private Species mSpecies;
    private DatabaseHelper mDbHelper;
    private PhotoViewAttacher mAttacher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle b = this.getArguments();
        if (b != null) {
            setSpecies((Species) b.getSerializable(SpeciesAcitivity.ARG_SPECIES));
        }
        return inflater.inflate(R.layout.fragment_species_images, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initImagePicker();
        initImageDisplay();
    }

    @Override
    public void onResume() {
        super.onResume();
        setImageDisplayFirstLeaflet();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDbHelper != null) {
            OpenHelperManager.releaseHelper();
            mDbHelper = null;
        }
    }

    private void setImageDisplayFirstLeaflet() {
        ImageView imageDisplay = (ImageView) getActivity().findViewById(R.id.image_display);
        LinearLayout imagePicker = (LinearLayout) getActivity().findViewById(R.id.image_picker);

        FrameLayout imageLayout = ((FrameLayout) imagePicker.getChildAt(0));
        if (imageLayout != null) {
            imageDisplay.setImageDrawable(((ImageView) imageLayout.getChildAt(0))
                    .getDrawable());
            if (getAttacher() == null) {
                setAttacher(new PhotoViewAttacher(imageDisplay));
            } else {
                getAttacher().update();
            }
        }
    }

    private void initImageDisplay() {
        setImageDisplayFirstLeaflet();

        getAttacher().setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float v, float v2) {
                if (isFullscreen()) {
                    toggleFullscreen(false);
                } else {
                    toggleFullscreen(true);
                }
            }
        });
    }

    private void toggleFullscreen(boolean fullscreen) {
        setFullscreen(fullscreen);
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

    private void initImagePicker() {
        LinearLayout imagePicker = (LinearLayout) getActivity().findViewById(R.id.image_picker);
        try {
            List<LeafletUrl> leafletUrls = getDbHelper().getLeafletUrlDao().queryForEq("associatedSpecies_id",
                    getSpecies().getId());
            for (LeafletUrl leafletUrl : leafletUrls) {
                Drawable d = getDrawableFromUrl(leafletUrl.getRawURL().replace("/species", "species"));
                if (d != null) {
                    imagePicker.addView(getSmallImageLayout(leafletUrl, d));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                getAttacher().update();
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

    private Drawable getDrawableFromUrl(String url) {
        InputStream ims = null;
        try {
            ims = getActivity().getAssets().open(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Drawable.createFromStream(ims, null);
    }

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return mDbHelper;
    }

    private PhotoViewAttacher getAttacher() {
        return mAttacher;
    }

    private void setAttacher(PhotoViewAttacher mAttacher) {
        this.mAttacher = mAttacher;
    }

    private Species getSpecies() {
        return mSpecies;
    }

    private void setSpecies(Species mSpecies) {
        this.mSpecies = mSpecies;
    }

    private boolean isFullscreen() {
        return fullscreen;
    }

    private void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }
}
