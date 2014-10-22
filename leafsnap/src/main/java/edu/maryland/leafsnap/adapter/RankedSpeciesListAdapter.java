package edu.maryland.leafsnap.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.model.RankedSpecies;
import edu.maryland.leafsnap.model.Species;
import edu.maryland.leafsnap.util.MediaUtils;

/**
 * @author Arthur Jacobs
 */
public class RankedSpeciesListAdapter extends ArrayAdapter<RankedSpecies> {

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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Species species = getItem(position).getSpecies();
        holder.index.setText("" + getItem(position).getRank());
        holder.text.setText(species.getCommomName());
        holder.subtext.setText(species.getScientificName());
        holder.image.setImageDrawable(MediaUtils.getDrawableFromAssets(getContext(),
                species.getExampleImageLeaf().getRawURL().replace("/species", "species").split("\\?")[0]));

        return convertView;
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
    }

}
