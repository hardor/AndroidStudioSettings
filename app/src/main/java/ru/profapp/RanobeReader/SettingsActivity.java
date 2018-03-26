package ru.profapp.RanobeReader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Common.ThemeUtils;
import ru.profapp.RanobeReader.CustomElements.LoginPreference;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Activity mActivity;
    private static Preference.OnPreferenceChangeListener sChangePreferenceListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {

                    if (preference.getKey().equals(preference.getContext().getString(
                            R.string.pref_general_hide_chapter))) {

                        RanobeKeeper.getInstance().setHideUnavailableChapters(
                                Boolean.valueOf(value.toString()));

                    } else if (preference.getKey().equals(preference.getContext().getString(
                            R.string.pref_general_app_theme))) {

                        ThemeUtils.setTheme(Boolean.valueOf(value.toString()));
                        Intent intent = new Intent(mActivity, mActivity.getClass());
                        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                                GeneralPreferenceFragment.class.getName());
                        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                        ThemeUtils.change(mActivity, intent);

                    }
                    return true;
                }
            };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        mActivity = this;
        ThemeUtils.onActivityCreateSetTheme(this,false);
        setupActionBar();
        setTitle(getResources().getText(R.string.action_settings));

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
//    private void setupActionBar() {
//        Toolbar toolbar;
//
//        ViewGroup root = (ViewGroup) findViewById(
//                android.R.id.list).getParent().getParent().getParent();
//        toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.view_toolbar, root, false);
//        root.addView(toolbar, 0);
//
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || RulatePreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            Preference cacheButton = findPreference(getString(R.string.CleanCacheButton));
            cacheButton.setOnPreferenceClickListener((Preference preference) -> {
                final Context context = preference.getContext();
                AsyncTask.execute(() -> DatabaseDao.getInstance(context).getTextDao().cleanTable());
                Toast.makeText(context, context.getText(R.string.cache_cleaned),
                        Toast.LENGTH_SHORT).show();
                return true;
            });

            Preference prefButton = findPreference(getString(R.string.CleanPreferencesButton));
            prefButton.setOnPreferenceClickListener(preference -> {
                final Context context = preference.getContext();
                AsyncTask.execute(() -> {
//                        File sharedPreferenceFile = new File(                                "/data/data/" + context.getPackageName() + "/shared_prefs/");
                    File sharedPreferenceFile = new File("/data/data/" + context.getPackageName() + "/shared_prefs/");
                    File[] listFiles = sharedPreferenceFile.listFiles();
                    for (File file : listFiles) {
                        file.delete();
                    }

                });
                Toast.makeText(context, context.getText(R.string.cache_cleaned),
                        Toast.LENGTH_SHORT).show();
                return true;
            });

            findPreference(
                    getString(R.string.pref_general_hide_chapter)).setOnPreferenceChangeListener(
                    sChangePreferenceListener);
            findPreference(
                    getString(R.string.pref_general_app_theme)).setOnPreferenceChangeListener(
                    sChangePreferenceListener);

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }

    /**
     * This fragment shows Rulate preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class RulatePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_rulate);
            Preference prefLogin = findPreference(getString(R.string.rulate_authorization_pref));
            SharedPreferences mPreferences = getActivity().getSharedPreferences(
                    StringResources.Rulate_Login_Pref, 0);

            String token = mPreferences.getString(StringResources.KEY_Token, "");

            if(!token.equals("")){
                prefLogin.setSummary(mPreferences.getString(StringResources.KEY_Login, ""));
            }else{
                prefLogin.setSummary(getActivity().getString(R.string.login_to_rulate_summary));
            }
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
