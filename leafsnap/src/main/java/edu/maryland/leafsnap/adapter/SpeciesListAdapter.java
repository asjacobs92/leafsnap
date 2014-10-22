package edu.maryland.leafsnap.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.model.Species;
import edu.maryland.leafsnap.util.MediaUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * @author Arthur Jacobs
 */
public class SpeciesListAdapter extends ArrayAdapter<Species> implements StickyListHeadersAdapter {

    private Filter mFilter;
    private LayoutInflater mInflater;
    private ArrayList<Species> mSpeciesList;
    private ArrayList<Species> mOriginalSpeciesList;

    public SpeciesListAdapter(Context context, ArrayList<Species> speciesList) {
        super(context, R.layout.list_item, speciesList);
        this.mSpeciesList = this.mOriginalSpeciesList = speciesList;
    }

    @Override
    public Species getItem(int position) {
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
            holder.image = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Species species = getItem(position);
        holder.text.setText(species.getCommomName());
        holder.subtext.setText(species.getScientificName());
        holder.image.setImageDrawable(MediaUtils.getDrawableFromAssets(getContext(),
                species.getExampleImageLeaf().getRawURL().replace("/species", "species").split("\\?")[0]));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new SpeciesFilter();

        return mFilter;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = getLayoutInflater().inflate(R.layout.list_section_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.section_header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        String headerText = "" + mSpeciesList.get(position).getCommomName().subSequence(0, 1).charAt(0);
        holder.text.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return mSpeciesList.get(position).getCommomName().subSequence(0, 1).charAt(0);
    }

    public LayoutInflater getLayoutInflater() {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(getContext());
        }
        return mInflater;
    }

    private static class HeaderViewHolder {
        TextView text;
    }

    private static class ViewHolder {
        TextView text;
        TextView subtext;
        ImageView image;
    }

    private class SpeciesFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Species> filteredSpeciesList = new ArrayList<Species>();
            for (Species oneSpecies : mOriginalSpeciesList) {
                if (oneSpecies.getCommomName().toUpperCase(Locale.getDefault())
                        .startsWith(constraint.toString().toUpperCase(Locale.getDefault())))
                    filteredSpeciesList.add(oneSpecies);
            }

            results.values = filteredSpeciesList;
            results.count = filteredSpeciesList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mSpeciesList = (ArrayList<Species>) results.values;
            notifyDataSetChanged();
        }
    }
}
