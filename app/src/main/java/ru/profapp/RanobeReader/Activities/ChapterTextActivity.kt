package ru.profapp.RanobeReader.Activities

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
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import io.fabric.sdk.android.Fabric
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.Common.RanobeConstants
import ru.profapp.RanobeReader.Common.StringResources
import ru.profapp.RanobeReader.Common.StringResources.Chapter_Url
import ru.profapp.RanobeReader.Common.ThemeUtils
import ru.profapp.RanobeReader.Fragments.RepositoryProvider
import ru.profapp.RanobeReader.Helpers.MyLog
import ru.profapp.RanobeReader.Helpers.RanobeKeeper
import ru.profapp.RanobeReader.Helpers.StringHelper
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfChapterTextGson
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.TextChapter
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R

class ChapterTextActivity : AppCompatActivity() {

    val gson = GsonBuilder().setLenient().create()!!
    lateinit var mCurrentChapter: Chapter
    private lateinit var mWebView: WebView
    private var mContext: Context? = null
    private var mIndex: Int = 0
    var mProgress: Float = 0.toFloat()
    private var mChapterCount: Int? = null
    private var mChapterList: List<Chapter> = ArrayList()
    private var sPref: SharedPreferences? = null
    private var lastIndexPref: SharedPreferences? = null
    private var sChapterPref: SharedPreferences? = null
    private lateinit var nextMenu: ImageButton
    private lateinit var prevMenu: ImageButton
    private lateinit var bookmarkMenu: ImageButton

    private var progressUrl: ProgressBar? = null

    private fun set_web_colors() {

        val settingPref = PreferenceManager.getDefaultSharedPreferences(
                applicationContext)
        val oldColor = settingPref.getBoolean(
                resources.getString(R.string.pref_general_app_theme), false)
        settingPref.edit().putBoolean(resources.getString(R.string.pref_general_app_theme), !oldColor).commit()
        ThemeUtils.setTheme(!oldColor)
        ThemeUtils.onActivityCreateSetTheme()
        this.recreate()

    }

    private fun set_bookmark() {

        sChapterPref = mContext!!.getSharedPreferences(StringHelper.CleanString(mCurrentChapter.ranobeUrl),
                Context.MODE_PRIVATE)

        sChapterPref!!.edit().putFloat(StringResources.Chapter_Position, this.calculateProgression()).commit()
        sChapterPref!!.edit().putString(Chapter_Url, mCurrentChapter.url).commit()
        Toast.makeText(mContext, getString(R.string.bookmark_saved), Toast.LENGTH_SHORT).show()
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
            mIndex = savedInstanceState.getInt("ChapterIndex", intent.getIntExtra("ChapterIndex", 0))
            mProgress = savedInstanceState.getFloat("Progress", -1f)
        } else {
            mIndex = intent.getIntExtra("ChapterIndex", 0)
            mProgress = intent.getFloatExtra("Progress", -1f)
        }

        mContext = this@ChapterTextActivity

        val currentRanobe = MyApp.ranobe
        mChapterList = currentRanobe?.chapterList ?: mChapterList

        mCurrentChapter = mChapterList[mIndex]
        mChapterCount = mChapterList.size


        progressUrl = findViewById(R.id.progressBar2)

        mWebView = findViewById(R.id.textWebview)
        mWebView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                title = mCurrentChapter.title
                progressUrl!!.visibility = View.VISIBLE
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

