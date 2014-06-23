package edu.maryland.leafsnap.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.Species;

public class HomeFragment extends Fragment {

    private static final int ANIMATION_LENGTH_MILLI = 6000;
    private static final int ANIMATION_FADE_LENGTH_MILLI = 3000;

    private DatabaseHelper mDbHelper = null;
    private HashMap<Drawable, String> mRandThumbnails = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRandThumbnails = new HashMap<Drawable, String>();
        mDbHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ImageView randThumbnail = (ImageView) getActivity().findViewById(R.id.rand_thumb);

        AnimationDrawable animation = initRandomImageAnimation();
        animation.start();
        randThumbnail.setImageDrawable(animation);
        randThumbnail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                TextView randThumbnailText = (TextView) getActivity().findViewById(R.id.rand_thumb_text);
                int visibility = isThumbnailTextVisible(randThumbnailText) ? View.INVISIBLE : View.VISIBLE;
                randThumbnailText.setVisibility(visibility);
            }
        });
    }

    private AnimationDrawable initRandomImageAnimation() {
        ArrayList<Species> speciesList = getSpeciesList();
        ArrayList<Drawable> imagesList = getImageList(speciesList);

        AnimationDrawable animation = new AnimationDrawable();

        for (Drawable image : imagesList) {
            animation.addFrame(image, ANIMATION_LENGTH_MILLI);
        }

        animation.setOneShot(false);
        animation.setEnterFadeDuration(ANIMATION_FADE_LENGTH_MILLI);
        animation.setExitFadeDuration(ANIMATION_FADE_LENGTH_MILLI);

        return animation;
    }

    private ArrayList<Drawable> getImageList(ArrayList<Species> speciesList) {
        ArrayList<Drawable> imagesList = new ArrayList<Drawable>();

        for (Species oneSpecies : speciesList) {
            ArrayList<String> exampleImageUrls = getExampleImageUrls(oneSpecies);
            for (String url : exampleImageUrls) {
                Drawable d = null;
                try {
                    InputStream ims = getActivity().getAssets().open(url);
                    d = Drawable.createFromStream(ims, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (d != null) {
                    mRandThumbnails.put(d, oneSpecies.getScientificName());
                    imagesList.add(d);
                }
            }
        }
        return imagesList;
    }

    private ArrayList<String> getExampleImageUrls(Species oneSpecies) {
        ArrayList<String> exampleImageUrls = new ArrayList<String>();
        String rawUrl = null;
        String[] splitUrl = null;

        rawUrl = oneSpecies.getExampleImageFlower().getRawURL().replace("/species", "species");
        splitUrl = rawUrl.split("\\?");
        exampleImageUrls.add(splitUrl[0]);

        rawUrl = oneSpecies.getExampleImageFruit().getRawURL().replace("/species", "species");
        splitUrl = rawUrl.split("\\?");
        exampleImageUrls.add(splitUrl[0]);

        rawUrl = oneSpecies.getExampleImageLeaf().getRawURL().replace("/species", "species");
        splitUrl = rawUrl.split("\\?");
        exampleImageUrls.add(splitUrl[0]);

        return exampleImageUrls;
    }

    private ArrayList<Species> getSpeciesList() {
        ArrayList<Species> speciesList = null;
        try {
            speciesList = (ArrayList<Species>) mDbHelper.getSpeciesDao().queryBuilder().orderByRaw("RANDOM()")
                    .limit(10L).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return speciesList;
    }

    private boolean isThumbnailTextVisible(TextView randThumbnailText) {
        return randThumbnailText.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDbHelper != null) {
            OpenHelperManager.releaseHelper();
            mDbHelper = null;
        }
    }
}
