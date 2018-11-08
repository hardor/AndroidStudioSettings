package ru.profapp.RanobeReader.Activities

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.BuildConfig
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Common.MyExceptionHandler
import ru.profapp.RanobeReader.Helpers.ThemeHelper
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        if (!MyApp.isApplicationInitialized) {
            val firstIntent = Intent(this, MainActivity::class.java)

            firstIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // So all other activities will be dumped
            startActivity(firstIntent)

            // We are done, so finish this activity and get out now
            finish()
            return
        }

        // Set up Crashlytics, disabled for debug builds
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        Fabric.with(this, crashlyticsKit)
        setupActionBar()
        title = resources.getText(R.string.action_settings)
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))
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
            findPreference(getString(R.string.pref_general_app_theme)).onPreferenceChangeListener = sChangePreferenceListener
            findPreference(getString(R.string.pref_general_volume_scroll)).onPreferenceChangeListener = sChangePreferenceListener
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
            val mPreferences = activity?.getSharedPreferences(Constants.Rulate_Login_Pref, 0)

            val token = mPreferences?.getString(Constants.KEY_Token, "") ?: ""

            if (token != "") {
                prefLogin.summary = mPreferences?.getString(Constants.KEY_Login, "") ?: ""
            } else {
                prefLogin.summary = resources.getString(R.string.summary_login)
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
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_ranoberf)
            val prefLogin = findPreference(getString(R.string.ranoberf_authorization_pref))

            val mPreferences = activity.getSharedPreferences(Constants.Ranoberf_Login_Pref, 0)

            val token = mPreferences?.getString(Constants.KEY_Token, "") ?: ""

            if (token != "") {
                prefLogin.summary = mPreferences?.getString(Constants.KEY_Login, "") ?: ""
            } else {
                prefLogin.summary = resources.getString(R.string.summary_login)
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

            val cacheButton = findPreference(getString(R.string.ClearCacheButton))

            cacheButton.setOnPreferenceClickListener { preference ->

                Completable.fromAction {
                    MyApp.database.textDao().cleanTable()
                }?.andThen(Completable.fromAction { MyApp.database.ranobeImageDao().cleanTable() })
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeOn(Schedulers.io())
                        ?.subscribe({
                            Toast.makeText(activity, resources.getText(R.string.cache_cremoved),
                                    Toast.LENGTH_SHORT).show()
                        }, {})

                true
            }

            val favButton = findPreference(getString(R.string.ClearLocalFavButton))
            favButton.setOnPreferenceClickListener {
                Completable.fromAction { MyApp.database.ranobeDao().cleanTable() }
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeOn(Schedulers.io())
                        ?.subscribe({
                            Toast.makeText(activity, resources.getText(R.string.bookmarks_removed),
                                    Toast.LENGTH_SHORT).show()
                        }, { })


                true
            }

            val clearReadChapterButton = findPreference(getString(R.string.ClearReadChapterButton))
            clearReadChapterButton.setOnPreferenceClickListener { preference ->
                activity.getSharedPreferences(Constants.is_readed_Pref, 0).edit().clear().apply()
                activity.getSharedPreferences(Constants.last_chapter_id_Pref, 0).edit().clear().apply()
                true
            }

            val prefButton = findPreference(getString(R.string.ClearHistoryButton))
            prefButton.setOnPreferenceClickListener {
                activity.getSharedPreferences(Constants.is_readed_Pref, 0).edit().clear().apply()
                activity.getSharedPreferences(Constants.last_chapter_id_Pref, 0).edit().clear().apply()

                Completable.fromAction { MyApp.database.ranobeHistoryDao().cleanHistory() }
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeOn(Schedulers.io())
                        ?.subscribe({
                            Toast.makeText(activity, resources.getText(R.string.history_removed), Toast.LENGTH_SHORT).show()
                        }, { })


                true
            }

            setHasOptionsMenu(true)
        }
    }

    companion object {

        private val sChangePreferenceListener = Preference.OnPreferenceChangeListener { preference, value ->

            when (preference.key) {
                preference.context.getString(R.string.pref_general_app_theme) -> {

                    ThemeHelper.setTheme(value.toString().toBoolean())
                    ThemeHelper.onActivityCreateSetTheme()
                }
                preference.context.getString(R.string.pref_general_volume_scroll) -> {
                    MyApp.useVolumeButtonsToScroll = value.toString().toBoolean()
                }
            }

            true
        }

        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }
    }

}

