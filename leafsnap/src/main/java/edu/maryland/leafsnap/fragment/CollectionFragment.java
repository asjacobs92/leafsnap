package edu.maryland.leafsnap.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.activity.CollectedLeafActivity;
import edu.maryland.leafsnap.adapter.CollectionListAdapter;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.util.SessionManager;

public class CollectionFragment extends Fragment {

    private SessionManager mSessionManager;

    private View mCollectionView;
    private View mEmptyCollectionView;

    private TextView mCollectionHeader;
    private CollectionListAdapter mCollectionAdapter;
    private ArrayList<CollectedLeaf> mCollectedSpecies;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collection, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCollectionView = getActivity().findViewById(R.id.collection_view);
        mEmptyCollectionView = getActivity().findViewById(R.id.empty_collection_view);
        mCollectionHeader = (TextView) getActivity().findViewById(R.id.collection_list_header);

        mCollectedSpecies = new ArrayList<CollectedLeaf>();
        mCollectionAdapter = new CollectionListAdapter(getActivity(), mCollectedSpecies);

        final ListView mCollectionList = (ListView) getActivity().findViewById(R.id.collection_list);
        mCollectionList.setAdapter(mCollectionAdapter);
        mCollectionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), CollectedLeafActivity.class);
                Bundle args = new Bundle();
                args.putSerializable(CollectedLeafActivity.ARG_COLLECTED_LEAF, mCollectedSpecies.get(position));
                intent.putExtras(args);
                getActivity().startActivity(intent);
            }
        });

        final Button editButton = (Button) getActivity().findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCollectionAdapter.isActionButtonVisible()) {
                    mCollectionAdapter.setActionButtonVisible(false);
                    editButton.setBackgroundResource(R.drawable.header_button_shape);
                } else {
                    mCollectionAdapter.setActionButtonVisible(true);
                    editButton.setBackgroundResource(R.drawable.header_button_shape_toggled);
                }
                mCollectionAdapter.notifyDataSetChanged();
            }
        });

        new PopulateCollectionListTask().execute();
    }

    private void setFragmentView() {
        if (hasUserCollected()) {
            mCollectionView.setVisibility(View.VISIBLE);
            mEmptyCollectionView.setVisibility(View.GONE);
            mCollectionHeader.setText(getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME) +
                    getActivity().getString(R.string.user_collection));
        } else {
            mCollectionView.setVisibility(View.GONE);
            mEmptyCollectionView.setVisibility(View.VISIBLE);
        }
    }

    private boolean hasUserCollected() {
        return getSessionManager().isLoggedIn() && mCollectedSpecies != null
                && !mCollectedSpecies.isEmpty();
    }

    private SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(getActivity());
        }
        return mSessionManager;
    }

    private class PopulateCollectionListTask extends AsyncTask<Void, Void, ArrayList<CollectedLeaf>> {

        private DatabaseHelper mDbHelper;

        @Override
        protected ArrayList<CollectedLeaf> doInBackground(Void... params) {
            ArrayList<CollectedLeaf> collectionList = null;
            try {
                collectionList = (ArrayList<CollectedLeaf>)
                        getDbHelper().getCollectedLeafDao().queryForAll();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return collectionList;
        }

        @Override
        protected void onPostExecute(ArrayList<CollectedLeaf> result) {
            if (result != null) {
                mCollectedSpecies.clear();
                mCollectedSpecies.addAll(result);
                mCollectionAdapter.notifyDataSetChanged();
                setFragmentView();
            }

            if (mDbHelper != null) {
                OpenHelperManager.releaseHelper();
                mDbHelper = null;
            }
        }

        private DatabaseHelper getDbHelper() {
            if (mDbHelper == null) {
                mDbHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
            }
            return mDbHelper;
        }
    }
}
