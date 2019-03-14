package ru.profapp.ranobe.activities

import android.content.Context
import android.content.Intent
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
import com.google.android.gms.ads.AdRequest
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ranobe_info.*
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.adapters.CommentsRecyclerViewAdapter
import ru.profapp.ranobe.adapters.ExpandableChapterRecyclerViewAdapter
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.common.MyExceptionHandler
import ru.profapp.ranobe.helpers.*
import ru.profapp.ranobe.models.Chapter
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.network.repositories.RanobeRfRepository
import ru.profapp.ranobe.network.repositories.RulateRepository
import ru.profapp.ranobe.utils.GlideApp
import ru.profapp.ranobe.utils.GlideRequests
import java.util.*
import javax.inject.Inject

class RanobeInfoActivity : AppCompatActivity() {

    private var currentTheme = ThemeHelper.sTheme
    private val recycleChapterList = ArrayList<Chapter>()
    private lateinit var mCurrentRanobe: Ranobe
    private lateinit var mContext: Context
    private var adapterExpandable: ExpandableChapterRecyclerViewAdapter? = null
    private var mChapterLayoutManager: LinearLayoutManager? = null

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private lateinit var mGlide: GlideRequests

    @Inject
    lateinit var adRequest: AdRequest

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        currentTheme = AppCompatDelegate.getDefaultNightMode()

