package ru.profapp.RanobeReader.Activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import io.fabric.sdk.android.Fabric
import ru.profapp.RanobeReader.BuildConfig
import ru.profapp.RanobeReader.Common.RanobeConstants
import ru.profapp.RanobeReader.Common.StringResources
import ru.profapp.RanobeReader.Common.StringResources.KEY_Login
import ru.profapp.RanobeReader.Common.ThemeUtils
import ru.profapp.RanobeReader.Fragments.RanobeRecyclerFragment
import ru.profapp.RanobeReader.Fragments.SearchFragment
import ru.profapp.RanobeReader.Helpers.MyLog
import ru.profapp.RanobeReader.Helpers.RanobeKeeper
import ru.profapp.RanobeReader.R

class MainActivity : AppCompatActivity(), RanobeRecyclerFragment.OnListFragmentInteractionListener, SearchFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    private val ExceptionHandler = Thread.UncaughtExceptionHandler { th, ex ->
        MyLog.SendError(MyLog.LogType.WARN, "MainActivity",
                "Uncaught exception", ex)
    }
    private var adView: AdView? = null

    private var currentFragment: String? = null
    private var currentTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        initSettingPreference()
        super.onCreate(savedInstanceState)
        // Set up Crashlytics, disabled for debug builds
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        Fabric.with(this, crashlyticsKit, Crashlytics())

        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler)
        setContentView(R.layout.activity_main)
        if (!BuildConfig.PAID_VERSION) {

            MobileAds.initialize(this, getString(R.string.app_admob_id))
            adView = findViewById(R.id.adView)

            val adRequest = AdRequest.Builder()

            if (BuildConfig.DEBUG) {
                adRequest.addTestDevice("sdfsdf")
            }

            adView!!.loadAd(adRequest.build())
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)

        val ft = supportFragmentManager.beginTransaction()
        currentFragment = RanobeConstants.FragmentType.Favorite.name
        currentTitle = resources.getText(R.string.favorite).toString()
        if (savedInstanceState != null) {
            currentFragment = savedInstanceState.getString("Fragment",
                    RanobeConstants.FragmentType.Favorite.name)
            currentTitle = savedInstanceState.getString("Title",
                    resources.getText(R.string.favorite).toString())
        }
        if (currentFragment == RanobeConstants.FragmentType.Search.name) {
            ft.replace(R.id.mainFrame, SearchFragment.newInstance())
        } else {
            ft.replace(R.id.mainFrame, RanobeRecyclerFragment.newInstance(currentFragment!!))
        }

        ft.commit()
        title = currentTitle

        val floatingActionButton = findViewById<FloatingActionButton>(R.id.fab)
        floatingActionButton.setOnClickListener { view ->
            try {
                val ranobeListview = findViewById<RecyclerView>(R.id.ranobeListView)
        ranobeListview.scrollToPosition(0)
    } catch (ignored: NullPointerException) {

    }


}

        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

    }

    private fun initSettingPreference() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)
        val settingPref = PreferenceManager.getDefaultSharedPreferences(
                applicationContext)
        RanobeKeeper.chapterTextSize = settingPref.getInt(
                applicationContext.getString(R.string.pref_general_text_size), 13)

        RanobeKeeper.autoSaveText = settingPref.getBoolean(
                applicationContext.getString(R.string.pref_general_auto_save),
                false)

        val rfPref = applicationContext.getSharedPreferences(
                StringResources.Ranoberf_Login_Pref, Context.MODE_PRIVATE)
        if (rfPref != null) {
            RanobeKeeper.ranobeRfToken = rfPref.getString(
                    KEY_Login, "")
        }

        ThemeUtils.setTheme(settingPref.getBoolean(
                applicationContext.getString(R.string.pref_general_app_theme), false))
        ThemeUtils.onActivityCreateSetTheme()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            title = resources.getText(R.string.action_settings)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        var fragment: Fragment? = null

        when (id) {
            R.id.nav_favorite -> {
                currentFragment = RanobeConstants.FragmentType.Favorite.name
                fragment = RanobeRecyclerFragment.newInstance(
                        RanobeConstants.FragmentType.Favorite.name)
                title = resources.getText(R.string.favorite)
                currentTitle = resources.getText(R.string.favorite).toString()
            }
            R.id.nav_rulate -> {
                currentFragment = RanobeConstants.FragmentType.Rulate.name
                fragment = RanobeRecyclerFragment.newInstance(
                        RanobeConstants.FragmentType.Rulate.name)
                title = resources.getText(R.string.tl_rulate_name)
                currentTitle = resources.getText(R.string.tl_rulate_name).toString()
            }
            R.id.nav_ranoberf -> {
                currentFragment = RanobeConstants.FragmentType.Ranoberf.name
                fragment = RanobeRecyclerFragment.newInstance(
                        RanobeConstants.FragmentType.Ranoberf.name)
                title = resources.getText(R.string.ranobe_rf)
                currentTitle = resources.getText(R.string.ranobe_rf).toString()
            }
            R.id.nav_ranobehub -> {
                currentFragment = RanobeConstants.FragmentType.RanobeHub.name
                fragment = RanobeRecyclerFragment.newInstance(
                        RanobeConstants.FragmentType.RanobeHub.name)
                title = resources.getText(R.string.ranobe_hub)
                currentTitle = resources.getText(R.string.ranobe_hub).toString()
            }
            R.id.nav_manage -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_search -> {
                currentFragment = RanobeConstants.FragmentType.Search.name
                fragment = SearchFragment.newInstance()
                title = resources.getText(R.string.search)
                currentTitle = resources.getText(R.string.search).toString()
            }
            R.id.nav_chapters -> {
                currentFragment = RanobeConstants.FragmentType.Saved.name

                fragment = RanobeRecyclerFragment.newInstance(RanobeConstants.FragmentType.Saved.name)
                title = resources.getText(R.string.saved_chapters)
                currentTitle = resources.getText(R.string.saved_chapters).toString()
            }
            R.id.nav_send -> {
                val appPackageName = packageName // getPackageName() from Context or Activity object
                try {
                    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
                    if (marketIntent.resolveActivity(this.packageManager) != null)
                        startActivity(marketIntent)
                    else
                        Toast.makeText(this, R.string.market_exist, Toast.LENGTH_SHORT).show()

                } catch (ignored: android.content.ActivityNotFoundException) {

                }

            }
        }

        if (fragment != null) {
            if (!BuildConfig.PAID_VERSION) {
                adView!!.loadAd(AdRequest.Builder().build())
            }
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.mainFrame, fragment)
            ft.commit()
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState)
        // Save our own state now

        outState.putString("Fragment", currentFragment)
        outState.putString("Title", currentTitle)

    }


}
