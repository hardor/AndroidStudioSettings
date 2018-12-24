package ru.profapp.ranobe.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vorlonsoft.android.rate.AppRate
import com.vorlonsoft.android.rate.StoreType
import com.vorlonsoft.android.rate.Time
import io.fabric.sdk.android.Fabric
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_chapter_text.*
import ru.profapp.ranobe.BuildConfig
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.common.MyExceptionHandler
import ru.profapp.ranobe.helpers.LogType
import ru.profapp.ranobe.helpers.ThemeHelper
import ru.profapp.ranobe.helpers.logError
import ru.profapp.ranobe.helpers.logMessage
import ru.profapp.ranobe.models.*
import ru.profapp.ranobe.network.repositories.RanobeHubRepository
import ru.profapp.ranobe.network.repositories.RanobeRfRepository
import ru.profapp.ranobe.network.repositories.RulateRepository
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ChapterTextActivity : AppCompatActivity() {

    lateinit var mCurrentChapter: Chapter
    private lateinit var mContext: Context
    private var hChapterUrl: String? = null
    private var chapterIndex: Int = 0
    var mProgress: Float = -1f
    private var mChapterCount: Int = 0
    private var mChapterList: List<Chapter> = ArrayList()

    private var lastChapterIdPref: SharedPreferences? = null
    private lateinit var currentRanobe: Ranobe
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    lateinit var bottomNavigationView: BottomNavigationView

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun setWebColors() {

        val settingPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val oldColor = settingPref.getBoolean(resources.getString(R.string.pref_general_app_theme), false)
        settingPref.edit().putBoolean(resources.getString(R.string.pref_general_app_theme), !oldColor).apply()
        ThemeHelper.setTheme(!oldColor)
        ThemeHelper.onActivityCreateSetTheme()
        this.recreate()

    }

    private fun calculateProgression(): Float {
        val positionTopView = textWebview.top.toFloat()
        val contentHeight = textWebview.contentHeight.toFloat()
        val currentScrollPosition = textWebview.scrollY.toFloat()
        return (currentScrollPosition - positionTopView) / contentHeight
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!MyApp.isApplicationInitialized || MyApp.ranobe == null) {
            val firstIntent = Intent(this, MainActivity::class.java)

            firstIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // So all other activities will be dumped
            startActivity(firstIntent)

            // We are done, so finish this activity and get out now
            finish()
            return
        }



        Fabric.with(this, Crashlytics())

        hideSystemUI()

        setContentView(R.layout.activity_chapter_text)

        val apprate = AppRate.with(this)
                .setStoreType(StoreType.GOOGLEPLAY)
                .setTimeToWait(Time.DAY, 10) // default is 10 days, 0 means install millisecond, 10 means app is launched 10 or more time units later than installation
                .setLaunchTimes(10)           // default is 10, 3 means app is launched 3 or more times
                .setRemindTimeToWait(Time.DAY, 2) // default is 1 day, 1 means app is launched 1 or more time units after neutral button clicked
                .setRemindLaunchesNumber(1)  // default is 0, 1 means app is launched 1 or more times after neutral button clicked
                .setSelectedAppLaunches(1)   // default is 1, 1 means each launch, 2 means every 2nd launch, 3 means every 3rd launch, etc
                .setShowLaterButton(true)           // default is true, true means to show the Neutral button ("Remind me later").
                .set365DayPeriodMaxNumberDialogLaunchTimes(3) // default is unlimited, 3 means 3 or less occurrences of the display of the Rate Dialog within a 365-day period
                .setVersionCodeCheck(true)          // default is false, true means to re-enable the Rate Dialog if a new version of app with different version code is installed
                .setVersionNameCheck(true)          // default is false, true means to re-enable the Rate Dialog if a new version of app with different version name is installed

                .setOnClickButtonListener { it ->
                    when (it.toInt()) {
                        DialogInterface.BUTTON_NEGATIVE -> logMessage(LogType.INFO, "Rate", "NEGATIVE")
                        DialogInterface.BUTTON_NEUTRAL -> logMessage(LogType.INFO, "Rate", "NEUTRAL")
                        DialogInterface.BUTTON_POSITIVE -> logMessage(LogType.INFO, "Rate", "POSITIVE")
                        else -> logMessage(LogType.INFO, "Rate", "ELSE $it")
                    }
                }

        if (BuildConfig.DEBUG) {
            apprate.isDebug = true  // default is false, true is for development only, true ensures that the Rate Dialog will be shown each time the app is launched
        }

        apprate.monitor()                         // Monitors the app launch times

        if (AppRate.with(this).storeType == StoreType.GOOGLEPLAY) { // Checks that current app store type from library options is StoreType.GOOGLEPLAY
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SERVICE_MISSING) { // Checks that Google Play is available
                AppRate.showRateDialogIfMeetsConditions(this) // Shows the Rate Dialog when conditions are met
            }
        } else {
            AppRate.showRateDialogIfMeetsConditions(this)     // Shows the Rate Dialog when conditions are met
        }

        //   AppRate.with(this).showRateDialog(this)

        bottomNavigationView = findViewById(R.id.button_layout)
        setupActionBar()

        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))

        if (savedInstanceState != null) {
            hChapterUrl = savedInstanceState.getString("ChapterUrl", null)
            mProgress = savedInstanceState.getFloat("Progress", -1f)
        } else {
            hChapterUrl = intent.getStringExtra("ChapterUrl")
            mProgress = intent.getFloatExtra("Progress", -1f)
        }

        mContext = this@ChapterTextActivity

        currentRanobe = MyApp.ranobe!!

        val tempList = currentRanobe.chapterList.filter { it -> it.canRead }

        mChapterList = if (tempList.any()) {
            tempList
        } else {
            currentRanobe.chapterList
        }

        mChapterCount = mChapterList.size

        if (hChapterUrl != null) {
            mCurrentChapter = mChapterList.firstOrNull { it.url == hChapterUrl } ?: mChapterList[0]
            chapterIndex = mChapterList.indexOf(mCurrentChapter)
        } else {
            try {
                chapterIndex = mChapterCount - 1
                mCurrentChapter = mChapterList[chapterIndex]
            } catch (e: ArrayIndexOutOfBoundsException) {
                chapterIndex = 0
                mCurrentChapter = Chapter().apply {
                    title = "Not found"
                    text = "Not found"
                }
                logError(LogType.ERROR, "ChapterTextActivity", currentRanobe.url, e)
            }
        }
        textWebview.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                supportActionBar?.title = mCurrentChapter.title
            }

            override fun onPageFinished(view: WebView, url: String) {

                view.postDelayed(object : Runnable {
                    override fun run() {
                        if (view.progress == 100) {
                            view.postDelayed({

                                var mScrollY = 0
                                if (mProgress > -1) {
                                    val webviewsize = (view.contentHeight - view.top).toFloat()
                                    val positionInWV = webviewsize * mProgress
                                    mScrollY = Math.round(view.top + positionInWV)
                                    mProgress = -1f
                                }

                                view.scrollTo(0, mScrollY)

                            }, 300)
                        } else {
                            view.post(this)
                        }
                    }
                }, 300)

                webViewProgressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }

        }
        textWebview.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        textWebview.settings.builtInZoomControls = false
        textWebview.settings.displayZoomControls = false

        textWebview.setBackgroundColor(resources.getColor(R.color.webViewBackground))

        textWebview.appbar = appbar_chT
        textWebview.bottomNavigationView = bottomNavigationView
        textWebview.isHorizontalScrollBarEnabled = false

        lastChapterIdPref = applicationContext.getSharedPreferences(Constants.last_chapter_id_Pref, Context.MODE_PRIVATE)

        initWebView()

        if (!currentRanobe.url.isBlank() && !currentRanobe.title.isBlank()) {
            val request = Completable.fromAction {
                MyApp.database.ranobeHistoryDao().insertNewRanobe(
                        RanobeHistory(currentRanobe.url, currentRanobe.title, currentRanobe.description)
                )
            }.subscribeOn(Schedulers.io()).subscribe({}, { error ->
                logError(LogType.ERROR, "", "", error, false)
            })
            compositeDisposable.add(request)
        }



        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_prev -> {
                    OnClicked(+1)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.navigation_next -> {
                    OnClicked(-1)
                    return@setOnNavigationItemSelectedListener true

                }

                R.id.navigation_bookmark -> {
                    saveProgressToDb()
                    Toast.makeText(mContext, getString(R.string.bookmark_added), Toast.LENGTH_SHORT).show()

                    return@setOnNavigationItemSelectedListener true
                }
                else -> {
                    return@setOnNavigationItemSelectedListener false
                }

            }
        }

        bottomNavigationView.menu.findItem(R.id.navigation_prev).isEnabled = (chapterIndex < mChapterCount - 1)
        bottomNavigationView.menu.findItem(R.id.navigation_next).isEnabled = (chapterIndex > 0)

    }

    private fun initWebView() {

        @ColorInt
        val color = resources.getColor(R.color.webViewText)
        @ColorInt
        val color2 = resources.getColor(R.color.webViewBackground)

        webViewProgressBar.visibility = View.VISIBLE

        val style = ("style = \"text-align: justify; text-indent: 20px;font-size: "
                + MyApp.chapterTextSize.toString() + "px;"
                + "color: " + String.format("#%06X", 0xFFFFFF and color)
                + "; background-color: " + String.format("#%06X", 0xFFFFFF and color2)
                + "\"")

        val request = GetChapterText()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    if (result) {
                        putToReaded()
                    }

                    val summary = ("<html><style>img{display: inline;height: auto;max-width: 90%;}</style><body "
                            + style + ">"
                            + "<b>" + mCurrentChapter.title + "</b>" + "</br>"
                            + (mCurrentChapter.text
                            ?: getString(R.string.no_access)) + "</body></html>")

                    textWebview.loadDataWithBaseURL("https:\\\\" + mCurrentChapter.url + "/", summary, "text/html", "UTF-8", null)

                }, { error ->

                    if (error is UnknownHostException || error is SocketTimeoutException || error is SocketException)
                        Toast.makeText(mContext, R.string.error_connection, Toast.LENGTH_SHORT).show()
                    else
                        logError(LogType.ERROR, "GetChapterText", mCurrentChapter.url, error.fillInStackTrace())

                    val summary = ("<html><style>img{display: inline;height: auto;max-width: 90%;}</style><body "
                            + style + ">"
                            + "<b>" + mCurrentChapter.title + "</b>" + "</br>"
                            + (mCurrentChapter.text ?: "Нет доступа") + "</body></html>")

                    textWebview.loadDataWithBaseURL("https:\\\\" + mCurrentChapter.url + "/", summary, "text/html", "UTF-8", null)

                })

        compositeDisposable.add(request)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)

                startActivity(intent)
            }
            R.id.navigation_day_night -> setWebColors()
            R.id.navigation_open_in_browser -> {
                var url = mCurrentChapter.url

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }

                if (url.contains(Constants.RanobeSite.Rulate.url)) {
                    url = "$url/ready"
                }

                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if (browserIntent.resolveActivity(this.packageManager) != null)
                    startActivity(browserIntent)
                else
                    Toast.makeText(this, R.string.browser_exist, Toast.LENGTH_SHORT).show()

            }
        }

        return true

    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_chT)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun GetChapterText(chapter: Chapter, context: Context): Single<Boolean> {
        mContext = context
        mCurrentChapter = chapter
        return GetChapterText()

    }

    private fun GetChapterTextFromWeb(url: String): Single<Boolean> {
        return when {
            url.contains(Constants.RanobeSite.Rulate.url) -> getRulateChapterText()
            url.contains(Constants.RanobeSite.RanobeRf.url) -> getRanobeRfChapterText()
            url.contains(Constants.RanobeSite.RanobeHub.url) -> getRanobeHubChapterText()
            else -> Single.just(false)
        }
    }

    private fun GetChapterText(): Single<Boolean> {

        return if (mCurrentChapter.text.isNullOrBlank() || mCurrentChapter.text.equals("null")) {

            return MyApp.database.textDao().getTextByChapterUrl(mCurrentChapter.url).map {
                if (!it.text.isBlank() && it.text != "null") {
                    mCurrentChapter.text = it.text
                    return@map true
                } else {
                    Completable.fromAction {
                        MyApp.database.textDao().delete(mCurrentChapter.url)
                    }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                        logError(LogType.ERROR, "", "", error, false)
                    })

                    return@map false
                }

            }.switchIfEmpty(Single.just(false))
                    .flatMap { itf ->

                        if (!itf) {
                            return@flatMap GetChapterTextFromWeb(mCurrentChapter.ranobeUrl)
                                    .map {
                                        if (!mCurrentChapter.text.isNullOrBlank() && it && !mCurrentChapter.text.equals("null")) {
                                            Completable.fromAction {
                                                MyApp.database.textDao().insert(TextChapter(mCurrentChapter))
                                            }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                                                logError(LogType.ERROR, "", "", error, false)
                                            })

                                        }
                                        return@map it
                                    }
                        }

                        return@flatMap Single.just(itf)
                    }

        } else {
            Single.just(true)
        }

    }

    private fun getRulateChapterText(): Single<Boolean> {
        val preferences = applicationContext.getSharedPreferences(Constants.Rulate_Login_Pref, 0)
        val token: String = preferences.getString(Constants.KEY_Token, "") ?: ""

        return RulateRepository.getChapterText(token, mCurrentChapter)

    }

    private fun getRanobeRfChapterText(): Single<Boolean> {
        return RanobeRfRepository.getChapterText(mCurrentChapter)
    }

    private fun getRanobeHubChapterText(): Single<Boolean> {
        return RanobeHubRepository.getChapterText(mCurrentChapter)
    }

    private fun saveProgressToDb(pr: Float? = null) {
        if (!mCurrentChapter.text.isNullOrBlank()) {
            val progress = pr ?: calculateProgression() ?: 0f
            val request = Completable.fromAction {
                MyApp.database.chapterProgressDao().insert(
                        ChapterProgress(mCurrentChapter.url, mCurrentChapter.ranobeUrl, progress)
                )
            }.subscribeOn(Schedulers.io())
                    .subscribe({}, { error ->
                        logError(LogType.ERROR, "saveProgressToDb", "", error, false)
                    })
            compositeDisposable.add(request)
        }
    }

    private fun saveHistoryToDb() {
        if (!mCurrentChapter.text.isNullOrBlank()) {
            val request = Completable.fromAction {
                MyApp.database.ranobeHistoryDao().insertNewChapter(
                        ChapterHistory(mCurrentChapter.url, mCurrentChapter.title, mCurrentChapter.ranobeName, mCurrentChapter.ranobeUrl, mCurrentChapter.index)
                )
            }.subscribeOn(Schedulers.io())
                    .subscribe({}, { error ->
                        logError(LogType.ERROR, "saveHistoryToDb", "", error, false)
                    })
            compositeDisposable.add(request)
        }
    }

    private fun OnClicked(i: Int) {

        val prevChapterIndex = chapterIndex
        chapterIndex += i
        if (chapterIndex >= 0 && chapterIndex <= mChapterCount - 1) {
            try {
                mCurrentChapter = mChapterList[chapterIndex]
                initWebView()
                mChapterList[prevChapterIndex].text = null

            } catch (e: ArrayIndexOutOfBoundsException) {
                chapterIndex = prevChapterIndex
            }

        } else {
            Toast.makeText(mContext, R.string.not_exist, Toast.LENGTH_SHORT).show()
            chapterIndex = prevChapterIndex
        }

        bottomNavigationView.menu.findItem(R.id.navigation_prev).isEnabled = (chapterIndex < mChapterCount - 1)
        bottomNavigationView.menu.findItem(R.id.navigation_next).isEnabled = (chapterIndex > 0)

    }

    private fun putToReaded() {

        if (lastChapterIdPref == null) {
            lastChapterIdPref = applicationContext.getSharedPreferences(Constants.last_chapter_id_Pref, Context.MODE_PRIVATE)
        }
        mCurrentChapter.isRead = true

        mCurrentChapter.id?.let { lastChapterIdPref!!.edit().putInt(mCurrentChapter.ranobeUrl, it).apply() }

        saveHistoryToDb()

        if (MyApp.autoAddBookmark) {
            saveProgressToDb(0f)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chaptermain, menu)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState)
        // Save our own state now
        outState.putString("ChapterUrl", mCurrentChapter.url)
        outState.putFloat("Progress", calculateProgression())
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode

        if (!MyApp.useVolumeButtonsToScroll) {
            return super.dispatchKeyEvent(event)
        } else {
            return when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        textWebview.pageUp(false)
                    }
                    true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        textWebview.pageDown(false)
                    }
                    true
                }
                else -> super.dispatchKeyEvent(event)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bottomNavigationView.menu.findItem(R.id.navigation_bookmark).isVisible = !MyApp.autoAddBookmark
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()
        if (MyApp.autoAddBookmark) {
            saveProgressToDb()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Thread.setDefaultUncaughtExceptionHandler(null)
        compositeDisposable.clear()
    }

}


