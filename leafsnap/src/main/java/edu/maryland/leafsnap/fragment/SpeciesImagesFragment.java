package edu.maryland.leafsnap.fragment;

import android.annotation.TargetApi;
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
import edu.maryland.leafsnap.util.SystemUiHider;

public class SpeciesImagesFragment extends Fragment {

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link edu.maryland.leafsnap.util.SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private Species mSpecies;

    private DatabaseHelper mDbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDbHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        Bundle b = this.getArguments();
        if (b != null) {
            mSpecies = (Species) b.getSerializable(SpeciesAcitivity.ARG_SPECIES);
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

    private void setImageDisplayFirstLeaflet() {
        ImageView imageDisplay = (ImageView) getActivity().findViewById(R.id.image_display);
        LinearLayout imagePicker = (LinearLayout) getActivity().findViewById(R.id.image_picker);

        FrameLayout imageLayout = ((FrameLayout) imagePicker.getChildAt(0));
        if (imageLayout != null) {
            imageDisplay.setImageDrawable(((ImageView) imageLayout.getChildAt(0))
                    .getDrawable());
        }
    }

    private void initImageDisplay() {
        ImageView imageDisplay = (ImageView) getActivity().findViewById(R.id.image_display);
        setImageDisplayFirstLeaflet();

        mSystemUiHider = SystemUiHider.getInstance(getActivity(), imageDisplay, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(getOnVisibilityChangeListener());

        imageDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });
    }

    private SystemUiHider.OnVisibilityChangeListener getOnVisibilityChangeListener() {
        return new SystemUiHider.OnVisibilityChangeListener() {
            int mControlsHeight;
            int mShortAnimTime;

            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
            public void onVisibilityChange(boolean visible) {
                if (visible) {
                    ((ActionBarActivity) getActivity()).getSupportActionBar().show();
                } else {
                    ((ActionBarActivity) getActivity()).getSupportActionBar().hide();
                }

                FrameLayout optionsBar = (FrameLayout) getActivity().findViewById(R.id.images_options_bar);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    if (mControlsHeight == 0) {
                        mControlsHeight = optionsBar.getHeight();
                    }

                    if (mShortAnimTime == 0) {
                        mShortAnimTime = getResources().getInteger(
                                android.R.integer.config_shortAnimTime);
                    }
                    optionsBar.animate()
                            .translationY(visible ? 0 : mControlsHeight)
                            .setDuration(mShortAnimTime);
                } else {
                    optionsBar.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            }
        };
    }

    private void initImagePicker() {
        LinearLayout imagePicker = (LinearLayout) getActivity().findViewById(R.id.image_picker);
        try {
            List<LeafletUrl> leafletUrls = mDbHelper.getLeafletUrlDao().queryForEq("associatedSpecies_id",
                    mSpecies.getId());
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
}
