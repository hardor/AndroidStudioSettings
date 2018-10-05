package ru.profapp.RanobeReaderTest.Activities

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
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReaderTest.Common.Constants
import ru.profapp.RanobeReaderTest.Common.ThemeUtils
import ru.profapp.RanobeReaderTest.Helpers.LogHelper
import ru.profapp.RanobeReaderTest.JsonApi.RanobeHubRepository
import ru.profapp.RanobeReaderTest.JsonApi.RanobeRfRepository
import ru.profapp.RanobeReaderTest.JsonApi.RulateRepository
import ru.profapp.RanobeReaderTest.Models.*
import ru.profapp.RanobeReaderTest.MyApp
import ru.profapp.RanobeReaderTest.R

class ChapterTextActivity : AppCompatActivity() {

    lateinit var mCurrentChapter: Chapter
    private lateinit var mWebView: WebView
    private var mContext: Context? = null
    private var mIndex: Int = 0
    private var chIndex: Int = 0
    var mProgress: Float = -1f
    private var mChapterCount: Int? = null
    private var mChapterList: List<Chapter> = ArrayList()

    private var lastIndexPref: SharedPreferences? = null
    private lateinit var nextMenu: ImageButton
    private lateinit var prevMenu: ImageButton

    var currentRanobe: Ranobe? = null

    private lateinit var progressBar: ProgressBar

    private fun set_web_colors() {

        val settingPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val oldColor = settingPref.getBoolean(resources.getString(R.string.pref_general_app_theme), false)
        settingPref.edit().putBoolean(resources.getString(R.string.pref_general_app_theme), !oldColor).commit()
        ThemeUtils.setTheme(!oldColor)
        ThemeUtils.onActivityCreateSetTheme()
        this.recreate()

    }

    private fun calculateProgression(): Float {
        val positionTopView = mWebView.top.toFloat()
        val contentHeight = mWebView.contentHeight.toFloat()
        val currentScrollPosition = mWebView.scrollY.toFloat()
        return (currentScrollPosition - positionTopView) / contentHeight
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setupActionBar()
        setContentView(R.layout.activity_chapter_text)

        if (savedInstanceState != null) {
            mIndex = savedInstanceState.getInt("ChapterIndex", -1)
            mProgress = savedInstanceState.getFloat("Progress", -1f)
        } else {
            chIndex = intent.getIntExtra("ChapterIndex", 0)
            mProgress = intent.getFloatExtra("Progress", -1f)
        }

        mContext = this@ChapterTextActivity

        currentRanobe = MyApp.ranobe
        mChapterList = currentRanobe?.chapterList?.filter { it -> it.canRead } ?: mChapterList

        if (mIndex >= 0) {
            mCurrentChapter = mChapterList[mIndex]
        } else {
            mCurrentChapter = mChapterList.firstOrNull { it.index == mIndex } ?: mChapterList.first()
            mIndex = mChapterList.indexOf(mCurrentChapter)
        }

        mCurrentChapter = mChapterList.firstOrNull { it.index == mIndex } ?: mChapterList.first()
        mChapterCount = mChapterList.size

        progressBar = findViewById(R.id.progressBar2)

        mWebView = findViewById(R.id.textWebview)
        mWebView.webViewClient = object : WebViewClient() {

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

                progressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }

        }
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // mWebView.getSettings().setBuiltInZoomControls(true);
        // TODO: check zooming
        mWebView.settings.displayZoomControls = true
        mWebView.setInitialScale(1)
        mWebView.settings.loadWithOverviewMode = true
        mWebView.settings.useWideViewPort = true

        mWebView.setBackgroundColor(resources.getColor(R.color.webViewBackground))

        lastIndexPref = getSharedPreferences(Constants.last_chapter_id_Pref, Context.MODE_PRIVATE)

        initWebView()

        Completable.fromAction {
            MyApp.database?.ranobeHistoryDao()?.insertNewRanobe(
                    RanobeHistory(currentRanobe?.url!!, currentRanobe?.title!!, currentRanobe?.description)
            )
        }?.subscribeOn(Schedulers.io())?.subscribe()

        prevMenu = findViewById(R.id.navigation_prev)
        nextMenu = findViewById(R.id.navigation_next)


        prevMenu.visibility = if (mIndex < mChapterCount!! - 1) View.VISIBLE else View.INVISIBLE
        nextMenu.visibility = if (mIndex > 0) View.VISIBLE else View.INVISIBLE

        nextMenu.setOnClickListener { OnClicked(-1) }

