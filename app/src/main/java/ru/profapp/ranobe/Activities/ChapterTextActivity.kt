package ru.profapp.ranobe.Activities

import android.content.Context
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
import io.fabric.sdk.android.Fabric
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_chapter_text.*
import ru.profapp.ranobe.Common.Constants
import ru.profapp.ranobe.Common.MyExceptionHandler
import ru.profapp.ranobe.Helpers.LogHelper
import ru.profapp.ranobe.Helpers.ThemeHelper
import ru.profapp.ranobe.Models.*
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.Network.Repositories.RanobeHubRepository
import ru.profapp.ranobe.Network.Repositories.RanobeRfRepository
import ru.profapp.ranobe.Network.Repositories.RulateRepository
import ru.profapp.ranobe.R
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
    private var currentRanobe: Ranobe? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private fun set_web_colors() {

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

        if (!MyApp.isApplicationInitialized) {
            val firstIntent = Intent(this, MainActivity::class.java)

            firstIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // So all other activities will be dumped
            startActivity(firstIntent)

            // We are done, so finish this activity and get out now
            finish()
            return
        }


        Fabric.with(this, Crashlytics())
        setupActionBar()
        setContentView(R.layout.activity_chapter_text)
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))

        if (savedInstanceState != null) {
            hChapterUrl = savedInstanceState.getString("ChapterUrl", null)
            mProgress = savedInstanceState.getFloat("Progress", -1f)
        } else {
            hChapterUrl = intent.getStringExtra("ChapterUrl")
            mProgress = intent.getFloatExtra("Progress", -1f)
        }

        mContext = this@ChapterTextActivity

        currentRanobe = MyApp.ranobe
        mChapterList = currentRanobe?.chapterList?.filter { it -> it.canRead } ?: mChapterList


        mChapterCount = mChapterList.size

        if (hChapterUrl != null) {
            mCurrentChapter = mChapterList.first { it.url == hChapterUrl }
            chapterIndex = mChapterList.indexOf(mCurrentChapter)
        } else {
            chapterIndex = mChapterCount - 1
            mCurrentChapter = mChapterList[chapterIndex]
        }
        textWebview.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                title = mCurrentChapter.title
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

        lastChapterIdPref = getSharedPreferences(Constants.last_chapter_id_Pref, Context.MODE_PRIVATE)

        initWebView()

        if (currentRanobe != null && !currentRanobe?.url.isNullOrBlank() && !currentRanobe?.title.isNullOrBlank()) {
            val request = Completable.fromAction {
                MyApp.database.ranobeHistoryDao().insertNewRanobe(
                        RanobeHistory(currentRanobe!!.url, currentRanobe!!.title, currentRanobe!!.description)
                )
            }.subscribeOn(Schedulers.io()).subscribe({}, { error ->
                LogHelper.logError(LogHelper.LogType.ERROR, "", "", error, false)
            })
            compositeDisposable.add(request)
        }

        navigation_prev.visibility = if (chapterIndex < mChapterCount - 1) View.VISIBLE else View.INVISIBLE
        navigation_next.visibility = if (chapterIndex > 0) View.VISIBLE else View.INVISIBLE

        navigation_next.setOnClickListener { OnClicked(-1) }

        navigation_prev.setOnClickListener { OnClicked(+1) }

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

                    if (error is UnknownHostException || error is SocketTimeoutException)
                        Toast.makeText(mContext, R.string.error_connection, Toast.LENGTH_SHORT).show()
                    else
                        LogHelper.logError(LogHelper.LogType.ERROR, "GetChapterText", "", error.fillInStackTrace())

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
            R.id.navigation_day_night -> set_web_colors()
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
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
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
                        LogHelper.logError(LogHelper.LogType.ERROR, "", "", error, false)
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
                                                LogHelper.logError(LogHelper.LogType.ERROR, "", "", error, false)
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
        val preferences = mContext.getSharedPreferences(Constants.Rulate_Login_Pref, 0)
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
                        LogHelper.logError(LogHelper.LogType.ERROR, "saveProgressToDb", "", error, false)
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
                        LogHelper.logError(LogHelper.LogType.ERROR, "saveHistoryToDb", "", error, false)
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
                chapterIndex =prevChapterIndex
            }

        } else {
            Toast.makeText(mContext, R.string.not_exist, Toast.LENGTH_SHORT).show()
            chapterIndex = prevChapterIndex
        }

        navigation_prev.visibility = if (chapterIndex < mChapterCount - 1) View.VISIBLE else View.INVISIBLE
        navigation_next.visibility = if (chapterIndex > 0) View.VISIBLE else View.INVISIBLE

    }

    private fun putToReaded() {

        if (lastChapterIdPref == null) {
            lastChapterIdPref = mContext.getSharedPreferences(Constants.last_chapter_id_Pref, Context.MODE_PRIVATE)
        }
        mCurrentChapter.isRead = true

        mCurrentChapter.id?.let { lastChapterIdPref!!.edit().putInt(mCurrentChapter.ranobeUrl, it).apply() }

        saveHistoryToDb()

        if (MyApp.autoAddBookmark) {
            saveProgressToDb(0f)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        if (MyApp.autoAddBookmark) {
            navigation_bookmark.visibility = View.GONE
        } else {
            navigation_bookmark.visibility = View.VISIBLE
            navigation_bookmark.setOnClickListener {
                saveProgressToDb()
                Toast.makeText(mContext, getString(R.string.bookmark_added), Toast.LENGTH_SHORT).show()
            }
        }
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


