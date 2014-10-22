package edu.maryland.leafsnap.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.j256.ormlite.dao.ForeignCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.adapter.RankedSpeciesListAdapter;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.model.RankedSpecies;
import edu.maryland.leafsnap.util.MediaUtils;

public class CollectedLeafActivity extends ActionBarActivity {

    public static final String ARG_COLLECTED_LEAF = "collected_leaf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collected_leaf);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            CollectedLeaf collectedLeaf = (CollectedLeaf) b.getSerializable(ARG_COLLECTED_LEAF);

            if (collectedLeaf.getSelectedSpeciesRel() != null) {
                setTitle(collectedLeaf.getSelectedSpeciesRel().getScientificName());
            } else {
                setTitle(getString(R.string.unlabeled_species));
            }

            ImageView originalImage = (ImageView) findViewById(R.id.collected_leaf_display);
            originalImage.setImageDrawable(
                    MediaUtils.getDrawableFromExternalStorage(this, collectedLeaf.getOriginalImageURL().getRawURL()));
            ImageView segmentedImage = (ImageView) findViewById(R.id.segmented_leaf_display);
            segmentedImage.setImageDrawable(
                    MediaUtils.getDrawableFromExternalStorage(this, collectedLeaf.getSegmentedImageURL().getRawURL()));

            ListView resultsList = (ListView) findViewById(R.id.results_list);
            ForeignCollection<RankedSpecies> candidateSpecies = collectedLeaf.getCandidateSpecies();
            if (candidateSpecies != null) {
                ArrayList<RankedSpecies> results = new ArrayList<RankedSpecies>(candidateSpecies);
                final RankedSpeciesListAdapter listAdapter = new RankedSpeciesListAdapter(this, results);
                resultsList.setAdapter(listAdapter);

                resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Intent intent = new Intent(getApplicationContext(), SpeciesAcitivity.class);
                        Bundle args = new Bundle();
                        args.putSerializable(SpeciesAcitivity.ARG_SPECIES,
                                listAdapter.getItem(position).getSpecies());
                        intent.putExtras(args);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}
