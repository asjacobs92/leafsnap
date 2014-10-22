package edu.maryland.leafsnap.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.HashMap;

import edu.maryland.leafsnap.R;
import edu.maryland.leafsnap.api.LeafletUserCollectionRequest;
import edu.maryland.leafsnap.api.LeafletUserRegistrationRequest;
import edu.maryland.leafsnap.data.DatabaseHelper;
import edu.maryland.leafsnap.model.CollectedLeaf;
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

    private AccountAction mAccountAction;
    private SessionManager mSessionManager;

    private DatabaseHelper mDbHelper;
    private LeafletUserRegistrationRequest mUserRequest;
    private LeafletUserCollectionRequest mCollectionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_account_action);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        EditText usernameEditText = (EditText) findViewById(R.id.username_input);
        EditText passwordEditText = (EditText) findViewById(R.id.password_input);
        usernameEditText.setText(getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME));
        passwordEditText.setText(getSessionManager().getCurrentUser().get(SessionManager.KEY_PASSWORD));
    }

    private void setupAccountActionView(String accountActionText, String actionButtonText) {
        TextView accountActionView = (TextView) findViewById(R.id.account_action);
        accountActionView.setText(accountActionText);
        Button accountActionButton = (Button) findViewById(R.id.action_button);
        accountActionButton.setText(actionButtonText);
    }

    public void onActionButtonClick(View view) {
        HashMap<String, String> user = getUserFromInput();
        mUserRequest = new LeafletUserRegistrationRequest(this);
        if (user != null) {
            switch (mAccountAction) {
                case CREATE:
                    mUserRequest.registerAccount(user.get(KEY_USERNAME), user.get(KEY_PASSWORD));
                    break;
                case UPDATE:
                    if (mUserRequest.isAccountRegistered()) {
                        mUserRequest.updateAccount(user.get(KEY_USERNAME), user.get(KEY_PASSWORD));
                    } else {
                        Toast.makeText(this, R.string.login_warning, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case VERIFY:
                    mUserRequest.verifyAccount(user.get(KEY_USERNAME), user.get(KEY_PASSWORD));
                    break;
                default:
                    break;
            }
            new AccountActionTask().execute();
        } else {
            Toast.makeText(this, R.string.fill_fields_warning, Toast.LENGTH_SHORT).show();
        }
    }

    private HashMap<String, String> getUserFromInput() {
        EditText usernameEditText = (EditText) findViewById(R.id.username_input);
        EditText passwordEditText = (EditText) findViewById(R.id.password_input);
        Editable username = usernameEditText.getText();
        Editable password = passwordEditText.getText();
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

    private void displayAccountActionResult() {
        ImageView accountStatusCheckmark = (ImageView) findViewById(R.id.account_status_checkmark);
        TextView accountStatusView = (TextView) findViewById(R.id.account_status);

        accountStatusCheckmark.setVisibility(View.VISIBLE);
        accountStatusView.setVisibility(View.VISIBLE);

        accountStatusView.setText(mUserRequest.getResponseMessage());
        accountStatusCheckmark.setImageDrawable(getResources().getDrawable(
                mUserRequest.wasSuccessful() ? R.drawable.check_right : R.drawable.check_wrong));

        if (mAccountAction == AccountAction.VERIFY
                && mUserRequest.wasSuccessful()) {
            syncUserCollection();
        }
    }

    private void syncUserCollection() {
        TextView collectionStatusView = (TextView) findViewById(R.id.collection_status);
        collectionStatusView.setVisibility(View.VISIBLE);
        collectionStatusView.setText(getResources().getText(R.string.collection_sync_progress));

        deleteLocalCollection();
        String username = getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME);
        mCollectionRequest = new LeafletUserCollectionRequest(this);
        mCollectionRequest.updateUserCollectionSyncStatus(username);
        new CollectionSyncTask().execute();
    }

    private void deleteLocalCollection() {
        try {
            TableUtils.clearTable(getDbHelper().getConnectionSource(), CollectedLeaf.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayCollectionSyncResult() {
        ImageView collectionStatusCheckmark = (ImageView) findViewById(R.id.collection_status_checkmark);
        TextView collectionStatusView = (TextView) findViewById(R.id.collection_status);

        collectionStatusCheckmark.setVisibility(View.VISIBLE);
        collectionStatusView.setVisibility(View.VISIBLE);

        collectionStatusView.setText(getResources().getText(R.string.collection_sync_success));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(this);
        }
        return mSessionManager;
    }

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
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

        if (mCollectionRequest != null) {
            mCollectionRequest.close();
            mCollectionRequest = null;
        }
    }

    public enum AccountAction {
        CREATE, UPDATE, VERIFY
    }

    private class AccountActionTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            while (!mUserRequest.isFinished()) {
                SystemClock.sleep(100);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayAccountActionResult();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    private class CollectionSyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            while (!mCollectionRequest.isFinished()) {
                SystemClock.sleep(100);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayCollectionSyncResult();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setSupportProgressBarIndeterminateVisibility(false);
        }
    }
}
