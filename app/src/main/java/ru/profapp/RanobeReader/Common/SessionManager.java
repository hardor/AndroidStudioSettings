package ru.profapp.RanobeReader.Common;

/**
 * Created by Ruslan on 08.02.2018.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import ru.profapp.RanobeReader.Rulate.JsonRulateApi;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Constructor
    public SessionManager() {

    }

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(StringResources.Rulate_Login_Pref, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public String[] createLoginSession(String name, String password) {
        try {
            String response = JsonRulateApi.getInstance().Login(name, password);

            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.get("status").equals("success")) {
                    return new String[]{"true", jsonObject.get("msg").toString(), jsonObject.get("token").toString()};
                }

                return new String[]{"false", jsonObject.get("msg").toString()};

            } catch (JSONException e) {
                return new String[]{"false", "Response error"};
            }

        } catch (IOException e) {
            return new String[]{"false", "Connection error"};
        }
    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(StringResources.KEY_Login, pref.getString(StringResources.KEY_Login, null));

        // user email id
        user.put(StringResources.KEY_Password, pref.getString(StringResources.KEY_Password, null));

        // return user
        return user;
    }

    /**
     * /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.contains(StringResources.KEY_Token);
    }
}