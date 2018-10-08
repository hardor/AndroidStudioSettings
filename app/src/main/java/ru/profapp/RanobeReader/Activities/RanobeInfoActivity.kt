package ru.profapp.RanobeReader.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.fabric.sdk.android.Fabric
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import ru.profapp.RanobeReader.Adapters.CommentsRecyclerViewAdapter
import ru.profapp.RanobeReader.Adapters.ExpandableChapterRecyclerViewAdapter
import ru.profapp.RanobeReader.BuildConfig
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Common.Constants.last_chapter_id_Pref
import ru.profapp.RanobeReader.Helpers.LogHelper
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R
import java.util.*

class RanobeInfoActivity : AppCompatActivity() {

    private val recycleChapterList = ArrayList<Chapter>()
    lateinit var chapterRecyclerView: RecyclerView
    var preferences: SharedPreferences? = null
    var rfpreferences: SharedPreferences? = null
    lateinit var currentRanobe: Ranobe
    var mContext: Context? = null
    private var notFavImage: Drawable? = null
    private var favImage: Drawable? = null
    lateinit var tabHost: TabHost
    private var adapterExpandable: ExpandableChapterRecyclerViewAdapter? = null
    private var sPref: SharedPreferences? = null
    private var lastIndexPref: SharedPreferences? = null
    private var mChapterLayoutManager: LinearLayoutManager? = null
    private var request: Disposable? = null

    private lateinit var aboutTextView: TextView
    private lateinit var additionalInfoTextView: TextView
    private lateinit var infoCard: CardView
    private lateinit var descriptionCard: CardView
    lateinit var imageView: ImageView
    private val imageOptions: RequestOptions = RequestOptions().placeholder(R.drawable.ic_adb_black_24dp).error(R.drawable.ic_error_outline_black_24dp).fitCenter()
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        // Set up Crashlytics, disabled for debug builds
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        Fabric.with(this, crashlyticsKit)
        setContentView(R.layout.activity_ranobe_info)

        initAds()

        mContext = this@RanobeInfoActivity
        currentRanobe = MyApp.ranobe!!

        notFavImage = mContext!!.resources.getDrawable(R.drawable.ic_favorite_border_black_24dp)
        favImage = mContext!!.resources.getDrawable(R.drawable.ic_favorite_black_24dp)

        val fab = findViewById<FloatingActionButton>(R.id.fav_button)
        fab.setOnClickListener { SetToFavorite(fab) }

        val bookmarkFab = findViewById<FloatingActionButton>(R.id.bookmark_fab)
        bookmarkFab.setOnClickListener {

            val chapterHistory = MyApp.database?.ranobeHistoryDao()?.getLastChapterByName(currentRanobe.title)?.subscribeOn(Schedulers.io())?.blockingGet()
            val intent = Intent(mContext, ChapterTextActivity::class.java)
            intent.putExtra("ChapterUrl", chapterHistory?.chapterUrl)
            intent.putExtra("Progress", chapterHistory?.progress)
            mContext!!.startActivity(intent)

        }

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        aboutTextView = findViewById(R.id.text_about)
        additionalInfoTextView = findViewById<TextView>(R.id.text_info)
        infoCard = findViewById<CardView>(R.id.ranobe_info_card)
        descriptionCard = findViewById<CardView>(R.id.ranobe_description_card)

        imageView = findViewById<ImageView>(R.id.main_logoimage)

        Glide.with(this).load(currentRanobe.image).apply(imageOptions).into(imageView)

        supportActionBar?.title = currentRanobe.title

        preferences = mContext!!.getSharedPreferences(Constants.Rulate_Login_Pref, 0)

        rfpreferences = mContext!!.getSharedPreferences(Constants.Ranoberf_Login_Pref, 0)

        chapterRecyclerView = findViewById(R.id.chapter_list)

