package edu.maryland.leafsnap.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.fragment.SpeciesDetailsFragment;
import edu.maryland.leafsnap.fragment.SpeciesImagesFragment;
import edu.maryland.leafsnap.model.Species;

public class SpeciesAcitivity extends ActionBarActivity {

    public static final String ARG_SPECIES = "species";

    /**
     * Whether or not we're showing the back of the card (otherwise showing the front).
     */
    private boolean mShowingBack = false;

    private Species mSpecies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            mSpecies = (Species) b.getSerializable(ARG_SPECIES);
            if (mSpecies != null) {
                setTitle(mSpecies.getScientificName());
            }
        }

        if (savedInstanceState == null) {
            SpeciesImagesFragment fragment = new SpeciesImagesFragment();
            Bundle args = new Bundle();
            args.putSerializable(SpeciesAcitivity.ARG_SPECIES, mSpecies);
            fragment.setArguments(args);
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.card_flip_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.species, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                item.setIcon(mShowingBack ? R.drawable.ic_action_about : R.drawable.ic_action_about_reverse);
                flipFragments();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void flipFragments() {
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            mShowingBack = false;
        } else {
            mShowingBack = true;
            SpeciesDetailsFragment fragment = new SpeciesDetailsFragment();
            Bundle args = new Bundle();
            args.putSerializable(SpeciesAcitivity.ARG_SPECIES, mSpecies);
            fragment.setArguments(args);
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.card_flip_right_in, R.anim.card_flip_right_out,
                            R.anim.card_flip_right_in, R.anim.card_flip_right_out)
                    .replace(R.id.card_flip_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
