package edu.maryland.leafsnap.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.activity.SpeciesAcitivity;
import edu.maryland.leafsnap.adapter.SpeciesListAdapter;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.Species;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BrowseFragment extends Fragment {

    private DatabaseHelper mDbHelper;
    private SpeciesListAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        StickyListHeadersListView speciesList = (StickyListHeadersListView)
                getActivity().findViewById(R.id.species_list);
        ArrayList<Species> allSpecies = getSpeciesList();
        if (allSpecies != null) {
            Collections.sort(allSpecies);

            mListAdapter = new SpeciesListAdapter(getActivity(), allSpecies);
            speciesList.setAdapter(mListAdapter);
            final EditText searchSpecies = (EditText) getActivity().findViewById(R.id.search_species);
            searchSpecies.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable arg0) {
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mListAdapter.getFilter().filter(s);
                }
            });

            speciesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), SpeciesAcitivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable(SpeciesAcitivity.ARG_SPECIES, mListAdapter.getItem(position));
                    intent.putExtras(args);
                    getActivity().startActivity(intent);
                    searchSpecies.setText("");
                }
            });
        }
    }

    private ArrayList<Species> getSpeciesList() {
        ArrayList<Species> speciesList = null;
        try {
            speciesList = (ArrayList<Species>) getDbHelper().getSpeciesDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return speciesList;
    }

    public DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return mDbHelper;
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
