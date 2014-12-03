package edu.maryland.leafsnap.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.Collections;
import java.util.HashMap;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.Species;
import edu.maryland.leafsnap.util.MediaUtils;

public class HomeFragment extends Fragment {

    private static final int ANIMATION_LENGTH_MILLI = 6000;
    private static final int ANIMATION_FADE_LENGTH_MILLI = 3000;

    private ImageView mRandThumbnail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mRandThumbnail = (ImageView) getActivity().findViewById(R.id.rand_thumb);

        new PopulateAnimationTask().execute();
    }

    private class PopulateAnimationTask extends AsyncTask<Void, Void, AnimationDrawable> {

        private DatabaseHelper mDbHelper;

        @Override
        protected AnimationDrawable doInBackground(Void... params) {
            ArrayList<Species> speciesList = null;
            try {
                speciesList = (ArrayList<Species>) getDbHelper().getSpeciesDao().
                        queryBuilder().orderByRaw("RANDOM()").limit(10L).query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (speciesList != null && !speciesList.isEmpty()) {
                AnimationDrawable animation = new AnimationDrawable();
                ArrayList<Drawable> imagesList = getImageList(speciesList);

                for (Drawable image : imagesList) {
                    animation.addFrame(image, ANIMATION_LENGTH_MILLI);
                }

                animation.setOneShot(false);
                animation.setEnterFadeDuration(ANIMATION_FADE_LENGTH_MILLI);
                animation.setExitFadeDuration(ANIMATION_FADE_LENGTH_MILLI);

                return animation;
            }

            return null;
        }

        @Override
        protected void onPostExecute(AnimationDrawable result) {
            if (result != null) {
                result.start();
                mRandThumbnail.setImageDrawable(result);
                mRandThumbnail.post(result);
            }

            if (mDbHelper != null) {
                OpenHelperManager.releaseHelper();
                mDbHelper = null;
            }
        }

        private ArrayList<Drawable> getImageList(ArrayList<Species> speciesList) {
            ArrayList<Drawable> imagesList = new ArrayList<Drawable>();

            for (Species oneSpecies : speciesList) {
                ArrayList<String> exampleImageUrls = getExampleImageUrls(oneSpecies);
                for (String url : exampleImageUrls) {
                    Drawable d = MediaUtils.getDrawableFromAssets(getActivity(), url);
                    if (d != null) {
                        imagesList.add(d);
                    }
                }
            }
            return imagesList;
        }

        private ArrayList<String> getExampleImageUrls(Species oneSpecies) {
            ArrayList<String> exampleImageUrls = new ArrayList<String>();
            exampleImageUrls.add(oneSpecies.getExampleImageFlower().getRawURL().replace("/species", "species").split("\\?")[0]);
            exampleImageUrls.add(oneSpecies.getExampleImageFruit().getRawURL().replace("/species", "species").split("\\?")[0]);
            exampleImageUrls.add(oneSpecies.getExampleImageLeaf().getRawURL().replace("/species", "species").split("\\?")[0]);
            return exampleImageUrls;
        }

        private DatabaseHelper getDbHelper() {
            if (mDbHelper == null) {
                mDbHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
            }
            return mDbHelper;
        }
    }


}
