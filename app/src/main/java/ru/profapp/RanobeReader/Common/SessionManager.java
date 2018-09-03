package ru.profapp.RanobeReader.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.json.JSONException;
import org.json.JSONObject;

import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;

public class SessionManager {
    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private Editor editor;

    public SessionManager(Context context) {
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(StringResources.Rulate_Login_Pref, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session *
     **/
    public String[] createRulateLoginSession(String name, String password) {
        try {
            String response = JsonRulateApi.getInstance().Login(name, password);

            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.get("status").equals("success")) {
                return new String[]{"true", jsonObject.get("msg").toString(),
                        jsonObject.getJSONObject("response").get("token").toString()};
            }

            return new String[]{"false", jsonObject.get("msg").toString()};

        } catch (JSONException e) {

            return new String[]{"false", "Response error"};
        } catch (ErrorConnectionException e) {
            return new String[]{"false", "Connection error"};
        }

    }

    public String[] createRanobeRfLoginSession(String name, String password) {
        try {
            String response = JsonRanobeRfApi.getInstance().Login(name, password);

            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 200) {
                return new String[]{"true", jsonObject.get("message").toString(),
                        jsonObject.getJSONObject("result").get("token").toString()};
            }

            return new String[]{"false", jsonObject.get("message").toString()};

        } catch (JSONException e) {

            return new String[]{"false", "Response error"};
        } catch (ErrorConnectionException e) {
            return new String[]{"false", "Connection error"};
        }

    }

}