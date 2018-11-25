package ru.profapp.RanobeReader.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TabHost
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import io.fabric.sdk.android.Fabric
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ranobe_info.*
import ru.profapp.RanobeReader.Adapters.CommentsRecyclerViewAdapter
import ru.profapp.RanobeReader.Adapters.ExpandableChapterRecyclerViewAdapter
import ru.profapp.RanobeReader.BuildConfig
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Common.Constants.is_readed_Pref
import ru.profapp.RanobeReader.Common.Constants.last_chapter_id_Pref
import ru.profapp.RanobeReader.Common.MyExceptionHandler
import ru.profapp.RanobeReader.Helpers.LogHelper
import ru.profapp.RanobeReader.Helpers.ThemeHelper
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.Network.Repositories.RanobeRfRepository
import ru.profapp.RanobeReader.Network.Repositories.RulateRepository
import ru.profapp.RanobeReader.R
import ru.profapp.RanobeReader.Utils.GlideApp
import java.util.*

class RanobeInfoActivity : AppCompatActivity() {

    private var currentTheme = ThemeHelper.sTheme
    private val recycleChapterList = ArrayList<Chapter>()
    private lateinit var preferences: SharedPreferences
    private lateinit var rfpreferences: SharedPreferences
    private lateinit var mCurrentRanobe: Ranobe
    private lateinit var mContext: Context
    private var adapterExpandable: ExpandableChapterRecyclerViewAdapter? = null
    private var sPref: SharedPreferences? = null
    private var lastChapterIdPref: SharedPreferences? = null
    private var mChapterLayoutManager: LinearLayoutManager? = null

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {

        currentTheme = AppCompatDelegate.getDefaultNightMode()
        super.onCreate(savedInstanceState)

        if (!MyApp.isApplicationInitialized || MyApp.ranobe == null) {
            val firstIntent = Intent(this, MainActivity::class.java)

            firstIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // So all other activities will be dumped
            startActivity(firstIntent)

            // We are done, so finish this activity and get out now
            finish()
            return
        }

        // Set up Crashlytics, disabled for debug builds
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        Fabric.with(this, crashlyticsKit)
        setContentView(R.layout.activity_ranobe_info)
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))

        mContext = this@RanobeInfoActivity

        mCurrentRanobe = MyApp.ranobe!!

        getFavoriteIcon()

        fab_rI_favorite.setOnClickListener { setToFavorite() }

        fabBookmark.setOnClickListener {

            val chapterHistory = MyApp.database.ranobeHistoryDao().getLastChapterByName(mCurrentRanobe.title).subscribeOn(Schedulers.io())?.blockingGet()
            val intent = Intent(mContext, ChapterTextActivity::class.java)
            intent.putExtra("ChapterUrl", chapterHistory?.chapterUrl)
            intent.putExtra("Progress", chapterHistory?.progress)
            mContext.startActivity(intent)

        }

        setSupportActionBar(toolbar_rI)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        GlideApp.with(this).load(mCurrentRanobe.image).into(main_logoimage)

        supportActionBar?.title = mCurrentRanobe.title

        preferences = mContext.getSharedPreferences(Constants.Rulate_Login_Pref, 0)

        rfpreferences = mContext.getSharedPreferences(Constants.Ranoberf_Login_Pref, 0)

        mChapterLayoutManager = LinearLayoutManager(mContext)
        rV_rI_chapters.layoutManager = mChapterLayoutManager
        rV_rI_chapters.onFlingListener = object : RecyclerView.OnFlingListener() {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                rV_rI_chapters.dispatchNestedFling(velocityX.toFloat(), velocityY.toFloat(), false)
                return false
            }
        }

        rV_rI_chapters.setHasFixedSize(true)
        pBar_RanobeInfo.visibility = View.VISIBLE
        loadData()
        loadChapters()


        tH_rI_comments.setup()

        val tabSpec: TabHost.TabSpec = tH_rI_comments.newTabSpec("chapters")

        tabSpec.setContent(R.id.cV_history_ranobe)
        tabSpec.setIndicator(resources.getString(R.string.chapters))
        tH_rI_comments.addTab(tabSpec)
        tH_rI_comments.currentTab = 0


