package ru.profapp.RanobeReader.CustomElements;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ru.profapp.RanobeReader.Common.SessionManager;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.R;

public final class RanoberfLoginPreference extends DialogPreference implements
        DialogInterface.OnClickListener {

    private SessionManager session;
    private SharedPreferences sharedPref;
    // Current value
    private String mCurrentValue;
    // View elements
    private EditText mLoginEditor;
    private EditText mPasswordEditor;

    public RanoberfLoginPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText("Login");
        setNegativeButtonText("Cancel");
        setDialogIcon(R.mipmap.ic_action_ranoberf);
    }

    @Override
    protected View onCreateDialogView() {

        // Inflate layout
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_login, null);

        Context context = this.getContext();
        sharedPref = context.getSharedPreferences(StringResources.Ranoberf_Login_Pref,
                Context.MODE_PRIVATE);

        session = new SessionManager(context);

        String value = sharedPref.getString(StringResources.KEY_Login, "");

        // Setup SeekBar

        mLoginEditor = view.findViewById(R.id.login_login);
        mLoginEditor.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mLoginEditor.setHint(getContext().getResources().getString(R.string.email));
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
        }
    }

    private void auth() {

        String username = mLoginEditor.getText().toString();
        String password = mPasswordEditor.getText().toString();

        sharedPref.edit().putString(StringResources.KEY_Login, username).commit();

        AlertDialog alert = new AlertDialog.Builder(getContext()).create();
        // Check if username, password is filled
        if (username.trim().length() > 0 && password.trim().length() > 0) {
            String[] result = session.createRanobeRfLoginSession(username, password);
            Boolean resBool = Boolean.valueOf(result[0]);
            if (resBool) {
                sharedPref.edit().putString(StringResources.KEY_Token, result[2]).commit();
                setSummary(username);
                RanobeKeeper.getInstance().setRanobeRfToken(
                        result[2]);
                alert.setMessage(getContext().getString(R.string.auth_succes));
            } else {
                sharedPref.edit().putString(StringResources.KEY_Token, "").commit();
                setSummary(getContext().getString(R.string.login_to_summary));

                alert.setMessage(getContext().getString(R.string.auth_error));
            }

            alert.setTitle(username);

            alert.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            });
            alert.show();

        } else {

            alert.setTitle(getContext().getString(R.string.login_failed));
            alert.setMessage(getContext().getString(R.string.enter_user_pass));
            alert.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
                sharedPref.edit().putString(StringResources.KEY_Token, "").commit();

                setSummary(getContext().getString(R.string.login_to_summary));
            });
            alert.setButton(Dialog.BUTTON_NEUTRAL, "Cancel", (dialog, which) -> {

            });
            alert.show();
        }

    }

}