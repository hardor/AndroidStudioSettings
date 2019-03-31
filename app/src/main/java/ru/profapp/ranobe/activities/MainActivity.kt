package ru.profapp.ranobe.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableItem
import co.zsmb.materialdrawerkt.draweritems.sectionHeader
import co.zsmb.materialdrawerkt.draweritems.switchable.switchItem
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsResponseListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.materialdrawer.Drawer
import com.webianks.easy_feedback.EasyFeedback
import de.cketti.library.changelog.ChangeLog
import io.reactivex.schedulers.Schedulers
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

class MainActivity : AppCompatActivity() {

    private lateinit var mBillingManager: BillingManager
    private var adView: AdView? = null

    private var currentTheme = ThemeHelper.sTheme

    private var currentFragment: String = Constants.FragmentType.Favorite.name
    private var currentTitle: String? = null

    private var alertErrorDialog: AlertDialog? = null

    private lateinit var materialDrawer: Drawer

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
            adView = findViewById<AdView>(R.id.adView)
            adView?.let {
                AdViewManager(lifecycle, it)
                it.loadAd(adRequest)
            }

        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        val cl = ChangeLog(this)
        if (cl.isFirstRun) {
            cl.logDialog.show()
        }

        materialDrawer = drawer {
            this.toolbar = toolbar

            headerDivider = false

            headerViewRes = if (MyApp.preferencesManager.isPremium) {
                R.layout.nav_header_main_premium
            }else{
                R.layout.nav_header_main
            }


            primaryItem {
                icon = R.drawable.ic_favorite_black_24dp_nav
                nameRes = R.string.favorite
                identifier = 100
            }

            expandableItem {
                nameRes = R.string.sites

                icon = R.drawable.ic_whatshot_black_24dp
                identifier = 201

                selectable = false


                primaryItem {
                    icon = R.mipmap.ic_rulate
                    nameRes = R.string.tl_rulate_name
                    identifier = 101
                    level = 2

                }
                primaryItem {
                    icon = R.mipmap.ic_ranoberf
                    nameRes = R.string.ranobe_rf
                    identifier = 102
                    level = 2

                }
                primaryItem {
                    icon = R.mipmap.ic_ranobehub
                    nameRes = R.string.ranobe_hub
                    identifier = 103
                    level = 2

                }
            }


            primaryItem {
                icon = R.drawable.ic_search_black_24dp
                nameRes = R.string.search
                identifier = 104
            }
            primaryItem {
                icon = R.drawable.ic_file_download_black_24dp_nav
                nameRes = R.string.saved_chapters
                identifier = 105
            }
            primaryItem {
                icon = R.drawable.ic_history_black_24dp
                nameRes = R.string.history
                identifier = 106
            }
            sectionHeader(R.string.general)


            primaryItem {
                icon = R.drawable.ic_settings_black_24dp
                nameRes = R.string.settings
                identifier = 107
            }
            primaryItem {
                icon = R.drawable.ic_send_black_24dp
                nameRes = R.string.feedback
                identifier = 108
            }

            primaryItem {
                icon = R.drawable.ic_attach_money_black_24dp
                nameRes = R.string.payment_ads_remove
                identifier = 109
                enabled = !MyApp.preferencesManager.isPremium
            }



            switchItem(R.string.app_theme) {
                icon = R.drawable.ic_settings_brightness_black_24dp_nav
                checked = MyApp.preferencesManager.isDarkTheme
                selectable = false
                onSwitchChanged { drawerItem, button, isEnabled ->

                    MyApp.preferencesManager.isDarkTheme = isEnabled
                    recreate()
                }
            }

            onItemClick { view, position, drawerItem ->
                var fragment: Fragment? = null
                when (drawerItem.identifier) {
                    100L -> {
                        currentFragment = Constants.FragmentType.Favorite.name
                        fragment = RanobeListFragment.newInstance(Constants.FragmentType.Favorite.name)
                        title = resources.getText(R.string.favorite)
                        currentTitle = resources.getText(R.string.favorite).toString()
                    }
                    101L -> {
                        currentFragment = Constants.FragmentType.Rulate.name
                        fragment = RanobeListFragment.newInstance(Constants.FragmentType.Rulate.name)
                        title = resources.getText(R.string.tl_rulate_name)
                        currentTitle = resources.getText(R.string.tl_rulate_name).toString()
                    }
                    102L -> {
                        currentFragment = Constants.FragmentType.Ranoberf.name
                        fragment = RanobeListFragment.newInstance(Constants.FragmentType.Ranoberf.name)
                        title = resources.getText(R.string.ranobe_rf)
                        currentTitle = resources.getText(R.string.ranobe_rf).toString()
                    }
                    103L -> {
                        currentFragment = Constants.FragmentType.RanobeHub.name
                        fragment = RanobeListFragment.newInstance(Constants.FragmentType.RanobeHub.name)
                        title = resources.getText(R.string.ranobe_hub)
                        currentTitle = resources.getText(R.string.ranobe_hub).toString()
                    }

                    104L -> {
                        currentFragment = Constants.FragmentType.Search.name
                        fragment = SearchFragment.newInstance()
                        title = resources.getText(R.string.search)
                        currentTitle = resources.getText(R.string.search).toString()
                    }
                    105L -> {
                        currentFragment = Constants.FragmentType.Saved.name
                        fragment = RanobeListFragment.newInstance(Constants.FragmentType.Saved.name)
                        title = resources.getText(R.string.saved_chapters)
                        currentTitle = resources.getText(R.string.saved_chapters).toString()
                    }
                    106L -> {
                        currentFragment = Constants.FragmentType.History.name
                        fragment = HistoryFragment.newInstance()
                        title = resources.getText(R.string.history)
                        currentTitle = resources.getText(R.string.history).toString()
                    }
                    107L -> {
                        launchActivity<SettingsActivity>()
                    }
                    108L -> {
                        EasyFeedback.Builder(this@MainActivity).withEmail("admin@profapp.ru").withSystemInfo().build().start()
                    }
                    109L -> {

                        AlertDialog.Builder(this@MainActivity).setTitle(getString(R.string.alert_premium_pay)).setIcon(R.drawable.ic_info_black_24dp).setMessage(getString(R.string.alert_premium_pay_message)).setCancelable(true).setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }.setPositiveButton("OK") { _, _ ->
                            mBillingManager.initiatePurchaseFlow(BillingConstants.SKU_PREMIUM, BillingClient.SkuType.INAPP)
                        }.create().show()

                    }

                }

                if (fragment != null) {


                    if (!MyApp.preferencesManager.isPremium) {
                        adView?.let {
                            AdViewManager(lifecycle, it)
                            it.loadAd(adRequest)
                        }
                    }

                    val ft = supportFragmentManager.beginTransaction()
                    ft.replace(R.id.mainFrame, fragment, MY_FRAGMENT)
                    ft.commit()
                }

                false
            }

        }


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


        val vkButton: ImageButton = materialDrawer.header.findViewById(R.id.vkButton)

        vkButton.setOnClickListener {
            val url = getString(R.string.all_vkUrl)

            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (browserIntent.resolveActivity(this.packageManager) != null) startActivity(browserIntent)
            else Toast.makeText(this, R.string.browser_exist, Toast.LENGTH_SHORT).show()
        }

        val navText: TextView = materialDrawer.header.findViewById(R.id.navText)
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

        if (materialDrawer.isDrawerOpen) {
            materialDrawer.closeDrawer();
        } else {
            super.onBackPressed();
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
