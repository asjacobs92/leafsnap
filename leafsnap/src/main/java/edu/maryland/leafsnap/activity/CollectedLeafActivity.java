package edu.maryland.leafsnap.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.j256.ormlite.dao.ForeignCollection;

import java.util.ArrayList;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.adapter.RankedSpeciesListAdapter;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.model.RankedSpecies;
import edu.maryland.leafsnap.util.MediaUtils;

public class CollectedLeafActivity extends ActionBarActivity {

    public static final String ARG_COLLECTED_LEAF = "collected_leaf";

    private CollectedLeaf mCollectedLeaf;
    private RankedSpeciesListAdapter mRankedListAdapter;
    private ArrayList<RankedSpecies> mCandidateSpecies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collected_leaf);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            mCollectedLeaf = (CollectedLeaf) b.getSerializable(ARG_COLLECTED_LEAF);
            if (mCollectedLeaf.getSelectedSpeciesRel() != null) {
                setTitle(mCollectedLeaf.getSelectedSpeciesRel().getScientificName());
            } else {
                setTitle(getString(R.string.unlabeled_species));
            }

            ListView resultsList = (ListView) findViewById(R.id.results_list);
            ImageView originalImage = (ImageView) findViewById(R.id.collected_leaf_display);
            final ImageView segmentedImage = (ImageView) findViewById(R.id.segmented_leaf_display);

            originalImage.setImageDrawable(MediaUtils.getDrawableFromExternalStorage(this,
                    mCollectedLeaf.getOriginalImageURL().getRawURL()));
            segmentedImage.setImageDrawable(MediaUtils.getDrawableFromExternalStorage(this,
                    mCollectedLeaf.getSegmentedImageURL().getRawURL()));

            originalImage.animate().translationX(-(1.5f)*originalImage.getDrawable().getIntrinsicWidth())
                    .setDuration(1000).setStartDelay(1000).start();
            segmentedImage.animate().translationX((1.6f)*segmentedImage.getDrawable().getIntrinsicWidth())
                    .setDuration(1000).setStartDelay(1000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    segmentedImage.setAlpha(1f);
                }
            }).start();

            mCandidateSpecies = new ArrayList<RankedSpecies>();
            mRankedListAdapter = new RankedSpeciesListAdapter(this, mCandidateSpecies);
            resultsList.setAdapter(mRankedListAdapter);

            resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), SpeciesActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable(SpeciesActivity.ARG_SPECIES,
                            mRankedListAdapter.getItem(position).getSpecies());
                    intent.putExtras(args);
                    startActivity(intent);
                }
            });

            Button labelButton = (Button) findViewById(R.id.label_button);
            labelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleLabelButton();
                }
            });

            new RankedListTask().execute();
        }
    }

    public void toggleLabelButton() {
        Button labelButton = (Button) findViewById(R.id.label_button);
        if (mRankedListAdapter.isActionButtonVisible()) {
            mRankedListAdapter.setActionButtonVisible(false);
            labelButton.setBackgroundResource(R.drawable.header_button_shape);
        } else {
            mRankedListAdapter.setActionButtonVisible(true);
            labelButton.setBackgroundResource(R.drawable.header_button_shape_toggled);
        }
        mRankedListAdapter.notifyDataSetChanged();
    }

    private class RankedListTask extends AsyncTask<Void, Void, ArrayList<RankedSpecies>> {

        @Override
        protected ArrayList<RankedSpecies> doInBackground(Void... params) {
            ForeignCollection<RankedSpecies> candidateSpecies = mCollectedLeaf.getCandidateSpecies();
            if (candidateSpecies != null) {
                return new ArrayList<RankedSpecies>(candidateSpecies);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<RankedSpecies> result) {
            if (result != null) {
                mCandidateSpecies.clear();
                mCandidateSpecies.addAll(result);
                mRankedListAdapter.notifyDataSetChanged();
            }
        }
    }
}
