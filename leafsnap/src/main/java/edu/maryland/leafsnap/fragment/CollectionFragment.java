package edu.maryland.leafsnap.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

    private DatabaseHelper mDbHelper;
    private SessionManager mSessionManager;

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
        setFragmentView();

        if (hasUserCollected()) {
            TextView collectionHeader = (TextView) getActivity().findViewById(R.id.collection_list_header);
            collectionHeader.setText(getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME) +
                    getActivity().getString(R.string.user_collection));

            ListView collectionList = (ListView) getActivity().findViewById(R.id.collection_list);
            final ArrayList<CollectedLeaf> collectedSpecies = getCollectionList();
            collectionList.setAdapter(new CollectionListAdapter(getActivity(), collectedSpecies));
            collectionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), CollectedLeafActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable(CollectedLeafActivity.ARG_COLLECTED_LEAF, collectedSpecies.get(position));
                    intent.putExtras(args);
                    getActivity().startActivity(intent);
                }
            });
        }
    }

    private void setFragmentView() {
        if (hasUserCollected()) {
            getActivity().findViewById(R.id.collection_view).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.empty_collection_view).setVisibility(View.GONE);
        } else {
            getActivity().findViewById(R.id.collection_view).setVisibility(View.GONE);
            getActivity().findViewById(R.id.empty_collection_view).setVisibility(View.VISIBLE);
        }
    }

    private boolean hasUserCollected() {
        return getSessionManager().isLoggedIn() && getCollectionList() != null && !getCollectionList().isEmpty();
    }

    private ArrayList<CollectedLeaf> getCollectionList() {
        ArrayList<CollectedLeaf> collectionList = null;
        try {
            collectionList = (ArrayList<CollectedLeaf>) getDbHelper().getCollectedLeafDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return collectionList;
    }

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return mDbHelper;
    }

    private SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(getActivity());
        }
        return mSessionManager;
    }

    @Override
    public void onResume() {
        super.onResume();
        setFragmentView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDbHelper != null) {
            OpenHelperManager.releaseHelper();
            mDbHelper = null;
        }
    }
}
