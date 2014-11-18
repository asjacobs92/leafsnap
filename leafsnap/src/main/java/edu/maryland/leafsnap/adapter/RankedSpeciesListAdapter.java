package edu.maryland.leafsnap.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.ArrayList;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.api.LeafletPhotoUploader;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.model.RankedSpecies;
import edu.maryland.leafsnap.model.Species;
import edu.maryland.leafsnap.util.MediaUtils;

/**
 * @author Arthur Jacobs
 */
public class RankedSpeciesListAdapter extends ArrayAdapter<RankedSpecies> {

    private boolean mActionButtonVisible = false;
    private LayoutInflater mInflater;
    private ArrayList<RankedSpecies> mSpeciesList;

    public RankedSpeciesListAdapter(Context context, ArrayList<RankedSpecies> speciesList) {
        super(context, R.layout.list_item, speciesList);
        this.mSpeciesList = speciesList;
    }

    @Override
    public RankedSpecies getItem(int position) {
        return mSpeciesList.get(position);
    }

    @Override
    public int getCount() {
        return mSpeciesList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.item_text);
            holder.subtext = (TextView) convertView.findViewById(R.id.item_subtext);
            holder.index = (TextView) convertView.findViewById(R.id.item_index);
            holder.image = (ImageView) convertView.findViewById(R.id.item_image);
            holder.actionButton = (Button) convertView.findViewById(R.id.item_action_button);
            holder.labelCheck = (ImageView) convertView.findViewById(R.id.item_label_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final RankedSpecies rankedSpecies = mSpeciesList.get(position);

        Species species = rankedSpecies.getSpecies();
        holder.index.setText("" + rankedSpecies.getRank());
        holder.text.setText(species.getCommomName());
        holder.subtext.setText(species.getScientificName());
        holder.image.setImageDrawable(MediaUtils.getDrawableFromAssets(getContext(),
                species.getExampleImageLeaf().getRawURL().replace("/species", "species").split("\\?")[0]));

        holder.actionButton.setText(getContext().getString(R.string.label));
        holder.actionButton.setBackgroundResource(R.drawable.label_button_shape);
        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LabelImageTask(rankedSpecies).execute();
            }
        });

        CollectedLeaf collectedLeaf = rankedSpecies.getAssociatedCollection();
        if (collectedLeaf.getSelectedSpeciesRel() != null && collectedLeaf.getSelectedSpeciesRel().compareTo(species) == 0) {
            holder.labelCheck.setVisibility(View.VISIBLE);
            holder.actionButton.setVisibility(View.GONE);
        } else {
            holder.labelCheck.setVisibility(View.GONE);
            if (isActionButtonVisible()) {
                holder.actionButton.setVisibility(View.VISIBLE);
            } else {
                holder.actionButton.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    public boolean isActionButtonVisible() {
        return mActionButtonVisible;
    }

    public void setActionButtonVisible(boolean mActionButtonVisible) {
        this.mActionButtonVisible = mActionButtonVisible;
    }

    public LayoutInflater getLayoutInflater() {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(getContext());
        }
        return mInflater;
    }

    private static class ViewHolder {
        TextView text;
        TextView subtext;
        TextView index;
        ImageView image;
        Button actionButton;
        ImageView labelCheck;
    }

    private class LabelImageTask extends AsyncTask<Void, Void, Boolean> {

        private RankedSpecies mRankedSpecies;

        public LabelImageTask(RankedSpecies species) {
            super();
            mRankedSpecies = species;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            LeafletPhotoUploader photoUploader =
                    new LeafletPhotoUploader(getContext(), mRankedSpecies.getAssociatedCollection());
            photoUploader.labelCollectedLeaf(mRankedSpecies.getSpecies());
            while (!photoUploader.isFinished()) {
                SystemClock.sleep(100);
            }
            photoUploader.close();

            return photoUploader.wasSuccessful();
        }

        @Override
        protected void onPostExecute(Boolean labelSuccessful) {
            if (labelSuccessful) {
                notifyDataSetChanged();
                ((Activity) getContext()).setTitle(mRankedSpecies.getSpecies().getScientificName());
            } else {
                Toast.makeText(getContext(), R.string.labeling_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
