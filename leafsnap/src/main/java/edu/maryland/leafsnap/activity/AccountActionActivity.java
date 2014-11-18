package edu.maryland.leafsnap.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.api.LeafletUserCollectionRequest;
import edu.maryland.leafsnap.api.LeafletUserRegistrationRequest;
import edu.maryland.leafsnap.util.SessionManager;


/**
 * TODO: comment this.
 * <p/>
 * Created by Arthur Jacobs on 03/07/2014.
 */
public class AccountActionActivity extends ActionBarActivity {

    public static final String ACTION_ARG = "account_action";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;

    private TextView mAccountStatusView;
    private ImageView mAccountStatusCheckMark;

    private TextView mCollectionStatusView;
    private ImageView mCollectionStatusCheckMark;

    private Button mAccountActionButton;
    private TextView mAccountActionView;

    private AccountAction mAccountAction;
    private SessionManager mSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_account_action);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsernameEditText = (EditText) findViewById(R.id.username_input);
        mPasswordEditText = (EditText) findViewById(R.id.password_input);

        mAccountActionView = (TextView) findViewById(R.id.account_action);
        mAccountActionButton = (Button) findViewById(R.id.action_button);

        mAccountStatusView = (TextView) findViewById(R.id.account_status);
        mAccountStatusCheckMark = (ImageView) findViewById(R.id.account_status_checkmark);

        mCollectionStatusView = (TextView) findViewById(R.id.collection_status);
        mCollectionStatusCheckMark = (ImageView) findViewById(R.id.collection_status_checkmark);

        mAccountAction = (AccountAction) getIntent().getSerializableExtra(ACTION_ARG);
        switch (mAccountAction) {
            case CREATE:
                setupAccountActionView(getResources().getText(R.string.new_account).toString(),
                        getResources().getText(R.string.create_account).toString());
                break;
            case UPDATE:
                setupAccountActionView(getResources().getText(R.string.change_username).toString(),
                        getResources().getText(R.string.update_account).toString());
                setupCurrentUserInfo();
                break;
            case VERIFY:
                setupAccountActionView(getResources().getText(R.string.different_login).toString(),
                        getResources().getText(R.string.login_account).toString());
                setupCurrentUserInfo();
                break;
            default:
                break;
        }
    }

    private void setupCurrentUserInfo() {
        mUsernameEditText.setText(getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME));
        mPasswordEditText.setText(getSessionManager().getCurrentUser().get(SessionManager.KEY_PASSWORD));
    }

    private void setupAccountActionView(String accountActionText, String actionButtonText) {
        mAccountActionView.setText(accountActionText);
        mAccountActionButton.setText(actionButtonText);
    }

    public void onActionButtonClick(View view) {
        HashMap<String, String> user = getUserFromInput();
        if (user != null) {
            new AccountActionTask().execute(user);
        } else {
            Toast.makeText(this, R.string.fill_fields_warning, Toast.LENGTH_SHORT).show();
        }
    }

    private HashMap<String, String> getUserFromInput() {
        Editable username = ((EditText) findViewById(R.id.username_input)).getText();
        Editable password = ((EditText) findViewById(R.id.password_input)).getText();

        HashMap<String, String> user = null;
        if (username != null && password != null) {
            if (!username.toString().trim().isEmpty() && !password.toString().trim().isEmpty()) {
                user = new HashMap<String, String>();
                user.put(KEY_USERNAME, username.toString().trim());
                user.put(KEY_PASSWORD, password.toString().trim());
            }
        }

        return user;
    }

    private SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(this);
        }
        return mSessionManager;
    }

    public enum AccountAction {
        CREATE, UPDATE, VERIFY
    }

    private class AccountActionTask extends AsyncTask<HashMap<String, String>, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Boolean doInBackground(HashMap<String, String>... user) {
            final LeafletUserRegistrationRequest mUserRequest =
                    new LeafletUserRegistrationRequest(getBaseContext());
            switch (mAccountAction) {
                case CREATE:
                    mUserRequest.registerAccount(user[0].get(KEY_USERNAME), user[0].get(KEY_PASSWORD));
                    break;
                case UPDATE:
                    if (mUserRequest.isAccountRegistered()) {
                        mUserRequest.updateAccount(user[0].get(KEY_USERNAME), user[0].get(KEY_PASSWORD));
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), R.string.login_warning, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    break;
                case VERIFY:
                    mUserRequest.verifyAccount(user[0].get(KEY_USERNAME), user[0].get(KEY_PASSWORD));
                    break;
            }

            while (!mUserRequest.isFinished()) {
                SystemClock.sleep(100);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView accountStatus = (TextView) findViewById(R.id.account_status);
                    accountStatus.setText(mUserRequest.getResponseMessage());
                }
            });

            return mUserRequest.wasSuccessful();
        }

        @Override
        protected void onPostExecute(Boolean wasSuccessful) {
            setSupportProgressBarIndeterminateVisibility(false);

            mAccountStatusView.setVisibility(View.VISIBLE);
            mAccountStatusCheckMark.setVisibility(View.VISIBLE);

            mAccountStatusCheckMark.setImageResource(wasSuccessful ? R.drawable.check_right : R.drawable.check_wrong);

            if (mAccountAction == AccountAction.VERIFY && wasSuccessful) {
                new CollectionSyncTask().execute();
            }
        }
    }

    private class CollectionSyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);
            mCollectionStatusView.setVisibility(View.VISIBLE);
            mCollectionStatusView.setText(getResources().getText(R.string.collection_sync_progress));
        }

        @Override
        protected Void doInBackground(Void... params) {
            LeafletUserCollectionRequest mCollectionRequest =
                    new LeafletUserCollectionRequest(getBaseContext());
            mCollectionRequest.updateUserCollectionSyncStatus();
            while (!mCollectionRequest.isFinished()) {
                SystemClock.sleep(100);
            }
            mCollectionRequest.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setSupportProgressBarIndeterminateVisibility(false);

            mCollectionStatusView.setVisibility(View.VISIBLE);
            mCollectionStatusCheckMark.setVisibility(View.VISIBLE);
            mCollectionStatusView.setText(getResources().getText(R.string.collection_sync_success));
        }
    }
}
