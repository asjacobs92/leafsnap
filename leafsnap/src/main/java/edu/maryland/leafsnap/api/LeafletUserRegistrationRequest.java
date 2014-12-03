package edu.maryland.leafsnap.api;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import edu.maryland.leafsnap.util.SessionManager;

/**
 * TODO: comment this.
 * <p/>
 * <p/>
 * Created by Arthur Jacobs on 03/07/2014.
 */
public class LeafletUserRegistrationRequest {
    private final static String ACCOUNT_DEFAULT_FAIL_MSG = "Account operation failed," +
            " most likely due to lack of connectivity.";
    private final static String ACCOUNT_CREATE_SUCCESS = "Account successfully created.";

    private boolean mFinished;
    private boolean mSuccessful;
    private String mResponseMessage;

    private Context mContext;
    private SessionManager mSessionManager;

    public LeafletUserRegistrationRequest(Context context) {
        mContext = context;
    }

    public boolean isAccountRegistered() {
        if (getSessionManager().isLoggedIn()) {
            HashMap<String, String> user = getSessionManager().getCurrentUser();
            String username = user.get(SessionManager.KEY_USERNAME);
            String password = user.get(SessionManager.KEY_PASSWORD);
            if (username == null || password == null) {
                return false;
            } else {
                if (username.isEmpty() || password.isEmpty()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    public void registerAccount(final String username, final String password) {
        RequestParams params = new RequestParams();
        params.put("fmt", "json");
        if (username == null || password == null) {
            params.put("auto", 1);
        } else {
            if (username.isEmpty() || password.isEmpty()) {
                params.put("auto", 1);
            } else {
                params.put("auto", 0);
            }
        }

        params.put("user", username);
        params.put("passwd", password);
        LeafletRestClient.post("/register/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                int successCode = 0;
                String responseMsg = "";
                try {
                    successCode = response.getInt("success");
                    if (successCode == 1) {
                        responseMsg = ACCOUNT_CREATE_SUCCESS;
                        String resultUsername = response.getString("user");
                        String resultPassword = response.getString("passwd");

                        getSessionManager().logoutUser();
                        getSessionManager().loginUser(resultUsername, resultPassword);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setSuccessful((successCode == 1));
                setResponseMessage(responseMsg);
                Log.d("CREATE ACCOUNT", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                setSuccessful(false);
                setResponseMessage(ACCOUNT_DEFAULT_FAIL_MSG);
                Log.d("CREATE ACCOUNT", ACCOUNT_DEFAULT_FAIL_MSG);
            }
        });
        setFinished(true);
    }

    public void updateAccount(final String newUsername, final String newPassword) {
        RequestParams params = new RequestParams();
        params.put("fmt", "json");
        params.put("olduser", getSessionManager().getCurrentUser().get(SessionManager.KEY_USERNAME));
        params.put("oldpasswd", getSessionManager().getCurrentUser().get(SessionManager.KEY_PASSWORD));
        params.put("user", newUsername);
        params.put("passwd", newPassword);
        Log.d("TAG", newUsername + " " + newPassword);
        LeafletRestClient.post("/updatelogin/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("TAG", response.toString());
                int successCode = 0;
                String responseMsg = "";
                try {
                    successCode = response.getInt("success");
                    responseMsg = response.getString("msg");
                    if (successCode == 1) {
                        getSessionManager().logoutUser();
                        getSessionManager().loginUser(newUsername, newPassword);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setSuccessful((successCode == 1));
                setResponseMessage(responseMsg);
                Log.d("UPDATE ACCOUNT", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                setSuccessful(false);
                setResponseMessage(ACCOUNT_DEFAULT_FAIL_MSG);
                Log.d("UPDATE ACCOUNT", ACCOUNT_DEFAULT_FAIL_MSG);
            }
        });
        setFinished(true);
    }

    public void verifyAccount(final String username, final String password) {
        RequestParams params = new RequestParams();
        params.put("fmt", "json");
        params.put("user", username);
        params.put("passwd", password);
        LeafletRestClient.post("/login/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                int successCode = 0;
                String responseMsg = "";
                try {
                    successCode = response.getInt("success");
                    responseMsg = response.getString("msg");
                    if (successCode == 1) {
                        getSessionManager().logoutUser();
                        getSessionManager().loginUser(username, password);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setSuccessful((successCode == 1));
                setResponseMessage(responseMsg);
                Log.d("VERIFY ACCOUNT", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                setSuccessful(false);
                setResponseMessage(ACCOUNT_DEFAULT_FAIL_MSG);
                Log.d("VERIFY ACCOUNT", ACCOUNT_DEFAULT_FAIL_MSG);
            }
        });
        setFinished(true);
    }

    public boolean wasSuccessful() {
        return mSuccessful;
    }

    public void setSuccessful(boolean mSuccessful) {
        this.mSuccessful = mSuccessful;
    }

    public boolean isFinished() {
        return mFinished;
    }

    public void setFinished(boolean finished) {
        this.mFinished = finished;
    }

    public String getResponseMessage() {
        return mResponseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.mResponseMessage = responseMessage;
    }

    public SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(mContext);
        }
        return mSessionManager;
    }
}
