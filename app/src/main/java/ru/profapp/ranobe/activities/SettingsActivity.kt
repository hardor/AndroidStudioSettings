package ru.profapp.ranobe.activities

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
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.common.MyExceptionHandler
import ru.profapp.ranobe.helpers.ThemeHelper
import ru.profapp.ranobe.helpers.launchActivity

class SettingsActivity : AppCompatPreferenceActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!MyApp.isApplicationInitialized) {

            launchActivity<MainActivity> {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            // We are done, so finish this activity and get out now
            finish()
            return
        }

        MyApp.component.inject(this)

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
        return (PreferenceFragment::class.java.name == fragmentName || GeneralPreferenceFragment::class.java.name == fragmentName || AuthPreferenceFragment::class.java.name == fragmentName || DataPreferenceFragment::class.java.name == fragmentName)
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
            findPreference(getString(R.string.pref_general_auto_bookmark)).onPreferenceChangeListener = sChangePreferenceListener
            findPreference(getString(R.string.pref_general_swipe_navigate)).onPreferenceChangeListener = sChangePreferenceListener

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
    class AuthPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_auth)
            val prefLogin = findPreference(getString(R.string.rulate_authorization_pref))


            val token = MyApp.preferencesManager.rulateToken

            if (token != "") {
                prefLogin.summary = MyApp.preferencesManager.rulateLogin
            } else {
                prefLogin.summary = resources.getString(R.string.summary_login)
            }

            val prefLogin2 = findPreference(getString(R.string.ranoberf_authorization_pref))

            val token2 = MyApp.preferencesManager.ranoberfToken

            if (token2 != "") {
                prefLogin2.summary = MyApp.preferencesManager.ranoberfLogin
            } else {
                prefLogin2.summary = resources.getString(R.string.summary_login)
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

                GlobalScope.launch(Dispatchers.IO  ) {
                    MyApp.database.textDao().cleanTable()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity,
                            resources.getText(R.string.cache_cremoved),
                            Toast.LENGTH_SHORT).show()
                    }
                }

                true
            }

            val favButton = findPreference(getString(R.string.ClearLocalFavButton))
            favButton.setOnPreferenceClickListener {

                GlobalScope.launch(Dispatchers.IO  ) {
                    MyApp.database.ranobeDao().cleanTable()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity,
                            resources.getText(R.string.bookmarks_removed),
                            Toast.LENGTH_SHORT).show()
                    }
                }



                true
            }

            val clearReadChapterButton = findPreference(getString(R.string.ClearReadChapterButton))
            clearReadChapterButton.setOnPreferenceClickListener { preference ->
                activity.applicationContext.getSharedPreferences(Constants.last_chapter_id_Pref, 0)
                    .edit().clear().apply()
                Toast.makeText(activity,
                    resources.getText(R.string.last_read_chapter_removed),
                    Toast.LENGTH_SHORT).show()
                true
            }


            val clearImageCacheButton = findPreference(getString(R.string.ClearImageCacheButton))
            clearImageCacheButton.setOnPreferenceClickListener { preference ->
                GlobalScope.launch(Dispatchers.IO  ) {
                    MyApp.database.ranobeImageDao().cleanTable()
                    Glide.get(activity.applicationContext).clearDiskCache()


                    withContext(Dispatchers.Main) {

                        Glide.get(activity.applicationContext).clearMemory()

                        Toast.makeText(activity,
                            resources.getText(R.string.images_cache_removed),
                            Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }

            val prefButton = findPreference(getString(R.string.ClearHistoryButton))
            prefButton.setOnPreferenceClickListener {

                GlobalScope.launch(Dispatchers.IO  ) {
                    MyApp.database.ranobeHistoryDao().cleanHistory()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity,
                            resources.getText(R.string.history_removed),
                            Toast.LENGTH_SHORT).show()
                    }
                }


                true
            }

            val clearProgressButton = findPreference(getString(R.string.ClearProgressButton))
            clearProgressButton.setOnPreferenceClickListener {

                GlobalScope.launch(Dispatchers.IO  ) {
                    MyApp.database.chapterProgressDao().cleanTable()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity,
                            resources.getText(R.string.history_removed),
                            Toast.LENGTH_SHORT).show()
                    }
                }


                true
            }


            setHasOptionsMenu(true)
        }
    }

    companion object {

        private val TAG = "Settings Activity"

        private val sChangePreferenceListener = Preference.OnPreferenceChangeListener { preference, value ->

            when (preference.key) {
                preference.context.getString(R.string.pref_general_app_theme) -> {
                    MyApp.preferencesManager.isDarkTheme = value.toString().toBoolean()
                    ThemeHelper.setTheme(value.toString().toBoolean())
                    ThemeHelper.onActivityCreateSetTheme()
                }
                preference.context.getString(R.string.pref_general_volume_scroll) -> {
                    MyApp.preferencesManager.useVolumeButtonsToScroll = value.toString().toBoolean()
                }
                preference.context.getString(R.string.pref_general_auto_bookmark) -> {
                    MyApp.preferencesManager.isAutoAddBookmark = value.toString().toBoolean()
                }
                preference.context.getString(R.string.pref_general_swipe_navigate) -> {
                    MyApp.preferencesManager.useSwipeForNavigate = value.toString().toBoolean()
                }

            }

            true
        }

        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }
    }

    override fun onDestroy() {

        Thread.setDefaultUncaughtExceptionHandler(null)
        super.onDestroy()
    }

}

