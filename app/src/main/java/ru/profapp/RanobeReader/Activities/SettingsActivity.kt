package ru.profapp.RanobeReader.Activities

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.BuildConfig
import ru.profapp.RanobeReader.Common.StringResources
import ru.profapp.RanobeReader.Common.ThemeUtils
import ru.profapp.RanobeReader.Helpers.RanobeKeeper
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up Crashlytics, disabled for debug builds
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        Fabric.with(this, crashlyticsKit)
        setupActionBar()
        title = resources.getText(R.string.action_settings)

    }

    /**
     * Set up the [ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return (PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || RulatePreferenceFragment::class.java.name == fragmentName
                || RanobeRfPreferenceFragment::class.java.name == fragmentName
                || DataPreferenceFragment::class.java.name == fragmentName)
        //|| ExportPreferenceFragment.class.getName().equals(fragmentName);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

            findPreference(
                    getString(R.string.pref_general_auto_save)).onPreferenceChangeListener = sChangePreferenceListener

            findPreference(
                    getString(R.string.pref_general_app_theme)).onPreferenceChangeListener = sChangePreferenceListener
        }


        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                activity.onBackPressed()
                return true
            }
            return super.onOptionsItemSelected(item)
        }

    }

    /**
     * This fragment shows Rulate preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class RulatePreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_rulate)
            val prefLogin = findPreference(getString(R.string.rulate_authorization_pref))
            val mPreferences = activity?.getSharedPreferences(
                    StringResources.Rulate_Login_Pref, 0)

            val token = mPreferences?.getString(StringResources.KEY_Token, "") ?: ""

            if (token != "") {
                prefLogin.summary = mPreferences?.getString(StringResources.KEY_Login, "") ?: ""
            } else {
                prefLogin.summary = resources.getString(R.string.login_to_summary)
            }
            setHasOptionsMenu(true)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                activity?.onBackPressed()
                return true
            }
            return super.onOptionsItemSelected(item)
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class RanobeRfPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_ranoberf)
            val prefLogin = findPreference(getString(R.string.ranoberf_authorization_pref))

            val mPreferences = activity.getSharedPreferences(StringResources.Ranoberf_Login_Pref, 0)

            val token = mPreferences?.getString(StringResources.KEY_Token, "") ?: ""

            if (token != "") {
                prefLogin.summary = mPreferences?.getString(StringResources.KEY_Login, "") ?: ""
            } else {
                prefLogin.summary = resources.getString(R.string.login_to_summary)
            }
            setHasOptionsMenu(true)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                activity?.onBackPressed()
                return true
            }
            return super.onOptionsItemSelected(item)
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DataPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data)

            val cacheButton = findPreference(getString(R.string.CleanCacheButton))

            cacheButton.setOnPreferenceClickListener { preference ->
                Completable.fromAction {
                    MyApp.database?.textDao()?.cleanTable()
                }?.andThen(Completable.fromAction { MyApp.database?.ranobeImageDao()?.cleanTable() })
                        ?.subscribeOn(Schedulers.io())
                        ?.subscribe({
                            Toast.makeText(activity, resources.getText(R.string.cache_cleaned),
                                    Toast.LENGTH_SHORT).show()
                        }, {})

                true
            }

            val favButton = findPreference(getString(R.string.CleanFavoriteButton))
            favButton.setOnPreferenceClickListener { preference ->
                Completable.fromAction { MyApp.database?.ranobeDao()?.cleanTable() }?.subscribeOn(Schedulers.io())
                        ?.subscribe({
                            Toast.makeText(activity, resources.getText(R.string.bookmarks_cleaned),
                                    Toast.LENGTH_SHORT).show()
                        }, { })


                true
            }

            val prefButton = findPreference(getString(R.string.CleanHistoryButton))
            prefButton.setOnPreferenceClickListener { preference ->
                activity!!.getSharedPreferences(
                        StringResources.Last_readed_Pref,
                        0).edit().clear().apply()
                activity!!.getSharedPreferences(StringResources.is_readed_Pref,
                        0).edit().clear().apply()

                Toast.makeText(activity, resources.getText(R.string.cache_cleaned), Toast.LENGTH_SHORT).show()
                true
            }

            setHasOptionsMenu(true)
        }
    }

    companion object {

        private val sChangePreferenceListener = Preference.OnPreferenceChangeListener { preference, value ->

            when {
                preference.key == preference.context.getString(
                        R.string.pref_general_auto_save) -> RanobeKeeper.autoSaveText = java.lang.Boolean.valueOf(value.toString())
                preference.key == preference.context.getString(
                        R.string.pref_general_app_theme) -> {

                    ThemeUtils.setTheme(java.lang.Boolean.valueOf(value.toString()))
                    ThemeUtils.onActivityCreateSetTheme()
                }
            }

            true
        }

        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }
    }

    //    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    //    public static class ExportPreferenceFragment extends PreferenceFragment {
    //
    //        @Override
    //        public void onCreate(Bundle savedInstanceState?) {
    //            super.onCreate(savedInstanceState);
    //            addPreferencesFromResource(R.xml.pref_export);
    //
    //            Preference exportButton = findPreference(getString(R.string.ExportButton));
    //            exportButton.setOnPreferenceClickListener((Preference preference) -> {
    //                final Context context = preference.getContext();
    //
    //                String fileName = "ranobeReader.backup";
    //
    //
    //                File output = isNew File(context.getFilesDir(), fileName);
    //                AsyncTask.execute(() -> {
    //                try {
    //                    String value = "";
    //                    FileOutputStream fileout = isNew FileOutputStream(output.getAbsolutePath());
    //                    OutputStreamWriter outputWriter = isNew OutputStreamWriter(fileout);
    //
    //                    List<Ranobe> ranobes = MyApp.database?.RanobeDao()?.GetFavoriteRanobes();
    //                    Gson gson = isNew GsonBuilder().setPrettyPrinting().create();
    //
    //                    //value = gson.toJson(ranobes.get(0));
    //                    // TypeToken<List<Ranobe>>() {}.getType());
    //
    //                    outputWriter.write(value);
    //                    outputWriter.close();
    //                    Toast.makeText(context, "File saved successfully!",
    //                            Toast.LENGTH_SHORT).show();
    //                } catch (IOException e) {
    //                    Log.e("Exception", "File write failed: " + e.toString());
    //                }
    //
    //
    //
    //                });
    //                return true;
    //            });
    //
    //            Preference importButton = findPreference(getString(R.string.ImportButton));
    //            importButton.setOnPreferenceClickListener((Preference preference) -> {
    //                final Context context = preference.getContext();
    //                Intent intent = isNew Intent()
    //                        .setType("*/*")
    //                        .setAction(Intent.ACTION_GET_CONTENT);
    //
    //                startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
    //
    //                return true;
    //            });
    //
    //
    //
    //            setHasOptionsMenu(true);
    //        }
    //
    //        @Override
    //        public boolean onOptionsItemSelected(MenuItem item) {
    //            int id = item.getItemId();
    //            if (id == android.R.id.home) {
    //                getActivity().onBackPressed();
    //                return true;
    //            }
    //            return super.onOptionsItemSelected(item);
    //        }
    //
    //    }

}

