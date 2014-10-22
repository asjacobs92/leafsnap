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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.util.MediaUtils;

/**
 * TODO
 * @author Arthur Jacobs
 */
public class CollectionListAdapter extends ArrayAdapter<CollectedLeaf> {

    private LayoutInflater mInflater;
    private ArrayList<CollectedLeaf> mCollectionList;

    public CollectionListAdapter(Context context, ArrayList<CollectedLeaf> collectionList) {
        super(context, R.layout.list_item, collectionList);
        mCollectionList = collectionList;
    }

    @Override
    public CollectedLeaf getItem(int position) {
        return mCollectionList.get(position);
    }

    @Override
    public int getCount() {
        return mCollectionList.size();
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

        CollectedLeaf collectedLeaf = getItem(position);
        if (collectedLeaf.getSelectedSpeciesRel() != null) {
            holder.text.setText(collectedLeaf.getSelectedSpeciesRel().getScientificName());
        } else {
            holder.text.setText(getContext().getString(R.string.unlabeled_species));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = collectedLeaf.getCollectedDate();
        holder.subtext.setText(getContext().getString(R.string.collected) + " " +
                dateFormat.format(date));
        holder.image.setImageDrawable(MediaUtils.getDrawableFromExternalStorage(getContext(),
                collectedLeaf.getOriginalImageURL().getRawURL()));

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
        ImageView image;
    }


}
