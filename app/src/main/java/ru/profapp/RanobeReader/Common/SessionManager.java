package ru.profapp.RanobeReader.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;

public class SessionManager {
    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private Editor editor;

    // Constructor
    public SessionManager() {

    }

    public SessionManager(Context context) {
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(StringResources.Rulate_Login_Pref, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session *
     **/
    public String[] createLoginSession(String name, String password) {
        String response = JsonRulateApi.getInstance().Login(name, password);

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.get("status").equals("success")) {
                return new String[]{"true", jsonObject.get("msg").toString(), jsonObject.getJSONObject("response").get("token").toString()};
            }

            return new String[]{"false", jsonObject.get("msg").toString()};

        } catch (JSONException e) {

            return new String[]{"false", "Response error"};
        }

    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
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