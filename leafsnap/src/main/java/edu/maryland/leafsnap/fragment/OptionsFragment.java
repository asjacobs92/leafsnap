package edu.maryland.leafsnap.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.activity.OptionsAccountActivity;
import edu.maryland.leafsnap.activity.OptionsDatabaseActivity;
import edu.maryland.leafsnap.util.SessionManager;

public class OptionsFragment extends Fragment {

    private SessionManager mSessionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_options, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        setAccountButtonListener();
        setDatabaseButtonListener();
        setCurrentUsername();
    }

    private void setCurrentUsername() {
        TextView username = (TextView) getActivity().findViewById(R.id.username);
        if (getSessionManager().isLoggedIn()) {
            username.setText(getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME));
        } else {
            username.setText(getResources().getText(R.string.not_logged_in));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setCurrentUsername();
    }

    private void setAccountButtonListener() {
        Button accountButton = (Button) getActivity().findViewById(R.id.account_button);
        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startActivity(new Intent(getActivity(), OptionsAccountActivity.class));
            }
        });
    }

    private void setDatabaseButtonListener() {
        Button databaseButton = (Button) getActivity().findViewById(R.id.database_button);
        databaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startActivity(new Intent(getActivity(), OptionsDatabaseActivity.class));
            }
        });
    }

    public SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(getActivity());
        }
        return mSessionManager;
    }
}
