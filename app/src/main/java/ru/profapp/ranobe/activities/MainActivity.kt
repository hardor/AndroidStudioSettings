package ru.profapp.ranobe.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsResponseListener
import com.google.android.gms.ads.AdRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.webianks.easy_feedback.EasyFeedback
import de.cketti.library.changelog.ChangeLog
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import ru.profapp.ranobe.BuildConfig
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.billing.BillingConstants
import ru.profapp.ranobe.billing.BillingManager
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.common.MyExceptionHandler
import ru.profapp.ranobe.fragments.HistoryFragment
import ru.profapp.ranobe.fragments.RanobeListFragment
import ru.profapp.ranobe.fragments.SearchFragment
import ru.profapp.ranobe.helpers.AdViewManager
import ru.profapp.ranobe.helpers.ThemeHelper
import ru.profapp.ranobe.helpers.launchActivity
import ru.profapp.ranobe.models.Chapter
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.network.repositories.RanobeRfRepository
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mBillingManager: BillingManager

    private var currentTheme = ThemeHelper.sTheme

    private var currentFragment: String = Constants.FragmentType.Favorite.name
    private var currentTitle: String? = null

    private var alertErrorDialog: AlertDialog? = null

    @Inject
    lateinit var adRequest: AdRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSettingPreference()
        MyApp.isApplicationInitialized = true
        MyApp.component.inject(this)

        if (MyApp.preferencesManager.isPremium) {
            setContentView(R.layout.activity_main_premium)
        } else {
            setContentView(R.layout.activity_main)
        }

        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))

        Thread {
            val isFirstStart = MyApp.preferencesManager.isFirstStart
            if (isFirstStart) {
                launchActivity<IntroActivity>()
            }
        }.start()

        if (intent.getBooleanExtra("crash", false)) {
            intent.removeExtra("crash")
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.all_uncaughtException)).setMessage(getString(R.string.all_appCrashed)).setIcon(R.drawable.ic_info_black_24dp).setCancelable(true).setPositiveButton("OK") { dialog, _ ->
                        dialog.cancel()
                    }

            alertErrorDialog = builder.create()
            alertErrorDialog?.show()
        }


        if (!MyApp.preferencesManager.isPremium) {
            AdViewManager(lifecycle, adView)
            adView.loadAd(adRequest)
        }


        setSupportActionBar(toolbar)


        val cl = ChangeLog(this)
        if (cl.isFirstRun) {
            cl.logDialog.show()
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)

        val ft = supportFragmentManager.beginTransaction()

        if (savedInstanceState == null) {
            currentFragment = Constants.FragmentType.Favorite.name
            currentTitle = resources.getText(R.string.favorite).toString()
            ft.replace(R.id.mainFrame, RanobeListFragment.newInstance(currentFragment), MY_FRAGMENT).commit()
        } else {
            currentFragment = savedInstanceState.getString("Fragment", Constants.FragmentType.Favorite.name)
            currentTitle = savedInstanceState.getString("Title", resources.getText(R.string.favorite).toString())

            val myFragment = supportFragmentManager.findFragmentByTag("MY_FRAGMENT")
            if (myFragment == null) {
                if (currentFragment == Constants.FragmentType.Search.name) {
                    ft.replace(R.id.mainFrame, SearchFragment.newInstance(), MY_FRAGMENT)
                } else {
                    ft.replace(R.id.mainFrame, RanobeListFragment.newInstance(currentFragment), MY_FRAGMENT)
                }
                ft.commit()
            }
        }

        title = currentTitle

        val floatingActionButton = findViewById<FloatingActionButton>(R.id.fab)
        floatingActionButton.setOnClickListener {
            val chapterProgress = MyApp.database.chapterProgressDao().getLastChapter().subscribeOn(Schedulers.io())?.blockingGet()
            if (chapterProgress != null) {
                if (MyApp.ranobe == null || !MyApp.ranobe!!.url.contains(chapterProgress.ranobeUrl) || !MyApp.ranobe!!.wasUpdated) {

                    val ranobe = Ranobe()
                    ranobe.url = chapterProgress.ranobeUrl

                    ranobe.updateRanobe(this).subscribeOn(Schedulers.io()).blockingGet()
                    if (!ranobe.chapterList.any()) {
                        ranobe.chapterList.add(Chapter(chapterProgress))
                    }
                    MyApp.ranobe = ranobe
                }
                if (MyApp.ranobe != null && MyApp.ranobe!!.url.contains(chapterProgress.ranobeUrl)) {

                    launchActivity<ChapterTextActivity> {
                        putExtra("ChapterUrl", chapterProgress.chapterUrl)
                        putExtra("Progress", chapterProgress.progress)
                    }
                }

            } else {
                Toast.makeText(this, resources.getText(R.string.not_history), Toast.LENGTH_SHORT).show()
            }

        }

        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.itemIconTintList = null
        navigationView.setNavigationItemSelectedListener(this)

        val vkButton: ImageButton = navigationView.getHeaderView(0).findViewById(R.id.vkButton)

        vkButton.setOnClickListener {
            val url = getString(R.string.all_vkUrl)

            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (browserIntent.resolveActivity(this.packageManager) != null) startActivity(browserIntent)
            else Toast.makeText(this, R.string.browser_exist, Toast.LENGTH_SHORT).show()
        }

        val navText: TextView = navigationView.getHeaderView(0).findViewById(R.id.navText)
        navText.text = "${getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}"


        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = BillingManager(this, object : BillingManager.BillingUpdatesListener {
            override fun onBillingClientSetupFinished() {

                @BillingClient.SkuType val billingType = BillingClient.SkuType.INAPP
                mBillingManager.querySkuDetailsAsync(billingType, BillingConstants.getSkuList(billingType), object : SkuDetailsResponseListener {
                    override fun onSkuDetailsResponse(responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
                        if (responseCode != BillingClient.BillingResponse.OK) {
                            Log.w(TAG, "Unsuccessful query for type: $billingType. Error code: $responseCode");
                        } else if (skuDetailsList != null && skuDetailsList.size > 0) {

                        }
                    }


                });
            }

            override fun onConsumeFinished(token: String, result: Int) {
                Log.d(TAG, "Consumption finished. Purchase token: " + token + ", result: " + result);

                // Note: We know this is the SKU_GAS, because it's the only one we consume, so we don't
                // check if token corresponding to the expected sku was consumed.
                // If you have more than one sku, you probably need to validate that the token matches
                // the SKU you expect.
                // It could be done by maintaining a map (updating it every time you call consumeAsync)
                // of all tokens into SKUs which were scheduled to be consumed and then looking through
                // it here to check which SKU corresponds to a consumed token.
                if (result == BillingClient.BillingResponse.OK) {
                    // Successfully consumed, so we apply the effects of the item in our
                    // game world's logic, which in our case means filling the gas tank a bit
                    Log.d(TAG, "Consumption successful. Provisioning.");

                } else {
                    Log.w(TAG, "Consumption error. result: " + result);

                }

                Log.d(TAG, "End consumption flow.");
            }

            override fun onPurchasesUpdated(purchases: List<Purchase>) {

                val prevPremiumStatus = MyApp.preferencesManager.isPremium
                for (purchase in purchases) {
                    when (purchase.sku) {

                        BillingConstants.SKU_PREMIUM -> {
                            mBillingManager.consumeAsync(purchase.purchaseToken);
                            MyApp.preferencesManager.isPremium = true
                        }

                    }
                }
                if (prevPremiumStatus != MyApp.preferencesManager.isPremium) this@MainActivity.recreate()

            }

        })

    }

    private fun initSettingPreference() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)

        ThemeHelper.setTheme(MyApp.preferencesManager.isDarkTheme)

        ThemeHelper.onActivityCreateSetTheme()
        currentTheme = AppCompatDelegate.getDefaultNightMode()

        val token = MyApp.preferencesManager.ranoberfToken
        if (!token.isBlank()) {
            RanobeRfRepository.token = token
        }

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
            launchActivity<SettingsActivity>()
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
                currentFragment = Constants.FragmentType.Favorite.name
                fragment = RanobeListFragment.newInstance(Constants.FragmentType.Favorite.name)
                title = resources.getText(R.string.favorite)
                currentTitle = resources.getText(R.string.favorite).toString()
            }
            R.id.nav_rulate -> {
                currentFragment = Constants.FragmentType.Rulate.name
                fragment = RanobeListFragment.newInstance(Constants.FragmentType.Rulate.name)
                title = resources.getText(R.string.tl_rulate_name)
                currentTitle = resources.getText(R.string.tl_rulate_name).toString()
            }
            R.id.nav_ranoberf -> {
                currentFragment = Constants.FragmentType.Ranoberf.name
                fragment = RanobeListFragment.newInstance(Constants.FragmentType.Ranoberf.name)
                title = resources.getText(R.string.ranobe_rf)
                currentTitle = resources.getText(R.string.ranobe_rf).toString()
            }
            R.id.nav_ranobehub -> {
                currentFragment = Constants.FragmentType.RanobeHub.name
                fragment = RanobeListFragment.newInstance(Constants.FragmentType.RanobeHub.name)
                title = resources.getText(R.string.ranobe_hub)
                currentTitle = resources.getText(R.string.ranobe_hub).toString()
            }
            R.id.nav_manage -> {
                launchActivity<SettingsActivity>()
            }
            R.id.nav_search -> {
                currentFragment = Constants.FragmentType.Search.name
                fragment = SearchFragment.newInstance()
                title = resources.getText(R.string.search)
                currentTitle = resources.getText(R.string.search).toString()
            }
            R.id.nav_chapters -> {
                currentFragment = Constants.FragmentType.Saved.name
                fragment = RanobeListFragment.newInstance(Constants.FragmentType.Saved.name)
                title = resources.getText(R.string.saved_chapters)
                currentTitle = resources.getText(R.string.saved_chapters).toString()
            }
            R.id.nav_history -> {
                currentFragment = Constants.FragmentType.History.name
                fragment = HistoryFragment.newInstance()
                title = resources.getText(R.string.history)
                currentTitle = resources.getText(R.string.history).toString()
            }
            R.id.nav_send -> {
                EasyFeedback.Builder(this).withEmail("admin@profapp.ru").withSystemInfo().build().start()
            }
            R.id.nav_ads_remove -> {

                AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.alert_premium_pay))
                        .setIcon(R.drawable.ic_info_black_24dp).setMessage(getString(R.string.alert_premium_pay_message))
                        .setCancelable(true)
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        .setPositiveButton("OK") { _, _ ->
                            mBillingManager.initiatePurchaseFlow(BillingConstants.SKU_PREMIUM, BillingClient.SkuType.INAPP)
                        }.create().show()

            }
        }

        if (fragment != null) {


            if (!MyApp.preferencesManager.isPremium) {
                AdViewManager(lifecycle, adView)
                adView.loadAd(adRequest)
            }

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.mainFrame, fragment, MY_FRAGMENT)
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

    override fun onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        if (currentTheme != AppCompatDelegate.getDefaultNightMode()) {
            recreate()
        }


    }

    override fun onPause() {
        alertErrorDialog?.dismiss()
        alertErrorDialog = null
        super.onPause()
    }

    companion object {
        const val MY_FRAGMENT = "MY_FRAGMENT"
        private val TAG = "Main Activity"
    }

}
