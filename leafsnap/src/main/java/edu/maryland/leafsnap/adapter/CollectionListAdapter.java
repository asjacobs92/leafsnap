package edu.maryland.leafsnap.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.activity.CollectedLeafActivity;
import edu.maryland.leafsnap.api.LeafletPhotoUploader;
import edu.maryland.leafsnap.api.LeafletUserCollectionRequest;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.fragment.CollectionFragment;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.model.Species;
import edu.maryland.leafsnap.util.MediaUtils;

/**
 * TODO
 *
 * @author Arthur Jacobs
 */
public class CollectionListAdapter extends ArrayAdapter<CollectedLeaf> {

    private boolean mActionButtonVisible = false;
    private LayoutInflater mInflater;
    private CollectionFragment mFragment;
    private ArrayList<CollectedLeaf> mCollectionList;

    public CollectionListAdapter(CollectionFragment fragment, ArrayList<CollectedLeaf> collectionList) {
        super(fragment.getActivity(), R.layout.list_item, collectionList);
        mFragment = fragment;
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.item_text);
            holder.subtext = (TextView) convertView.findViewById(R.id.item_subtext);
            holder.image = (ImageView) convertView.findViewById(R.id.item_image);
            holder.actionButton = (Button) convertView.findViewById(R.id.item_action_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CollectedLeaf collectedLeaf = mCollectionList.get(position);
        if (collectedLeaf.getSelectedSpeciesRel() != null) {
            holder.text.setText(collectedLeaf.getSelectedSpeciesRel().getScientificName());
        } else {
            holder.text.setText(getContext().getString(R.string.unlabeled_species));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = collectedLeaf.getCollectedDate();
        holder.subtext.setText(getContext().getString(R.string.collected) + " " +
                dateFormat.format(date));
        new LoadItemImageTask(collectedLeaf, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        holder.actionButton.setText(getContext().getString(R.string.delete));
        holder.actionButton.setBackgroundResource(R.drawable.delete_button_shape);
        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.delete)
                    .setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DeleteImageTask(mCollectionList.get(position)).execute();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
            }
        });

        if (isActionButtonVisible()) {
            holder.actionButton.setVisibility(View.VISIBLE);
        } else {
            holder.actionButton.setVisibility(View.GONE);
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
        ImageView image;
        Button actionButton;
    }

    private class DeleteImageTask extends AsyncTask<Void, Void, Boolean> {

        private DatabaseHelper mDbHelper;
        private CollectedLeaf mCollectedLeaf;

        public DeleteImageTask(CollectedLeaf leaf) {
            super();
            mCollectedLeaf = leaf;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final LeafletPhotoUploader photoUploader =
                    new LeafletPhotoUploader(getContext(), mCollectedLeaf);
            photoUploader.deleteCollectedLeaf();
            while (!photoUploader.isFinished()) {
                SystemClock.sleep(100);
            }
            photoUploader.close();

            return photoUploader.wasSuccessful();
        }

        @Override
        protected void onPostExecute(Boolean deleteSuccessful) {
            if (deleteSuccessful) {
                try {
                    mCollectionList.remove(mCollectionList.indexOf(mCollectedLeaf));
                    notifyDataSetChanged();
                    mFragment.setFragmentView();
                    mFragment.toggleEditButton();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getContext(), R.string.deletion_error, Toast.LENGTH_SHORT).show();
            }

            if (mDbHelper != null) {
                OpenHelperManager.releaseHelper();
                mDbHelper = null;
            }
        }

        private DatabaseHelper getDbHelper() {
            if (mDbHelper == null) {
                mDbHelper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
            }
            return mDbHelper;
        }
    }

    private class LoadItemImageTask extends AsyncTask<Void, Void, Drawable> {

        private CollectedLeaf mCollectedLeaf;
        private ViewHolder mHolder;

        public LoadItemImageTask(CollectedLeaf collectedLeaf, ViewHolder holder) {
            mCollectedLeaf = collectedLeaf;
            mHolder = holder;
        }

        @Override
        protected Drawable doInBackground(Void... arg0) {
            return MediaUtils.getDrawableFromExternalStorage(getContext(),
                    mCollectedLeaf.getOriginalImageURL().getRawURL());
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (drawable != null) {
                mHolder.image.setImageDrawable(drawable);
            }
        }
    }
}
