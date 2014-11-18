package edu.maryland.leafsnap.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import edu.maryland.leafsnap.activity.SpeciesActivity;
import edu.maryland.leafsnap.adapter.SpeciesListAdapter;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.Species;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BrowseFragment extends Fragment {

    private EditText mSearchSpecies;
    private StickyListHeadersListView mSpeciesList;

    private ArrayList<Species> mAllSpecies;
    private SpeciesListAdapter mSpeciesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        mAllSpecies = new ArrayList<Species>();
        mSpeciesAdapter = new SpeciesListAdapter(getActivity(), mAllSpecies);

        mSearchSpecies = (EditText) getActivity().findViewById(R.id.search_species);
        mSearchSpecies.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSpeciesAdapter.getFilter().filter(s);
            }
        });

        mSpeciesList = (StickyListHeadersListView)
                getActivity().findViewById(R.id.species_list);
        mSpeciesList.setAdapter(mSpeciesAdapter);

        mSpeciesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SpeciesActivity.class);
                Bundle args = new Bundle();
                args.putSerializable(SpeciesActivity.ARG_SPECIES, mSpeciesAdapter.getItem(position));
                intent.putExtras(args);
                getActivity().startActivity(intent);
                mSearchSpecies.setText("");
                mSearchSpecies.clearFocus();
            }
        });

        new PopulateSpeciesListTask().execute();
    }

    private class PopulateSpeciesListTask extends AsyncTask<Void, Void, ArrayList<Species>> {

        private DatabaseHelper mDbHelper;

        @Override
        protected ArrayList<Species> doInBackground(Void... params) {
            ArrayList<Species> speciesList = null;
            try {
                speciesList = (ArrayList<Species>) getDbHelper().getSpeciesDao().queryForAll();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return speciesList;
        }

        @Override
        protected void onPostExecute(ArrayList<Species> result) {
            if (result != null) {
                Collections.sort(result);
                mAllSpecies.clear();
                mAllSpecies.addAll(result);
                mSpeciesAdapter.notifyDataSetChanged();
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
