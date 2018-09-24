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
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DividerItemDecoration
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
import ru.profapp.RanobeReader.Common.StringResources
import ru.profapp.RanobeReader.Helpers.MyLog
import ru.profapp.RanobeReader.Helpers.StringHelper
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R
import java.util.*

class RanobeInfoActivity : AppCompatActivity() {

    private val recycleChapterList = ArrayList<Chapter>()
    lateinit var recyclerView: RecyclerView
    var preferences: SharedPreferences? = null
    var rfpreferences: SharedPreferences? = null
    lateinit var mCurrentRanobe: Ranobe
    var mContext: Context? = null
    private var borderImage: Drawable? = null
    private var fillImage: Drawable? = null
    lateinit var tabHost:TabHost
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
    private lateinit var myOptions: RequestOptions
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

        loadAds()

        mContext = this@RanobeInfoActivity
        mCurrentRanobe = MyApp.ranobe!!

        borderImage = mContext!!.resources.getDrawable(R.drawable.ic_favorite_border_black_24dp)
        fillImage = mContext!!.resources.getDrawable(R.drawable.ic_favorite_black_24dp)

        val nestedScrollView = findViewById<NestedScrollView>(R.id.ranobe_info_NestedScrollView)

        val fab = findViewById<FloatingActionButton>(R.id.fav_button)
        fab.setOnClickListener { SetToFavorite(fab) }

