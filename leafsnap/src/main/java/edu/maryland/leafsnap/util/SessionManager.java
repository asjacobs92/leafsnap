package edu.maryland.leafsnap.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

/**
 * TODO: comment this.
 * <p/>
 * Created by Arthur Jacobs on 25/06/2014.
 */
public class SessionManager {

    // User name (make variable public to access from outside)
    public static final String KEY_USERNAME = "username";
    // User password (make variable public to access from outside)
    public static final String KEY_PASSWORD = "password";
    // Sharedpref file name
    private static final String PREF_NAME = "LeafsnapPref";
    // All Shared Preferences Keys
    private static final String IS_LOGIN = "isLoggedIn";
    // Shared pref mode
    private static final int PRIVATE_MODE = 0;

    private Editor mEditor;
    private Context mContext;
    private SharedPreferences mPref;

    public SessionManager(Context context) {
        setContext(context);
    }

    /**
     * Create login session
     */
    public void loginUser(String name, String password) {
        if (name != null && password != null) {
            if (!name.isEmpty() && !password.isEmpty()) {
                // Storing login value as TRUE
                getEditor().putBoolean(IS_LOGIN, true);

                // Storing name in pref
                getEditor().putString(KEY_USERNAME, name);
                getEditor().putString(KEY_PASSWORD, password);

                // commit changes
                getEditor().commit();
            }
        }
    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getCurrentUser() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_USERNAME, getSharedPreferences().getString(KEY_USERNAME, null));
        user.put(KEY_PASSWORD, getSharedPreferences().getString(KEY_PASSWORD, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        getEditor().clear();
        getEditor().commit();
    }

    /**
     * Quick check for login
     * *
     */
    public boolean isLoggedIn() {
        return getSharedPreferences().getBoolean(IS_LOGIN, false);
    }

    public SharedPreferences getSharedPreferences() {
        if (mPref == null) {
            mPref = getContext().getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        }
        return mPref;
    }

    public Editor getEditor() {
        if (mEditor == null) {
            mEditor = getSharedPreferences().edit();
        }
        return mEditor;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }
}
