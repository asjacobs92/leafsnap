package edu.maryland.leafsnap.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.activity.SpeciesActivity;
import edu.maryland.leafsnap.model.Species;

public class SpeciesDetailsFragment extends Fragment {

    private Species mSpecies;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle b = this.getArguments();
        if (b != null) {
            mSpecies = (Species) b.getSerializable(SpeciesActivity.ARG_SPECIES);
        }
        return inflater.inflate(edu.maryland.leafsnap.R.layout.fragment_species_details, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSpecies != null) {
            setCommomNameValue();
            setScientificNameValue();
            setDescriptionValue();
            setHabitatValue();
            setGrowthValue();
            setBloomValue();
            setLongevityValue();
            setPresenceValue();
        }
    }

    private void setCommomNameValue() {
        TextView commomNameView = (TextView) getActivity().findViewById(R.id.commom_name);
        commomNameView.setText(mSpecies.getCommomName());
    }

    private void setScientificNameValue() {
        TextView scientificNameView = (TextView) getActivity().findViewById(R.id.scientific_name);
        scientificNameView.setText(mSpecies.getScientificName());
    }

    private void setDescriptionValue() {
        TextView descriptionView = (TextView) getActivity().findViewById(R.id.description);
        descriptionView.setText(mSpecies.getDescription());
    }

    private void setHabitatValue() {
        if (mSpecies.getHabitat() != null) {
            setParamValue(getActivity().getResources().getString(R.string.species_habitat), mSpecies.getHabitat());
        }
    }

    private void setGrowthValue() {
        if (mSpecies.getGrowth() != null) {
            setParamValue(getActivity().getResources().getString(R.string.species_growth), mSpecies.getGrowth());
        }
    }

    private void setBloomValue() {

        if (mSpecies.getBloom() != null) {
            setParamValue(getActivity().getResources().getString(R.string.species_bloom), mSpecies.getBloom());
        }
    }

    private void setLongevityValue() {
        if (mSpecies.getLongevity() != null) {
            setParamValue(getActivity().getResources().getString(R.string.species_longevity), mSpecies.getLongevity());
        }
    }

    private void setPresenceValue() {
        if (mSpecies.getPresence() != null) {
            setParamValue(getActivity().getResources().getString(R.string.species_presence), mSpecies.getPresence());
        }
    }

    private void setParamValue(String param, String content) {
        String text = "<font color=#FFFFFF><b>" + param +
                "</b></font> <font color=#DBD6D2>" + content + "</font>";

        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.details_layout);
        TextView textView = new TextView(getActivity());
        textView.setText(Html.fromHtml(text));
        textView.setPadding(getActivity().getResources().getDimensionPixelSize(R.dimen.species_details_padding_left),
                getActivity().getResources().getDimensionPixelSize(R.dimen.species_details_padding_top), 0, 0);
        textView.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
        layout.addView(textView);
    }
}