//        if (MyApp.hidePaymentChapter) {
//            hideButton.setImageResource(R.drawable.ic_visibility_black_24dp)
//        } else {
//            hideButton.setImageResource(R.drawable.ic_visibility_off_black_24dp)
//        }
//        hideButton.setOnClickListener { v ->
//
//            mCurrentRanobe.hidePaymentChapters = !mCurrentRanobe.hidePaymentChapters
//
//            if (mCurrentRanobe.hidePaymentChapters) {
//                hideButton.setImageResource(R.drawable.ic_visibility_off_black_24dp)
//            } else {
//                hideButton.setImageResource(R.drawable.ic_visibility_black_24dp)
//            }
//        }
        initAds()

    }


    private fun initAds() {
        MobileAds.initialize(applicationContext, getString(R.string.app_admob_id))
        val adRequest = AdRequest.Builder()

        if (BuildConfig.DEBUG) {
            adRequest.addTestDevice("test")
        }

        adView?.loadAd(adRequest.build())
    }

    private fun loadData() {

        if (!mCurrentRanobe.description.isNullOrEmpty()) {
            cV_RI_description.visibility = View.VISIBLE
        } else {
            cV_RI_description.visibility = View.GONE
        }
        if (!mCurrentRanobe.additionalInfo.isNullOrEmpty()) {
            cV_RI_info.visibility = View.VISIBLE
        } else {
            cV_RI_info.visibility = View.GONE
        }


        GlideApp.with(this).load(mCurrentRanobe.image).into(main_logoimage)

        var aboutText = String.format("%s / %s \n\n%s", mCurrentRanobe.title, mCurrentRanobe.engTitle, mCurrentRanobe.description)
        if (!mCurrentRanobe.rating.isNullOrBlank())
            aboutText = aboutText.plus("\n\nРейтинг: ${mCurrentRanobe.rating}")

        if (mCurrentRanobe.genres != null) {
            aboutText = aboutText.plus("\n\n${mCurrentRanobe.genres}")
        }

        tV_rI_about.text = aboutText
        tV_rI_additionalInfo.text = mCurrentRanobe.additionalInfo
    }

    private fun loadChapters() {
        recycleChapterList.clear()
        sPref = mContext.getSharedPreferences(is_readed_Pref, Context.MODE_PRIVATE)
        lastChapterIdPref = mContext.getSharedPreferences(last_chapter_id_Pref, Context.MODE_PRIVATE)

        val request = mCurrentRanobe.updateRanobe(mContext).map {
            var checked = false
            if (lastChapterIdPref != null) {
                val lastId: Int = lastChapterIdPref?.getInt(mCurrentRanobe.url, -1) ?: -1
                if (lastId > 0) {
                    checked = true
                    for (chapter in mCurrentRanobe.chapterList) {
                        if (chapter.id != null)
                            chapter.isRead = chapter.id!! <= lastId
                    }

                }
            }

            if (sPref != null && !checked) {
                for (chapter in mCurrentRanobe.chapterList) {
                    if (!chapter.isRead) {
                        chapter.isRead = sPref!!.getBoolean(chapter.url, false)
                    }
                }
            }
            return@map it
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({

                    pBar_RanobeInfo.visibility = View.GONE
                    loadData()
                    recycleChapterList.addAll(mCurrentRanobe.chapterList)
                    adapterExpandable = ExpandableChapterRecyclerViewAdapter(mContext, recycleChapterList, mCurrentRanobe)
                    rV_rI_chapters.adapter = adapterExpandable

                    if (mCurrentRanobe.comments.isNotEmpty()) {
                        rV_rI_comments.layoutManager = LinearLayoutManager(mContext)
                        rV_rI_comments.setHasFixedSize(true)
                        rV_rI_comments.onFlingListener = object : RecyclerView.OnFlingListener() {
                            @RequiresApi(Build.VERSION_CODES.KITKAT)
                            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                                rV_rI_comments.dispatchNestedFling(velocityX.toFloat(), velocityY.toFloat(), false)
                                return false
                            }
                        }

                        rV_rI_comments.adapter = CommentsRecyclerViewAdapter(mContext, mCurrentRanobe.comments)

                        val tabSpec: TabHost.TabSpec = tH_rI_comments.newTabSpec("comments")
                        tabSpec.setContent(R.id.cV_history_chapters)
                        tabSpec.setIndicator(resources.getString(R.string.comments))
                        tH_rI_comments.addTab(tabSpec)
                    }

                }, { error ->
                    LogHelper.logError(LogHelper.LogType.ERROR, "loadChapters", "", error)
                    pBar_RanobeInfo.visibility = View.GONE
                })

        compositeDisposable.add(request)
    }

    private fun getFavoriteIcon() {
        if (mCurrentRanobe.isFavorite || mCurrentRanobe.isFavoriteInWeb) {

            fab_rI_favorite.setImageResource(R.drawable.ic_favorite_black_24dp)

        } else {
            val request = MyApp.database.ranobeDao().isRanobeFavorite(mCurrentRanobe.url)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        if (it != null) {
                            mCurrentRanobe.isFavorite = it.isFavorite
                            mCurrentRanobe.isFavoriteInWeb = it.isFavoriteInWeb
                            fab_rI_favorite.setImageResource(R.drawable.ic_favorite_black_24dp)
                        } else {
                            mCurrentRanobe.isFavorite = false
                            mCurrentRanobe.isFavoriteInWeb = false
                            fab_rI_favorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                        }

                    }, { error ->
                        LogHelper.logError(LogHelper.LogType.ERROR, "loadChapters", "", error)
                        mCurrentRanobe.isFavorite = false
                        mCurrentRanobe.isFavoriteInWeb = false
                        fab_rI_favorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                    })
            compositeDisposable.add(request)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.action, menu)
        return true
    }

    private fun setToFavorite() {

        if (!mCurrentRanobe.isFavorite && !mCurrentRanobe.isFavoriteInWeb) {

            var fabRequest = Single.just(Pair(false, "not matching site"))
            when (mCurrentRanobe.ranobeSite) {

                Constants.RanobeSite.Rulate.url -> {
                    val token = preferences.getString(Constants.KEY_Token, "") ?: ""
                    if (!token.isBlank()) {
                        fabRequest = RulateRepository.addBookmark(token, mCurrentRanobe.id)
                    }
                }
                Constants.RanobeSite.RanobeRf.url -> {
                    val token = rfpreferences.getString(Constants.KEY_Token, "") ?: ""
                    if (!token.isBlank()) {
                        fabRequest = RanobeRfRepository.addBookmark(token, mCurrentRanobe.id, mCurrentRanobe.chapterList.firstOrNull()?.id)
                    }
                }
            }

            val request = fabRequest
                    .map { result ->
                        if (result.first) {
                            mCurrentRanobe.isFavorite = true
                            mCurrentRanobe.isFavoriteInWeb = true
                            return@map true
                        } else {
                            mCurrentRanobe.isFavorite = true
                            mCurrentRanobe.isFavoriteInWeb = false
                            return@map false
                        }
                    }.map {
                        MyApp.database.ranobeDao().insert(mCurrentRanobe)
                        MyApp.database.chapterDao().insertAll(*mCurrentRanobe.chapterList.toTypedArray())
                        return@map it
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        fab_rI_favorite.setImageResource(R.drawable.ic_favorite_black_24dp)
                        if (result)
                            Toast.makeText(mContext, mCurrentRanobe.title + " " + mContext.getString(R.string.added_to_web), Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(mContext, mCurrentRanobe.title + " " + mContext.getString(R.string.added_to_local), Toast.LENGTH_SHORT).show()
                    }, { error ->
                        LogHelper.logError(LogHelper.LogType.ERROR, "setToFavorite", "WEB", error, false)
                    })
            compositeDisposable.add(request)

        } else {

            if (mCurrentRanobe.isFavoriteInWeb) {
                var fabRequest = Single.just(Pair(false, "Can not remove from web site. Are you log into?"))
                when (mCurrentRanobe.ranobeSite) {

                    Constants.RanobeSite.Rulate.url -> {
                        val token = preferences.getString(Constants.KEY_Token, "") ?: ""
                        if (!token.isBlank()) {
                            fabRequest = RulateRepository.removeBookmark(token, mCurrentRanobe.id)
                        }
                    }
                    Constants.RanobeSite.RanobeRf.url -> {
                        val token = rfpreferences.getString(Constants.KEY_Token, "") ?: ""
                        if (!token.isBlank()) {
                            fabRequest = RanobeRfRepository.removeBookmark(token, mCurrentRanobe.bookmarkIdRf)
                        }
                    }
                }

                val request = fabRequest
                        .map { result ->
                            MyApp.database.ranobeDao().deleteWeb(mCurrentRanobe.url)
                            if (result.first) {
                                mCurrentRanobe.isFavorite = false
                                mCurrentRanobe.isFavoriteInWeb = false
                                return@map result
                            } else {
                                return@map result
                            }
                        }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ result ->
                            if (result.first) {
                                fab_rI_favorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                            } else {
                                Toast.makeText(mContext, result.second, Toast.LENGTH_SHORT).show()
                            }
                        }, { error ->
                            Toast.makeText(mContext, getString(R.string.cant_remove_web_bookmark), Toast.LENGTH_SHORT).show()
                            LogHelper.logError(LogHelper.LogType.ERROR, "", "", error, false)
                        })
                compositeDisposable.add(request)
            } else {
                val request = Completable.fromAction {
                    MyApp.database.ranobeDao().delete(mCurrentRanobe.url)
                }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            mCurrentRanobe.isFavorite = false
                            mCurrentRanobe.isFavoriteInWeb = false
                            fab_rI_favorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                        }, { error ->
                            LogHelper.logError(LogHelper.LogType.ERROR, "", "", error, false)
                        })

                compositeDisposable.add(request)

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        adView?.adListener = null
        adView?.removeAllViews()
        adView?.destroy()
        compositeDisposable.clear()
        Thread.setDefaultUncaughtExceptionHandler(null)

    }

    override fun onResume() {
        super.onResume()
        adView?.resume()
        if (currentTheme != AppCompatDelegate.getDefaultNightMode())
            recreate()
    }

    override fun onPause() {
        super.onPause()
        adView?.pause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.download_chapters -> {
                val intent = Intent(mContext, DownloadActivity::class.java)
                if (MyApp.ranobe != null) {
                    mContext.startActivity(intent)
                }
            }
            R.id.navigation_open_in_browser -> {
                var url = mCurrentRanobe.url

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