        prevMenu.setOnClickListener { OnClicked(+1) }

    }

    private fun initWebView() {

        @ColorInt
        val color = resources.getColor(R.color.webViewText)
        @ColorInt
        val color2 = resources.getColor(R.color.webViewBackground)

        progressBar.visibility = View.VISIBLE

        val style = ("style = \"text-align: justify; text-indent: 20px;font-size: "
                + MyApp.chapterTextSize!!.toString() + "px;"
                + "color: " + String.format("#%06X", 0xFFFFFF and color)
                + "; background-color: " + String.format("#%06X", 0xFFFFFF and color2)
                + "\"")


        GetChapterText(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    if (!mCurrentChapter.isRead && result!!) {
                        putToReaded()
                    }

                    val summary = ("<html><style>img{display: inline;height: auto;max-width: 90%;}</style><body "
                            + style + ">" + "<b>" + mCurrentChapter.title + "</b>" + "</br>"
                            + mCurrentChapter.text + "</body></html>")

                    mWebView.loadDataWithBaseURL("https:\\\\" + mCurrentChapter.url + "/", summary, "text/html", "UTF-8", null)

                }, { error ->
                    LogHelper.logError(LogHelper.LogType.ERROR, "GetChapterText", "", error.fillInStackTrace())
                    val summary = ("<html><style>img{display: inline;height: auto;max-width: 90%;}</style><body "
                            + style + ">" + "<b>" + mCurrentChapter.title + "</b>" + "</br>"
                            + mCurrentChapter.text + "</body></html>")

                    mWebView.loadDataWithBaseURL("https:\\\\" + mCurrentChapter.url + "/", summary, "text/html", "UTF-8", null)

                })

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

    fun GetChapterText(chapter: Chapter, context: Context): Boolean {
        mContext = context
        mCurrentChapter = chapter

        return GetChapterText(true)
                .subscribeOn(Schedulers.io())
                .blockingGet()

    }

    private fun GetChapterText(needSave: Boolean): Single<Boolean> {

        return if (mCurrentChapter.text.isNullOrBlank()) {

            return MyApp.database?.textDao()!!.getTextByChapterUrl(mCurrentChapter.url).map {
                mCurrentChapter.text = it.text
                return@map true
            }.onErrorResumeNext {

                val url = mCurrentChapter.ranobeUrl
                when {
                    url.contains(Constants.RanobeSite.Rulate.url) -> return@onErrorResumeNext getRulateChapterText()
                    url.contains(Constants.RanobeSite.RanobeRf.url) -> return@onErrorResumeNext getRanobeRfChapterText()
                    url.contains(Constants.RanobeSite.RanobeHub.url) -> return@onErrorResumeNext getRanobeHubChapterText()
                    else -> return@onErrorResumeNext Single.just(false)
                }

            }

        } else {
            Single.just(true)
        }.map {

            if ((MyApp.autoSaveText || needSave) && !mCurrentChapter.text.isNullOrBlank() && it) {
                Completable.fromAction {
                    MyApp.database?.textDao()?.insert(TextChapter(mCurrentChapter))
                }?.subscribeOn(Schedulers.io())?.subscribe()

            }
            return@map it
        }

    }

    private fun getRulateChapterText(): Single<Boolean> {
        val preferences = mContext!!.getSharedPreferences(Constants.Rulate_Login_Pref, 0)
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
        val progress = pr ?: calculateProgression()
        Completable.fromAction {
            MyApp.database?.ranobeHistoryDao()?.insertNewChapter(
                    ChapterHistory(mCurrentChapter.url, mCurrentChapter.title, mCurrentChapter.ranobeName, mCurrentChapter.index, progress)
            )
        }?.subscribeOn(Schedulers.io())?.subscribe()
    }

    private fun OnClicked(i: Int) {

        mIndex += i
        if (mIndex >= 0 && mIndex <= mChapterCount!! - 1) {
            try {
                saveProgressToDb()
                mCurrentChapter = mChapterList[mIndex]
                initWebView()
            } catch (e: ArrayIndexOutOfBoundsException) {
                mIndex -= i
            }

        } else {
            Toast.makeText(mContext, R.string.not_exist, Toast.LENGTH_SHORT).show()
            mIndex -= i
        }

        prevMenu.visibility = if (mIndex < mChapterCount!! - 1) View.VISIBLE else View.INVISIBLE
        nextMenu.visibility = if (mIndex > 0) View.VISIBLE else View.INVISIBLE

    }

    private fun putToReaded() {

        //Todo: add to history table
        if (lastIndexPref == null) {
            lastIndexPref = mContext!!.getSharedPreferences(Constants.last_chapter_id_Pref, Context.MODE_PRIVATE)
        }
        mCurrentChapter.isRead = true

        mCurrentChapter.id?.let { lastIndexPref!!.edit().putInt(mCurrentChapter.ranobeUrl, it).commit() }

        saveProgressToDb(0f)

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
        outState.putInt("ChapterIndex", mIndex)
        outState.putFloat("Progress", calculateProgression())
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode

        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    mWebView.pageUp(false)
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    mWebView.pageDown(false)
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    override fun onPause() {
        super.onPause()
        saveProgressToDb()
    }

}


