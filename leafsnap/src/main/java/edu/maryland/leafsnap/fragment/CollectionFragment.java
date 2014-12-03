package edu.maryland.leafsnap.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.activity.CollectedLeafActivity;
import edu.maryland.leafsnap.adapter.CollectionListAdapter;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.CollectedLeaf;
import edu.maryland.leafsnap.util.SessionManager;

public class CollectionFragment extends Fragment {

    private SessionManager mSessionManager;

    private View mCollectionView;
    private View mMapCollectionView;
    private View mListCollectionView;
    private View mEmptyCollectionView;

    private MapView mapView;
    private GoogleMap mCollectionMap;
    private TextView mCollectionHeader;
    private CollectionListAdapter mCollectionAdapter;
    private ArrayList<CollectedLeaf> mCollectedSpecies;

    private HashMap<String, CollectedLeaf> mMarkerLeafMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_collection, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mCollectionMap = mapView.getMap();
        mCollectionMap.getUiSettings().setMyLocationButtonEnabled(false);
        mCollectionMap.setMyLocationEnabled(true);
        mCollectionMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            final View view = inflater.inflate(R.layout.marker_info_window, null, false);

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                final CollectedLeaf leaf = mMarkerLeafMap.get(marker.getId());

                if (leaf != null) {
                    String label = getActivity().getString(R.string.unlabeled_species);
                    if (leaf.getSelectedSpeciesRel() != null) {
                        label = leaf.getSelectedSpeciesRel().getScientificName();
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = leaf.getCollectedDate();

                    TextView title = (TextView) view.findViewById(R.id.window_title);
                    title.setText(label);

                    TextView snippet = (TextView) view.findViewById(R.id.window_snippet);
                    snippet.setText(getActivity().getString(R.string.collected) + " " +
                            dateFormat.format(date));
                }

                return view;
            }
        });

        mCollectionMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final CollectedLeaf leaf = mMarkerLeafMap.get(marker.getId());
                if (leaf != null) {
                    Log.d("TAAAG", leaf.getLeafID() + "");
                    Intent intent = new Intent(getActivity(), CollectedLeafActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable(CollectedLeafActivity.ARG_COLLECTED_LEAF, leaf);
                    intent.putExtras(args);
                    getActivity().startActivity(intent);
                }
                else {
                    Log.d("TAAAG", "Something is wrong " + marker.getId() + marker.getTitle() + marker.getSnippet());
                }
            }
        });

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        mCollectionView = getActivity().findViewById(R.id.collection_view);
        mMapCollectionView = getActivity().findViewById(R.id.map_collection_view);
        mListCollectionView = getActivity().findViewById(R.id.list_collection_view);
        mEmptyCollectionView = getActivity().findViewById(R.id.empty_collection_view);
        mCollectionHeader = (TextView) getActivity().findViewById(R.id.collection_list_header);

        mCollectedSpecies = new ArrayList<CollectedLeaf>();
        mCollectionAdapter = new CollectionListAdapter(this, mCollectedSpecies);

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

        Button editButton = (Button) getActivity().findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditButton();
            }
        });

        Switch mapSwitch = (Switch) getActivity().findViewById(R.id.map_switch);
        mapSwitch.setChecked(false);
        mapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new PopulateCollectionMapTask().execute();
                    mMapCollectionView.setVisibility(View.VISIBLE);
                    mListCollectionView.setVisibility(View.GONE);
                } else {
                    mMapCollectionView.setVisibility(View.GONE);
                    mListCollectionView.setVisibility(View.VISIBLE);
                }
            }
        });

        new PopulateCollectionListTask().execute();
    }

    public void toggleEditButton() {
        Button editButton = (Button) getActivity().findViewById(R.id.edit_button);
        if (mCollectionAdapter.isActionButtonVisible()) {
            mCollectionAdapter.setActionButtonVisible(false);
            editButton.setBackgroundResource(R.drawable.header_button_shape);
        } else {
            mCollectionAdapter.setActionButtonVisible(true);
            editButton.setBackgroundResource(R.drawable.header_button_shape_toggled);
        }
        mCollectionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void setFragmentView() {
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

    private class PopulateCollectionMapTask extends AsyncTask<Void, Void, HashMap<MarkerOptions, CollectedLeaf>> {

        @Override
        protected HashMap<MarkerOptions, CollectedLeaf> doInBackground(Void... params) {
            HashMap<MarkerOptions, CollectedLeaf> markersOptions = new HashMap<MarkerOptions, CollectedLeaf>();
            if (mCollectedSpecies != null && !mCollectedSpecies.isEmpty()) {
                for (CollectedLeaf leaf : mCollectedSpecies) {
                    if (leaf.getLatitude() != null && leaf.getLongitude() != null) {
                        LatLng pos = new LatLng(Double.valueOf(leaf.getLatitude()), Double.valueOf(leaf.getLongitude()));
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(pos).anchor(0, 1);

                        markersOptions.put(markerOptions, leaf);
                    }
                }
            }

            return markersOptions;
        }

        @Override
        protected void onPostExecute(HashMap<MarkerOptions, CollectedLeaf> result) {
            mCollectionMap.clear();
            if (!result.isEmpty()) {
                mMarkerLeafMap = new HashMap<String, CollectedLeaf>();
                for (Map.Entry<MarkerOptions, CollectedLeaf> entry : result.entrySet()) {
                    mMarkerLeafMap.put(mCollectionMap.addMarker(entry.getKey()).getId(), entry.getValue());
                }
                MarkerOptions mrkOpt = (MarkerOptions) result.keySet().toArray()[0];
                // Updates the location and zoom of the MapView
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mrkOpt.getPosition(), 10);
                mCollectionMap.animateCamera(cameraUpdate);
            }
        }
    }
}
