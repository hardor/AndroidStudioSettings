package ru.profapp.RanobeReader;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Common.ThemeUtils;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final Preference.OnPreferenceChangeListener sChangePreferenceListener =
            (preference, value) -> {

                if (preference.getKey().equals(preference.getContext().getString(
                        R.string.pref_general_hide_chapter))) {

                    RanobeKeeper.getInstance().setHideUnavailableChapters(
                            Boolean.valueOf(value.toString()));
                } else if (preference.getKey().equals(preference.getContext().getString(
                        R.string.pref_general_auto_save))) {

                    RanobeKeeper.getInstance().setAutoSaveText(
                            Boolean.valueOf(value.toString()));
                } else if (preference.getKey().equals(preference.getContext().getString(
                        R.string.pref_general_app_theme))) {

                    ThemeUtils.setTheme(Boolean.valueOf(value.toString()));
                    ThemeUtils.onActivityCreateSetTheme();
                }

                return true;
            };


    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(this, crashlyticsKit);

        setupActionBar();
        setTitle(getResources().getText(R.string.action_settings));

    }

    /**
     * Set up the {@link ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

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
                || RulatePreferenceFragment.class.getName().equals(fragmentName)
                || RanobeRfPreferenceFragment.class.getName().equals(fragmentName)
                || DataPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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



            findPreference(
                    getString(R.string.pref_general_hide_chapter)).setOnPreferenceChangeListener(
                    sChangePreferenceListener);
            findPreference(
                    getString(R.string.pref_general_auto_save)).setOnPreferenceChangeListener(
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

            if (!token.equals("")) {
                prefLogin.setSummary(mPreferences.getString(StringResources.KEY_Login, ""));
            } else {
                prefLogin.setSummary(getActivity().getString(R.string.login_to_summary));
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class RanobeRfPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_ranoberf);
            Preference prefLogin = findPreference(getString(R.string.ranoberf_authorization_pref));

            SharedPreferences mPreferences = getActivity().getSharedPreferences(
                    StringResources.Ranoberf_Login_Pref, 0);

            String token = mPreferences.getString(StringResources.KEY_Token, "");

            if (!token.equals("")) {
                prefLogin.setSummary(mPreferences.getString(StringResources.KEY_Login, ""));
            } else {
                prefLogin.setSummary(getActivity().getString(R.string.login_to_summary));
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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data);

            Preference cacheButton = findPreference(getString(R.string.CleanCacheButton));
            cacheButton.setOnPreferenceClickListener((Preference preference) -> {
                final Context context = preference.getContext();
                AsyncTask.execute(() -> DatabaseDao.getInstance(context).getTextDao().cleanTable());
                Toast.makeText(context, context.getText(R.string.cache_cleaned),
                        Toast.LENGTH_SHORT).show();
                return true;
            });

            Preference favButton = findPreference(getString(R.string.CleanFavoriteButton));
            favButton.setOnPreferenceClickListener((Preference preference) -> {
                final Context context = preference.getContext();
                AsyncTask.execute(() -> DatabaseDao.getInstance(
                        context).getRanobeDao().cleanTable());
                Toast.makeText(context, context.getText(R.string.bookmarks_cleaned),
                        Toast.LENGTH_SHORT).show();
                return true;
            });

            Preference prefButton = findPreference(getString(R.string.CleanHistoryButton));
            prefButton.setOnPreferenceClickListener(preference -> {
                final Context context = preference.getContext();
                AsyncTask.execute(() -> {

                    getActivity().getSharedPreferences(StringResources.Last_readed_Pref,
                            0).edit().clear().apply();
                    getActivity().getSharedPreferences(StringResources.is_readed_Pref,
                            0).edit().clear().apply();

                });
                Toast.makeText(context, context.getText(R.string.cache_cleaned),
                        Toast.LENGTH_SHORT).show();
                return true;
            });

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