                progressUrl!!.visibility = View.GONE
                super.onPageFinished(view, url)
            }

        }
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        if (intent.getBooleanExtra("Bookmark", false)) {
            sChapterPref = mContext!!.getSharedPreferences(
                    StringHelper.CleanString(mCurrentChapter.ranobeUrl),
                    Context.MODE_PRIVATE)
            if (sChapterPref != null) {

                for ((key1, result) in sChapterPref!!.all) {
                    if (key1 == StringResources.Chapter_Position) {

                        if (result is Int) {
                            mProgress = result.toFloat()
                        } else if (result is Float) {
                            mProgress = result
                        }

                        break
                    }
                }

            } else {
                mProgress = -1f
            }
        }

        // mWebView.getSettings().setBuiltInZoomControls(true);
        // mWebView.getSettings().setDisplayZoomControls(false);

        mWebView.setBackgroundColor(resources.getColor(R.color.webViewBackground))

        sPref = getSharedPreferences(StringResources.is_readed_Pref, Context.MODE_PRIVATE)
        lastIndexPref = getSharedPreferences(StringResources.last_chapter_id_Pref, Context.MODE_PRIVATE)

        initWebView()


        prevMenu = findViewById(R.id.navigation_prev)
        nextMenu = findViewById(R.id.navigation_next)
        bookmarkMenu = findViewById(R.id.navigation_bookmark)

        prevMenu.visibility = if (mIndex < mChapterCount!! - 1) View.VISIBLE else View.INVISIBLE
        nextMenu.visibility = if (mIndex > 0) View.VISIBLE else View.INVISIBLE

        nextMenu.setOnClickListener { OnClicked(-1) }

        prevMenu.setOnClickListener { OnClicked(+1) }
        bookmarkMenu.setOnClickListener { set_bookmark() }


    }

    private fun initWebView() {
        @ColorInt val color = resources.getColor(R.color.webViewText)
        @ColorInt val color2 = resources.getColor(R.color.webViewBackground)
        val style = ("style = \"text-align: justify; text-indent: 20px;font-size: "
                + RanobeKeeper.chapterTextSize!!.toString() + "px;"
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

                    mWebView.loadDataWithBaseURL("https:\\\\" + mCurrentChapter.url + "/", summary,
                            "text/html", "UTF-8", null)

                }, { error ->
                    MyLog.SendError(MyLog.LogType.ERROR, "GetChapterText", "", error.fillInStackTrace())
                    val summary = ("<html><style>img{display: inline;height: auto;max-width: 90%;}</style><body "
                            + style + ">" + "<b>" + mCurrentChapter.title + "</b>" + "</br>"
                            + mCurrentChapter.text + "</body></html>")

                    mWebView.loadDataWithBaseURL("https:\\\\" + mCurrentChapter.url + "/", summary,
                            "text/html", "UTF-8", null)

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

                if (url.contains(RanobeConstants.RanobeSite.Rulate.url)) {
                    url = "$url/ready"
                }
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    if (browserIntent.resolveActivity(this.packageManager) != null)
                        startActivity(browserIntent)
                    else
                        Toast.makeText(this, R.string.browser_exist, Toast.LENGTH_SHORT).show()


                } catch (ignored: Exception) {

                }

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
        return true
        //return GetChapterText(true)
    }

    private fun GetChapterText(needSave: Boolean): Observable<Boolean> {


          var ch =  MyApp.database?.textDao()!!.getTextByChapterUrl(mCurrentChapter.url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                    .blockingGet(TextChapter("e","e","e","e",2))


        if (mCurrentChapter.text == null || mCurrentChapter.text!!.isEmpty()) {

            return MyApp.database?.textDao()!!.getTextByChapterUrl(mCurrentChapter.url).flatMapObservable {
                if (it != null) {
                    mCurrentChapter.text = it.text
                } else {
                    val url = mCurrentChapter.ranobeUrl
                    if (url.contains(RanobeConstants.RanobeSite.Rulate.url))
                        return@flatMapObservable getRulateChapterText()
                    return@flatMapObservable Observable.just(false)
                }
                return@flatMapObservable Observable.just(false)
            }.map {

                if ((RanobeKeeper.autoSaveText || needSave) && mCurrentChapter.text.isNullOrBlank()) {
                    Completable.fromAction {
                        MyApp.database?.textDao()?.insert(
                                TextChapter(mCurrentChapter))
                    }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe()

                }
                return@map it
            }


        }
        return Observable.just(false)


    }

    private fun getRanobeRfChapterText(): Boolean {
        val response = JsonRanobeRfApi.getInstance()!!.GetChapterText(mCurrentChapter)
        try {
            val readyGson = gson.fromJson(response, RfChapterTextGson::class.java)
            if (readyGson.status == 200) {

                mCurrentChapter.UpdateChapter(readyGson.result!!)
                if (readyGson.result.part!!.payment!! && mCurrentChapter.text == "") {
                    mCurrentChapter.text = "Даннная страница находится на платной подписке"
                    return false
                }
                return true
            } else {
                mCurrentChapter.text = readyGson.message

            }
        } catch (e: JsonParseException) {
            MyLog.SendError(MyLog.LogType.WARN, ChapterTextActivity::class.java.toString(),
                    mCurrentChapter.url, e)
            return false
        }

        return false
    }

    private fun getRulateChapterText(): Observable<Boolean> {
        val preferences = mContext!!.getSharedPreferences(StringResources.Rulate_Login_Pref, 0)
        val token: String = preferences.getString(StringResources.KEY_Token, "")
        val repository = RepositoryProvider.provideRulateRepository()
        return repository.getChapterText(token, mCurrentChapter)

    }

    private fun OnClicked(i: Int) {

        mIndex += i
        if (mIndex >= 0 && mIndex <= mChapterCount!! - 1) {
            try {
                mCurrentChapter = mChapterList[mIndex]
                val loadResult = GetChapterText(false)
                //initWebView(loadResult)
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
//        if (sPref == null) {
//            sPref = mContext!!.getSharedPreferences(StringResources.is_readed_Pref, Context.MODE_PRIVATE)
//        }
        if (lastIndexPref == null) {
            lastIndexPref = mContext!!.getSharedPreferences(StringResources.last_chapter_id_Pref, Context.MODE_PRIVATE)
        }

        mCurrentChapter.isRead = true
        //  sPref!!.edit().putBoolean(ChapterUrl, true).commit()
        lastIndexPref!!.edit().putInt(mCurrentChapter.ranobeUrl, mCurrentChapter.id).commit()
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

}