        val bookmarkFab = findViewById<FloatingActionButton>(R.id.bookmark_fab)
        bookmarkFab.setOnClickListener { view ->

            val sChapterPref = mContext!!.getSharedPreferences(
                    StringHelper.CleanString(mCurrentRanobe.url), Context.MODE_PRIVATE)

            val url = sChapterPref.getString(StringResources.Chapter_Url, null)
            val intent = Intent(mContext, ChapterTextActivity::class.java)
            var tempIndex = mCurrentRanobe.chapterList.size - 1
            if (url != null) {
                val tempList = mCurrentRanobe.chapterList
                for (i in tempList.indices) {
                    val chapter = tempList[i]
                    if (chapter.url == url) {
                        tempIndex = i
                        break
                    }

                }
            }
            intent.putExtra("ChapterIndex", tempIndex)
            intent.putExtra("Bookmark", true)

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
        myOptions = RequestOptions()
                .placeholder(R.drawable.ic_adb_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .fitCenter()
        Glide.with(mContext!!).load(mCurrentRanobe.image)
                .apply(myOptions)
                .into(imageView)

        supportActionBar?.title = mCurrentRanobe.title

        preferences = mContext!!.getSharedPreferences(StringResources.Rulate_Login_Pref, 0)

        rfpreferences = mContext!!.getSharedPreferences(StringResources.Ranoberf_Login_Pref, 0)



        recyclerView = findViewById(R.id.chapter_list)

        mChapterLayoutManager = LinearLayoutManager(mContext)
        recyclerView.layoutManager = mChapterLayoutManager
        recyclerView.onFlingListener = object : RecyclerView.OnFlingListener() {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                recyclerView.dispatchNestedFling(velocityX.toFloat(), velocityY.toFloat(), false)
                return false
            }
        }

        recyclerView.setHasFixedSize(true)
        val itemDecorator = DividerItemDecoration(mContext!!, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(mContext!!.resources.getDrawable(R.drawable.divider))
        recyclerView.addItemDecoration(itemDecorator)

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        loadChapters()

        tabHost = findViewById<TabHost>(R.id.tabHost)

        tabHost.setup()

        val tabSpec: TabHost.TabSpec = tabHost.newTabSpec("chapters")

        tabSpec.setContent(R.id.linearLayout)
        tabSpec.setIndicator(resources.getString(R.string.chapters))
        tabHost.addTab(tabSpec)
        tabHost.currentTab = 0
    }

    private fun loadAds() {
        MobileAds.initialize(this, getString(R.string.app_admob_id))
        val adView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder()

        if (BuildConfig.DEBUG) {
            adRequest.addTestDevice("sdfsdf")
        }

        adView.loadAd(adRequest.build())
    }

    private fun loadData() {
        if (!mCurrentRanobe.description.isNullOrEmpty()) {
            descriptionCard.visibility = View.VISIBLE
        } else {
            descriptionCard.visibility = View.GONE
        }
        if (!mCurrentRanobe.additionalInfo.isNullOrEmpty()) {
            infoCard.visibility = View.VISIBLE
        } else {
            infoCard.visibility = View.GONE
        }


        Glide.with(mContext!!).load(mCurrentRanobe.image)
                .apply(myOptions)
                .into(imageView)

        var aboutText = String.format("%s / %s \n\nРейтинг: %s\n%s", mCurrentRanobe.title,
                mCurrentRanobe.engTitle, mCurrentRanobe.rating, mCurrentRanobe.description)

        if (mCurrentRanobe.genres != null) {
            aboutText = aboutText + "\n\n" + mCurrentRanobe.genres
        }

        aboutTextView.text = aboutText
        additionalInfoTextView.text = mCurrentRanobe.additionalInfo
    }

    private fun loadChapters() {
        recycleChapterList.clear()
        sPref = mContext!!.getSharedPreferences(
                StringResources.is_readed_Pref,
                Context.MODE_PRIVATE)
        lastIndexPref = mContext!!.getSharedPreferences(StringResources.is_readed_Pref, Context.MODE_PRIVATE)

        request = mCurrentRanobe.updateRanobe(mContext!!)
                .map {
                    var checked = false
                    if (lastIndexPref != null) {
                        val lastId = lastIndexPref?.getInt(it.url, -1)
                        if (lastId!! > 0) {
                            checked = true
                            for (chapter in it.chapterList) chapter.isRead = chapter.id < lastId

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
                    mCurrentRanobe = result
                    progressBar.visibility = View.GONE
                    loadData()
                    result.wasUpdated = true
                    recycleChapterList.addAll(result?.chapterList!!)
                    adapterExpandable = ExpandableChapterRecyclerViewAdapter(recycleChapterList, mCurrentRanobe)
                    recyclerView.adapter = adapterExpandable

                    if (mCurrentRanobe.comments.isNotEmpty()) {
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

                        val commentsAdapter = CommentsRecyclerViewAdapter(
                                mCurrentRanobe.comments)
                        commentRecycleView.adapter = commentsAdapter

                        val tabSpec: TabHost.TabSpec  = tabHost.newTabSpec("comments")
                        tabSpec.setContent(R.id.linearLayout2)
                        tabSpec.setIndicator(resources.getString(R.string.comments))
                        tabHost.addTab(tabSpec)
                    }

                }, { error ->
                    MyLog.SendError(MyLog.LogType.ERROR, "loadChapters", "", error.fillInStackTrace())

                    mCurrentRanobe.wasUpdated = false
                    progressBar.visibility = View.GONE
                })


    }

    private fun getFavoriteIcon(item: MenuItem) {
        AsyncTask.execute {
            if (mCurrentRanobe.isFavorite || mCurrentRanobe.isFavoriteInWeb) {
                runOnUiThread { item.icon = fillImage }
            } else {
                runOnUiThread { item.icon = borderImage }
            }//  || DatabaseDao.Companion.getInstance(                    context).ranobeDao().IsRanobeFavorite(                    mCurrentRanobe.getUrl())                    != null
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

        if (!mCurrentRanobe.isFavorite && !mCurrentRanobe.isFavoriteInWeb) {
            item.setImageDrawable(fillImage)

            object : Thread() {
                override fun run() {
                    var wasadded = false
                    if (mCurrentRanobe.ranobeSite == Constants.RanobeSite.Rulate.url) {

                        val token = preferences!!.getString(
                                StringResources.KEY_Token, "")
                        if (token != "") {
                            try {
                                val response = JsonRulateApi.getInstance()!!.AddBookmark(
                                        mCurrentRanobe.id, token)

                                val jsonObject = JSONObject(response)
                                if (jsonObject.getString("status") == "success") {
                                    wasadded = true
                                    mCurrentRanobe.isFavoriteInWeb = true
                                    runOnUiThread {
                                        Toast.makeText(mContext,
                                                mCurrentRanobe.title + " "
                                                        + mContext!!.getString(
                                                        R.string.added_to_web),
                                                Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                MyLog.SendError(MyLog.LogType.WARN,
                                        RanobeInfoActivity::class.java.toString(), "", e)

                            }

                        }

                    } else if (mCurrentRanobe.ranobeSite == Constants.RanobeSite.RanobeRf.url) {

                        val token = rfpreferences!!.getString(
                                StringResources.KEY_Token, "")
                        if (token != "") {
                            try {
                                val response = JsonRanobeRfApi.getInstance()!!.AddBookmark(
                                        mCurrentRanobe.id,
                                        mCurrentRanobe.chapterList[0].id,
                                        token)

                                val jsonObject = JSONObject(response)
                                if (jsonObject.getInt("status") == 200) {
                                    wasadded = true
                                    mCurrentRanobe.isFavoriteInWeb = true
                                    runOnUiThread {
                                        Toast.makeText(mContext,
                                                mCurrentRanobe.title + " "
                                                        + mContext!!.getString(
                                                        R.string.added_to_web),
                                                Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                MyLog.SendError(MyLog.LogType.WARN,
                                        RanobeInfoActivity::class.java.toString(), "", e)

                            }

                        }

                    }

                    if (!wasadded) {
                        mCurrentRanobe.isFavorite = true
                        AsyncTask.execute {
                            MyApp.database?.ranobeDao()?.insert(
                                    mCurrentRanobe)
                            MyApp.database?.chapterDao()?.insertAll(
                                    *mCurrentRanobe.chapterList.toTypedArray())

                        }

                        runOnUiThread {
                            Toast.makeText(mContext,
                                    mCurrentRanobe.title + " " + mContext!!.getString(
                                            R.string.added_to_local), Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }.start()

        } else {

            if (mCurrentRanobe.isFavoriteInWeb) {

                if (mCurrentRanobe.ranobeSite.contains(
                                Constants.RanobeSite.Rulate.url)) {
                    val token = preferences!!.getString(StringResources.KEY_Token, "") ?: ""
                    if (token != "") {
                        try {
                            val response = JsonRulateApi.getInstance()!!.RemoveBookmark(
                                    mCurrentRanobe.id, token)

                            val jsonObject = JSONObject(response)
                            if (jsonObject.getString("status") == "success") {
                                mCurrentRanobe.isFavoriteInWeb = false
                                AsyncTask.execute {
                                    MyApp.database?.ranobeDao()?.deleteWeb(
                                            mCurrentRanobe.url)
                                }
                                item.setImageDrawable(borderImage)
                            }
                        } catch (e: JSONException) {
                            MyLog.SendError(MyLog.LogType.WARN,
                                    RanobeInfoActivity::class.java.toString(), "", e)

                        }

                    }

                } else if (mCurrentRanobe.ranobeSite.contains(
                                Constants.RanobeSite.RanobeRf.url)) {
                    val token = rfpreferences!!.getString(StringResources.KEY_Token, "") ?: ""
                    if (token != "") {

                        val thread = Thread {
                            try {
                                val response = JsonRanobeRfApi.getInstance()!!.RemoveBookmark(
                                        mCurrentRanobe.bookmarkIdRf, token)

                                val jsonObject = JSONObject(response)
                                if (jsonObject.getInt("status") == 200) {
                                    mCurrentRanobe.isFavoriteInWeb = false
                                    AsyncTask.execute {
                                        MyApp.database?.ranobeDao()?.deleteWeb(
                                                mCurrentRanobe.url)
                                    }
                                    runOnUiThread {
                                        item.setImageDrawable(borderImage)
                                    }

                                }
                            } catch (e: JSONException) {

                                runOnUiThread {
                                    Toast.makeText(mContext,
                                            mContext!!.getString(
                                                    R.string.update_to_remove),
                                            Toast.LENGTH_SHORT).show()
                                }

                            }

                        }

                        thread.start()

                    }
                }

            } else {
                AsyncTask.execute {
                    MyApp.database?.ranobeDao()?.delete(
                            mCurrentRanobe.url)
                }
                mCurrentRanobe.isFavorite = false
                item.setImageDrawable(borderImage)
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
                var url = mCurrentRanobe.url

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }

                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW,                            Uri.parse(url))
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

}