        if (!MyApp.isApplicationInitialized || MyApp.ranobe == null) {

            launchActivity<MainActivity> {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // We are done, so finish this activity and get out now
            finish()
            return
        }

        MyApp.component.inject(this)

        setContentView(R.layout.activity_ranobe_info)
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))

        mContext = this@RanobeInfoActivity


        mGlide = GlideApp.with(this@RanobeInfoActivity)

        mCurrentRanobe = MyApp.ranobe!!

        getFavoriteIcon()

        rInfoFabFavorite.setOnClickListener { setToFavorite() }

        rInfoFabBookmark.setOnClickListener {

            val chapterProgress =
                MyApp.database.chapterProgressDao().getLastChapterByRanobeUrl(mCurrentRanobe.url)
                    .subscribeOn(Schedulers.io())?.blockingGet()

            launchActivity<ChapterTextActivity> {
                putExtra("ChapterUrl", chapterProgress?.chapterUrl)
                putExtra("Progress", chapterProgress?.progress)
            }

        }

        setSupportActionBar(rInfoToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mGlide.load(mCurrentRanobe.image).into(rInfoAppBarImage)

        supportActionBar?.title = mCurrentRanobe.title

        mChapterLayoutManager = LinearLayoutManager(mContext)
        rInfoTabCardChaptersRecycler.layoutManager = mChapterLayoutManager
        rInfoTabCardChaptersRecycler.onFlingListener = object : RecyclerView.OnFlingListener() {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                rInfoTabCardChaptersRecycler.dispatchNestedFling(
                    velocityX.toFloat(),
                    velocityY.toFloat(),
                    false
                )
                return false
            }
        }

        rInfoTabCardChaptersRecycler.setHasFixedSize(true)
        rInfoTabCardChaptersProgressBar.visibility = View.VISIBLE
        loadData()
        loadChapters()

        rInfoTabHost.setup()

        val tabSpec: TabHost.TabSpec = rInfoTabHost.newTabSpec("chapters")

        tabSpec.setContent(R.id.rInfoTabCardChapters)
        tabSpec.setIndicator(resources.getString(R.string.chapters))
        rInfoTabHost.addTab(tabSpec)
        rInfoTabHost.currentTab = 0

        AdViewManager(lifecycle,rInfoAdView)
        rInfoAdView.loadAd(adRequest)
    }


    private fun loadData() {

        if (!mCurrentRanobe.description.isNullOrEmpty()) {
            rInfoCardViewDescription.visibility = View.VISIBLE
        } else {
            rInfoCardViewDescription.visibility = View.GONE
        }
        if (!mCurrentRanobe.additionalInfo.isNullOrEmpty()) {
            rInfoCardViewAdditional.visibility = View.VISIBLE
        } else {
            rInfoCardViewAdditional.visibility = View.GONE
        }


        mGlide.load(mCurrentRanobe.image).into(rInfoAppBarImage)

        var aboutText = String.format(
            "%s / %s \n\n%s",
            mCurrentRanobe.title,
            mCurrentRanobe.engTitle,
            mCurrentRanobe.description
        )
        if (!mCurrentRanobe.rating.isNullOrBlank())
            aboutText = aboutText.plus("\n\nРейтинг: ${mCurrentRanobe.rating}")

        if (mCurrentRanobe.genres != null) {
            aboutText = aboutText.plus("\n\n${mCurrentRanobe.genres}")
        }

        rInfoCardDescriptionAbout.text = aboutText
        rInfoCardViewAdditionalInfo.text = mCurrentRanobe.additionalInfo
    }

    private fun loadChapters() {
        recycleChapterList.clear()


        val request = mCurrentRanobe.updateRanobe(mContext).map {

            if (mCurrentRanobe.chapterList.any()) {

                val lastChapterUrl =
                    MyApp.preferencesManager.getLastChapterUrl(mCurrentRanobe.url)

                if (lastChapterUrl.isNotEmpty()) {
                    val chapterIndex =
                        mCurrentRanobe.chapterList.firstOrNull { c -> c.url == lastChapterUrl }
                            ?.index
                    if (chapterIndex != null) {
                        for (chapter in mCurrentRanobe.chapterList) {
                            chapter.isRead = chapter.index <= chapterIndex
                        }
                    }
                } else {
                    val lastId: Int? = MyApp.preferencesManager.getLastChapterId(mCurrentRanobe.url)
                    if (lastId !=null) {

                        for (chapter in mCurrentRanobe.chapterList) {
                            if (chapter.id != null)
                                chapter.isRead = chapter.id!! <= lastId
                        }

                    }
                }

            }
            return@map it
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                {

                    rInfoTabCardChaptersProgressBar.visibility = View.GONE
                    loadData()
                    recycleChapterList.addAll(mCurrentRanobe.chapterList)
                    adapterExpandable = ExpandableChapterRecyclerViewAdapter(
                        mContext,
                        recycleChapterList,
                        mCurrentRanobe
                    )
                    rInfoTabCardChaptersRecycler.adapter = adapterExpandable

                    if (mCurrentRanobe.comments.isNotEmpty()) {
                        rInfoTabCardCommentsRecycler.layoutManager = LinearLayoutManager(mContext)
                        rInfoTabCardCommentsRecycler.setHasFixedSize(true)
                        rInfoTabCardCommentsRecycler.onFlingListener =
                            object : RecyclerView.OnFlingListener() {
                                @RequiresApi(Build.VERSION_CODES.KITKAT)
                                override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                                    rInfoTabCardCommentsRecycler.dispatchNestedFling(
                                        velocityX.toFloat(),
                                        velocityY.toFloat(),
                                        false
                                    )
                                    return false
                                }
                            }

                        rInfoTabCardCommentsRecycler.adapter =
                            CommentsRecyclerViewAdapter(mGlide, mCurrentRanobe.comments)

                        val tabSpec: TabHost.TabSpec = rInfoTabHost.newTabSpec("comments")
                        tabSpec.setContent(R.id.rInfoTabCardComments)
                        tabSpec.setIndicator(resources.getString(R.string.comments))
                        rInfoTabHost.addTab(tabSpec)
                    }

                },
                { error ->
                    logError(LogType.ERROR, "loadChapters", mCurrentRanobe.url, error)
                    rInfoTabCardChaptersProgressBar.visibility = View.GONE
                })

        compositeDisposable.add(request)
    }

    private fun getFavoriteIcon() {
        if (mCurrentRanobe.isFavorite || mCurrentRanobe.isFavoriteInWeb) {

            rInfoFabFavorite.setImageResource(R.drawable.ic_favorite_black_24dp)

        } else {
            val request = MyApp.database.ranobeDao().isRanobeFavorite(mCurrentRanobe.url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it != null) {
                        mCurrentRanobe.isFavorite = it.isFavorite
                        mCurrentRanobe.isFavoriteInWeb = it.isFavoriteInWeb
                        rInfoFabFavorite.setImageResource(R.drawable.ic_favorite_black_24dp)
                    } else {
                        mCurrentRanobe.isFavorite = false
                        mCurrentRanobe.isFavoriteInWeb = false
                        rInfoFabFavorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                    }

                }, { error ->
                    logError(LogType.ERROR, "loadChapters", "", error)
                    mCurrentRanobe.isFavorite = false
                    mCurrentRanobe.isFavoriteInWeb = false
                    rInfoFabFavorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
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
                    val token = MyApp.preferencesManager.rulateToken
                    if (!token.isBlank()) {
                        fabRequest = RulateRepository.addBookmark(token, mCurrentRanobe.id)
                    }
                }
                Constants.RanobeSite.RanobeRf.url -> {
                    val token = MyApp.preferencesManager.ranoberfToken
                    if (!token.isBlank()) {
                        fabRequest = RanobeRfRepository.addBookmark(
                            token,
                            mCurrentRanobe.id,
                            mCurrentRanobe.chapterList.firstOrNull()?.id
                        )
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
                    MyApp.database.chapterDao()
                        .insertAll(*mCurrentRanobe.chapterList.toTypedArray())
                    return@map it
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    rInfoFabFavorite.setImageResource(R.drawable.ic_favorite_black_24dp)
                    if (result)
                        Toast.makeText(
                            mContext,
                            mCurrentRanobe.title + " " + mContext.getString(R.string.added_to_web),
                            Toast.LENGTH_SHORT
                        ).show()
                    else
                        Toast.makeText(
                            mContext,
                            mCurrentRanobe.title + " " + mContext.getString(R.string.added_to_local),
                            Toast.LENGTH_SHORT
                        ).show()
                }, { error ->
                    logError(LogType.ERROR, "setToFavorite", "WEB", error, false)
                })
            compositeDisposable.add(request)

        } else {

            if (mCurrentRanobe.isFavoriteInWeb) {
                var fabRequest =
                    Single.just(Pair(false, "Can not remove from web site. Are you log into?"))
                when (mCurrentRanobe.ranobeSite) {

                    Constants.RanobeSite.Rulate.url -> {
                        val token = MyApp.preferencesManager.rulateToken
                        if (!token.isBlank()) {
                            fabRequest = RulateRepository.removeBookmark(token, mCurrentRanobe.id)
                        }
                    }
                    Constants.RanobeSite.RanobeRf.url -> {
                        val token = MyApp.preferencesManager.ranoberfToken
                        if (!token.isBlank()) {
                            fabRequest = RanobeRfRepository.removeBookmark(
                                token,
                                mCurrentRanobe.bookmarkIdRf
                            )
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
                            rInfoFabFavorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                        } else {
                            Toast.makeText(mContext, result.second, Toast.LENGTH_SHORT).show()
                        }
                    }, { error ->
                        Toast.makeText(
                            mContext,
                            getString(R.string.cant_remove_web_bookmark),
                            Toast.LENGTH_SHORT
                        ).show()
                        logError(LogType.ERROR, "", "", error, false)
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
                        rInfoFabFavorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                    }, { error ->
                        logError(LogType.ERROR, "", "", error, false)
                    })

                compositeDisposable.add(request)

            }
        }

    }

    override fun onDestroy() {

        compositeDisposable.clear()
        Thread.setDefaultUncaughtExceptionHandler(null)
        super.onDestroy()

    }

    override fun onResume() {
        super.onResume()
        if (currentTheme != AppCompatDelegate.getDefaultNightMode())
            recreate()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.download_chapters -> {

                if (MyApp.ranobe != null) {
                    launchActivity<DownloadActivity> ()
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
