package ru.profapp.RanobeReader.CustomElements;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ru.profapp.RanobeReader.Common.SessionManager;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.R;

public final class LoginPreference extends DialogPreference implements DialogInterface.OnClickListener {


    private SharedPreferences.Editor editor;
    private SessionManager session;
    // Current value
    private String mCurrentValue;
    // View elements
    private EditText mLoginEditor;
    private EditText mPasswordEditor;

    public LoginPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText("Login");
        setNegativeButtonText("Cancel");
        setDialogIcon(R.mipmap.rulate);
    }

    @Override
    protected View onCreateDialogView() {

        // Inflate layout
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_login, null);

        Context context = this.getContext();
        SharedPreferences sharedPref = context.getSharedPreferences(StringResources.Rulate_Login_Pref, Context.MODE_PRIVATE);

        session = new SessionManager(context);
        editor = sharedPref.edit();

        String value = sharedPref.getString(StringResources.KEY_Login, "");

        // Setup SeekBar
        mLoginEditor = view.findViewById(R.id.login_login);
        mLoginEditor.setText(value);
        mPasswordEditor = view.findViewById(R.id.login_password);


        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // Return if change was cancelled
        if (!positiveResult) {
            return;
        }

        // Persist current value if needed
        if (shouldPersist()) {
            persistString(mCurrentValue);
        }

        // Notify activity about changes (to update preference summary line)
        notifyChanged();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            auth();
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            // do your stuff to handle negative button
        }
    }

    private void auth() {

        String username = mLoginEditor.getText().toString();
        String password = mPasswordEditor.getText().toString();

        editor.putString(StringResources.KEY_Login, username);
        editor.commit();

        AlertDialog alert = new AlertDialog.Builder(getContext()).create();
        // Check if username, password is filled
        if (username.trim().length() > 0 && password.trim().length() > 0) {
            String[] result = session.createLoginSession(username, password);
            Boolean resBool = Boolean.valueOf(result[0]);
            if (resBool) {
                editor.putString(StringResources.KEY_Token, result[2]);
                editor.commit();
                setSummary(username);
            }else{
                editor.putString(StringResources.KEY_Token, "");
                editor.commit();
                setSummary(getContext().getString(R.string.login_to_rulate_summary));
            }

            alert.setTitle(username);
            alert.setMessage(result[1]);
            alert.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            });
            alert.show();

        } else {
            // user didn't entered username or password
            // Show alert asking him to enter the details


            alert.setTitle(getContext().getString(R.string.login_failed));
            alert.setMessage(getContext().getString(R.string.enter_user_pass));
            alert.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
                editor.putString(StringResources.KEY_Token, "");
                editor.commit();
                setSummary(getContext().getString(R.string.login_to_rulate_summary));
            });
            alert.setButton(Dialog.BUTTON_NEUTRAL, "Cancel", (dialog, which) -> {

            });
            alert.show();
        }

    }

}