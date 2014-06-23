package edu.maryland.leafsnap.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * @author Arthur Jacobs
 */
public class SpeciesListAdapter extends ArrayAdapter<Species> implements StickyListHeadersAdapter {

    private Filter mFilter;
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Species> mSpeciesList;
    private ArrayList<Species> mOriginalSpeciesList;

    public SpeciesListAdapter(Context context, ArrayList<Species> speciesList) {
        super(context, R.layout.species_list_item, speciesList);
        this.mContext = context;
        this.mSpeciesList = speciesList;
        this.mOriginalSpeciesList = speciesList;
    }

    @Override
    public Species getItem(int position) {
        return mSpeciesList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = getInflater().inflate(R.layout.species_list_item, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.itemText);
            holder.subtext = (TextView) convertView.findViewById(R.id.itemSubtext);
            holder.image = (ImageView) convertView.findViewById(R.id.item_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Species species = getItem(position);
        holder.text.setText(species.getCommomName());
        holder.subtext.setText(species.getScientificName());
        String[] splitUrl = species.getExampleImageFlower().getRawURL().replace("/species",
                "species").split("\\?");
        holder.image.setImageDrawable(getDrawableFromUrl(splitUrl[0]));

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.species_list_section_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.section_header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        //set header text as first char in name
        String headerText = "" + mSpeciesList.get(position).getCommomName().subSequence(0, 1).charAt(0);
        holder.text.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        return mSpeciesList.get(position).getCommomName().subSequence(0, 1).charAt(0);
    }

    private Drawable getDrawableFromUrl(String url) {
        InputStream ims = null;
        try {
            ims = mContext.getAssets().open(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Drawable.createFromStream(ims, null);
    }

    public LayoutInflater getInflater() {
        if (mInflater == null) {
            mInflater = ((Activity) mContext).getLayoutInflater();
        }
        return mInflater;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new SpeciesFilter();

        return mFilter;
    }

    @Override
    public int getCount() {
        return mSpeciesList.size();
    }

    static class HeaderViewHolder {
        TextView text;
    }

    static class ViewHolder {
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