        mChapterLayoutManager = LinearLayoutManager(mContext)
        chapterRecyclerView.layoutManager = mChapterLayoutManager
        chapterRecyclerView.onFlingListener = object : RecyclerView.OnFlingListener() {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                chapterRecyclerView.dispatchNestedFling(velocityX.toFloat(), velocityY.toFloat(), false)
                return false
            }
        }

        chapterRecyclerView.setHasFixedSize(true)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        loadData()
        loadChapters()

        tabHost = findViewById<TabHost>(R.id.tabHost)

        tabHost.setup()

        val tabSpec: TabHost.TabSpec = tabHost.newTabSpec("chapters")

        tabSpec.setContent(R.id.linearLayout)
        tabSpec.setIndicator(resources.getString(R.string.chapters))
        tabHost.addTab(tabSpec)
        tabHost.currentTab = 0
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkAction = intent.action
        val appLinkData: Uri? = intent.data

        // Todo: catch URLS
        if (Intent.ACTION_VIEW == appLinkAction) {
            appLinkData?.lastPathSegment?.also { recipeId ->
                //                Uri.parse("content://com.recipe_app/recipe/")
                //                        .buildUpon()
                //                        .appendPath(recipeId)
                //                        .build().also { appData ->
                //                            //  showRecipe(appData)
                //                        }
            }
        }
    }

    private fun initAds() {
        MobileAds.initialize(this, getString(R.string.app_admob_id))
        val adView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder()

        if (BuildConfig.DEBUG) {
            adRequest.addTestDevice("test")
        }

        adView.loadAd(adRequest.build())
    }

    private fun loadData() {
        if (!currentRanobe.description.isNullOrEmpty()) {
            descriptionCard.visibility = View.VISIBLE
        } else {
            descriptionCard.visibility = View.GONE
        }
        if (!currentRanobe.additionalInfo.isNullOrEmpty()) {
            infoCard.visibility = View.VISIBLE
        } else {
            infoCard.visibility = View.GONE
        }


        Glide.with(mContext!!).load(currentRanobe.image).apply(imageOptions).into(imageView)

        var aboutText = String.format("%s / %s \n\n%s\n\nРейтинг: %s", currentRanobe.title, currentRanobe.engTitle, currentRanobe.description, currentRanobe.rating)

        if (currentRanobe.genres != null) {
            aboutText = aboutText + "\n\n" + currentRanobe.genres
        }

        aboutTextView.text = aboutText
        additionalInfoTextView.text = currentRanobe.additionalInfo
    }

    private fun loadChapters() {
        recycleChapterList.clear()
        sPref = mContext!!.getSharedPreferences(last_chapter_id_Pref, Context.MODE_PRIVATE)
        lastIndexPref = mContext!!.getSharedPreferences(last_chapter_id_Pref, Context.MODE_PRIVATE)

        request = currentRanobe.updateRanobe(mContext!!).map {
            var checked = false
            if (lastIndexPref != null) {
                val lastId: Int = lastIndexPref?.getInt(it.url, -1) ?: -1
                if (lastId > 0) {
                    checked = true
                    for (chapter in it.chapterList) chapter.isRead = chapter.id!! < lastId

                }
            }

            if (sPref != null && !checked) {
                for (chapter in it.chapterList) {
                    if (!chapter.isRead) {
                        chapter.isRead = sPref?.getBoolean(chapter.url, false)!!
                    }
                }
            }
            return@map it
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    currentRanobe = result
                    progressBar.visibility = View.GONE
                    loadData()
                    recycleChapterList.addAll(result?.chapterList!!)
                    adapterExpandable = ExpandableChapterRecyclerViewAdapter(mContext!!, recycleChapterList, currentRanobe)
                    chapterRecyclerView.adapter = adapterExpandable

                    if (currentRanobe.comments.isNotEmpty()) {
                        val commentRecycleView = findViewById<RecyclerView>(R.id.comment_list)
                        commentRecycleView.layoutManager = LinearLayoutManager(mContext)
                        commentRecycleView.setHasFixedSize(true)
                        commentRecycleView.onFlingListener = object : RecyclerView.OnFlingListener() {
                            @RequiresApi(Build.VERSION_CODES.KITKAT)
                            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                                commentRecycleView.dispatchNestedFling(velocityX.toFloat(), velocityY.toFloat(), false)
                                return false
                            }
                        }

                        commentRecycleView.adapter = CommentsRecyclerViewAdapter(mContext!!, currentRanobe.comments)

                        val tabSpec: TabHost.TabSpec = tabHost.newTabSpec("comments")
                        tabSpec.setContent(R.id.linearLayout2)
                        tabSpec.setIndicator(resources.getString(R.string.comments))
                        tabHost.addTab(tabSpec)
                    }

                }, { error ->
                    LogHelper.logError(LogHelper.LogType.ERROR, "loadChapters", "", error.fillInStackTrace())
                    progressBar.visibility = View.GONE
                })

    }

    private fun getFavoriteIcon(item: MenuItem) {
        if (currentRanobe.isFavorite || currentRanobe.isFavoriteInWeb) {
            item.icon = favImage
        } else {
            MyApp.database?.ranobeDao()?.isRanobeFavorite(currentRanobe.url)?.observeOn(AndroidSchedulers.mainThread())?.subscribeOn(Schedulers.io())?.subscribe({
                if (it != null) {
                    currentRanobe.isFavorite = it.isFavorite
                    currentRanobe.isFavoriteInWeb = it.isFavoriteInWeb
                }
                item.icon = favImage
            }, {
                item.icon = notFavImage
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.action, menu)

        if (menu != null) {
            val item = menu.findItem(R.id.fav_button)
            if (item != null) {
                getFavoriteIcon(item)
            }
        }

        return true
    }

    private fun SetToFavorite(item: FloatingActionButton) {

        if (!currentRanobe.isFavorite && !currentRanobe.isFavoriteInWeb) {
            item.setImageDrawable(favImage)

            object : Thread() {
                override fun run() {
                    var wasadded = false
                    if (currentRanobe.ranobeSite == Constants.RanobeSite.Rulate.url) {

                        val token = preferences!!.getString(Constants.KEY_Token, "")
                        if (token != "") {
                            try {
                                val response = "" //JsonRulateApi.getInstance()!!.AddBookmark(currentRanobe.id, token)

                                val jsonObject = JSONObject(response)
                                if (jsonObject.getString("status") == "success") {
                                    wasadded = true
                                    currentRanobe.isFavoriteInWeb = true
                                    runOnUiThread {
                                        Toast.makeText(mContext, currentRanobe.title + " " + mContext!!.getString(R.string.added_to_web), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                LogHelper.logError(LogHelper.LogType.WARN, RanobeInfoActivity::class.java.toString(), "", e)

                            }

                        }

                    } else if (currentRanobe.ranobeSite == Constants.RanobeSite.RanobeRf.url) {

                        val token = rfpreferences!!.getString(Constants.KEY_Token, "")
                        if (token != "") {
                            try {
                                val response = "" //JsonRanobeRfApi.getInstance()!!.AddBookmark(   currentRanobe.id,   currentRanobe.chapterList[0].id,                                        token)

                                val jsonObject = JSONObject(response)
                                if (jsonObject.getInt("status") == 200) {
                                    wasadded = true
                                    currentRanobe.isFavoriteInWeb = true
                                    runOnUiThread {
                                        Toast.makeText(mContext, currentRanobe.title + " " + mContext!!.getString(R.string.added_to_web), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                LogHelper.logError(LogHelper.LogType.WARN, RanobeInfoActivity::class.java.toString(), "", e)

                            }

                        }

                    }

                    if (!wasadded) {
                        currentRanobe.isFavorite = true
                        AsyncTask.execute {
                            MyApp.database?.ranobeDao()?.insert(currentRanobe)
                            MyApp.database?.chapterDao()?.insertAll(*currentRanobe.chapterList.toTypedArray())

                        }

                        runOnUiThread {
                            Toast.makeText(mContext, currentRanobe.title + " " + mContext!!.getString(R.string.added_to_local), Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }.start()

        } else {

            if (currentRanobe.isFavoriteInWeb) {

                if (currentRanobe.ranobeSite.contains(Constants.RanobeSite.Rulate.url)) {
                    val token = preferences!!.getString(Constants.KEY_Token, "") ?: ""
                    if (token != "") {
                        try {
                            val response = "" // JsonRulateApi.getInstance()!!.RemoveBookmark(                                    currentRanobe.id, token)

                            val jsonObject = JSONObject(response)
                            if (jsonObject.getString("status") == "success") {
                                currentRanobe.isFavoriteInWeb = false
                                AsyncTask.execute {
                                    MyApp.database?.ranobeDao()?.deleteWeb(currentRanobe.url)
                                }
                                item.setImageDrawable(notFavImage)
                            }
                        } catch (e: JSONException) {
                            LogHelper.logError(LogHelper.LogType.WARN, RanobeInfoActivity::class.java.toString(), "", e)

                        }

                    }

                } else if (currentRanobe.ranobeSite.contains(Constants.RanobeSite.RanobeRf.url)) {
                    val token = rfpreferences!!.getString(Constants.KEY_Token, "") ?: ""
                    if (token != "") {

                        val thread = Thread {
                            try {
                                val response = "" //JsonRanobeRfApi.getInstance()!!.RemoveBookmark(                                        currentRanobe.bookmarkIdRf, token)

                                val jsonObject = JSONObject(response)
                                if (jsonObject.getInt("status") == 200) {
                                    currentRanobe.isFavoriteInWeb = false
                                    AsyncTask.execute {
                                        MyApp.database?.ranobeDao()?.deleteWeb(currentRanobe.url)
                                    }
                                    runOnUiThread {
                                        item.setImageDrawable(notFavImage)
                                    }

                                }
                            } catch (e: JSONException) {

                                runOnUiThread {
                                    Toast.makeText(mContext, mContext!!.getString(R.string.update_to_remove), Toast.LENGTH_SHORT).show()
                                }

                            }

                        }

                        thread.start()

                    }
                }

            } else {
                AsyncTask.execute {
                    MyApp.database?.ranobeDao()?.delete(currentRanobe.url)
                }
                currentRanobe.isFavorite = false
                item.setImageDrawable(notFavImage)
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        request?.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.download_chapters -> {
                val intent = Intent(mContext, DownloadActivity::class.java)
                if (MyApp.ranobe != null) {
                    mContext!!.startActivity(intent)
                }
            }
            R.id.navigation_open_in_browser -> {
                var url = currentRanobe.url

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
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

}
