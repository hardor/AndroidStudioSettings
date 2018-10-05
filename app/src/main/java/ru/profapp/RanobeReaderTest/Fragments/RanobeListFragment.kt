package ru.profapp.RanobeReaderTest.Fragments

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReaderTest.Adapters.RanobeRecyclerViewAdapter
import ru.profapp.RanobeReaderTest.Common.Constants
import ru.profapp.RanobeReaderTest.Common.Constants.RanobeSite.None
import ru.profapp.RanobeReaderTest.Common.Constants.RanobeSite.Title
import ru.profapp.RanobeReaderTest.Common.Constants.chaptersNum
import ru.profapp.RanobeReaderTest.Common.Constants.fragmentBundle
import ru.profapp.RanobeReaderTest.Common.OnLoadMoreListener
import ru.profapp.RanobeReaderTest.Helpers.LogHelper
import ru.profapp.RanobeReaderTest.JsonApi.RanobeHubRepository
import ru.profapp.RanobeReaderTest.JsonApi.RanobeRfRepository
import ru.profapp.RanobeReaderTest.JsonApi.RulateRepository
import ru.profapp.RanobeReaderTest.Models.Chapter
import ru.profapp.RanobeReaderTest.Models.Ranobe
import ru.profapp.RanobeReaderTest.MyApp
import ru.profapp.RanobeReaderTest.R
import java.net.UnknownHostException

class RanobeListFragment : Fragment() {

    private val ranobeList = mutableListOf<Ranobe>()
    private var progressDialog: ProgressDialog? = null
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private var mListener: OnListFragmentInteractionListener? = null
    private lateinit var mRanobeRecyclerViewAdapter: RanobeRecyclerViewAdapter
    private var mContext: Context? = null
    private var fragmentType: Constants.FragmentType? = null
    private var page: Int = 0
    private var loadFromDatabase: Boolean = false
    private var oldListSize: Int = 0
    private var request: Disposable? = null

    private fun getRulateWebFavorite(): Single<List<Ranobe>> {
        val mPreferences = mContext!!.getSharedPreferences(Constants.Rulate_Login_Pref, 0)
        val token = mPreferences.getString(Constants.KEY_Token, "") ?: ""
        return RulateRepository.getFavoriteBooks(token)
    }

    private fun getRanobeRfWebFavorite(): Single<List<Ranobe>> {

        val mPreferences = mContext!!.getSharedPreferences(Constants.Ranoberf_Login_Pref, 0)

        val token = mPreferences.getString(Constants.KEY_Token, "")
        return RanobeRfRepository.getFavoriteBooks(token)
    }

    private fun getRanobeHubWebFavorite(): Single<List<Ranobe>> {
        return Single.just(listOf())
    }

    init {
        page = 0
        loadFromDatabase = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (arguments != null && arguments!!.containsKey(fragmentBundle)) {
            fragmentType = Constants.FragmentType.valueOf(arguments!!.getString(fragmentBundle))
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_ranobe_list, container, false)

        mContext = view.context
        val recyclerView = view.findViewById<RecyclerView>(R.id.ranobeListView)

        recyclerView.layoutManager = LinearLayoutManager(mContext)

        mRanobeRecyclerViewAdapter = RanobeRecyclerViewAdapter(mContext!!, recyclerView, ranobeList)

        recyclerView.adapter = mRanobeRecyclerViewAdapter

        //set load more listener for the RecyclerView adapterExpandable
        if (fragmentType != Constants.FragmentType.Favorite
                && fragmentType != Constants.FragmentType.Search
                && fragmentType != Constants.FragmentType.Saved) {

            mRanobeRecyclerViewAdapter.onLoadMoreListener = object : OnLoadMoreListener {
                override fun onLoadMore() {
                    refreshItems(false)
                }
            }
        }
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener {
            mSwipeRefreshLayout.isRefreshing = false
            page = 0
            refreshItems(true)
        }
        mSwipeRefreshLayout.isRefreshing = true

        refreshItems(true)

        return view
    }

    private fun refreshItems(remove: Boolean) {

        mSwipeRefreshLayout.isRefreshing = true
        oldListSize = ranobeList.size
        if (remove) {
            page = 0
            oldListSize = ranobeList.size
            ranobeList.clear()
            mRanobeRecyclerViewAdapter.notifyItemRangeRemoved(0, oldListSize)
            oldListSize = 0
        }
        //Todo: remove sPref. Move to lastIndexPref
        val sPref = mContext!!.getSharedPreferences(Constants.is_readed_Pref, MODE_PRIVATE)
        val lastIndexPref = mContext!!.getSharedPreferences(Constants.last_chapter_id_Pref, MODE_PRIVATE)

        val loader: Single<List<Ranobe>> = when (fragmentType) {
            Constants.FragmentType.Rulate -> rulateLoadRanobe()
            Constants.FragmentType.Ranoberf -> ranobeRfLoadRanobe()
            Constants.FragmentType.RanobeHub -> ranobeHubLoadRanobe()
            Constants.FragmentType.Favorite -> {
                favoriteLoadRanobe()
            }
            Constants.FragmentType.Saved -> savedLoadRanobe()
            else -> favoriteLoadRanobe()
        }




        request = loader
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map { ranobeList ->
                    for (ranobe in ranobeList) {
                        if (ranobe.image.isNullOrBlank()) {
                            MyApp.database?.ranobeImageDao()?.getImageByUrl(ranobe.url)?.observeOn(AndroidSchedulers.mainThread())?.subscribeOn(Schedulers.io())?.subscribe { it ->
                                ranobe.image = it.image
                            }
                        }
                    }
                    return@map ranobeList

                }.map { it ->
                    var checked = false
                    if (lastIndexPref != null) {
                        for (ranobe in it) {
                            if (lastIndexPref.contains(ranobe.url)) {
                                val lastId = lastIndexPref.getInt(ranobe.url, -1)
                                if (lastId > 0) {
                                    checked = true
                                    val sizeList = ranobe.chapterList.size
                                    for (chapter in ranobe.chapterList.subList(0, Math.min(chaptersNum, sizeList))) {
                                        if (chapter.id!! <= lastId) {
                                            chapter.isRead = true
                                        }
                                    }
                                }

                            }

                        }
                    }
                    if (sPref != null && !checked) {
                        for (ranobe in it) {
                            val sizeList = ranobe.chapterList.size
                            for (chapter in ranobe.chapterList.subList(0, Math.min(chaptersNum, sizeList))) {
                                if (!chapter.isRead) {
                                    chapter.isRead = sPref.getBoolean(chapter.url, false)
                                }
                            }
                        }
                    }
                    return@map it
                }.subscribe({ result ->
                    ranobeList.addAll(result)
                    mRanobeRecyclerViewAdapter.notifyItemRangeInserted(oldListSize, ranobeList.size)

                    page++
                    onItemsLoadFinished()

                }, { error ->
                    LogHelper.logError(LogHelper.LogType.ERROR, "refreshItems", "", error.fillInStackTrace())
                    onItemsLoadFinished(error)
                })

    }

    private fun onItemsLoadFinished(error: Throwable? = null) {

        if (error != null) {
            if (error is CompositeException) {
                var errorConnection = false
                for (err in error.exceptions) {
                    if (err is UnknownHostException) {
                        Toast.makeText(mContext, R.string.ErrorConnection, Toast.LENGTH_SHORT).show()
                        errorConnection = true
                        break
                    }
                }
                if (!errorConnection) Toast.makeText(mContext, R.string.Error, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, R.string.Error, Toast.LENGTH_SHORT).show()
            }

        }

        mRanobeRecyclerViewAdapter.setLoaded()
        mSwipeRefreshLayout.isRefreshing = false
        progressDialog?.dismiss()
    }

    private fun favoriteLoadRanobe(): Single<List<Ranobe>> {

        progressDialog = ProgressDialog(context)
        progressDialog!!.setMessage(resources.getString(R.string.load_please_wait))

        progressDialog!!.setCancelable(false)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        mSwipeRefreshLayout.isRefreshing = false
        progressDialog!!.setButton(Dialog.BUTTON_NEGATIVE, "Cancel") { dialog, which ->

            request?.dispose()
            dialog.dismiss()
        }


        if (loadFromDatabase) {

            progressDialog!!.setTitle(resources.getString(R.string.load_ranobes_from_db))
            progressDialog!!.show()
            return (MyApp.database?.ranobeDao()?.getFavoriteRanobes()
                    ?.map { it ->
                        val newRanobeList = mutableListOf<Ranobe>()

                        val groupList = it.groupBy { g -> g.ranobe.ranobeSite }
                        for (siteGroup in groupList) {
                            val webGroupList = siteGroup.value.groupBy { g -> g.ranobe.isFavoriteInWeb }

                            for (webGroup in webGroupList) {
                                val titleRanobe = Ranobe(Title)
                                titleRanobe.title = (Constants.RanobeSite.fromUrl(siteGroup.key)?.title
                                        ?: None.title).plus(if (webGroup.key) "Web" else "Local")
                                newRanobeList.add(titleRanobe)
                                newRanobeList.addAll(siteGroup.value.map { gr ->
                                    gr.ranobe.chapterList = gr.chapterList
                                    return@map gr.ranobe
                                })
                            }

                        }

                        return@map newRanobeList.toList()
                    } ?: Single.just(listOf())).doOnSuccess {
                loadFromDatabase = false
            }

        } else {
            progressDialog!!.setTitle(resources.getString(R.string.Load_from_Rulate))
            progressDialog!!.show()
            return Single.fromObservable(
                    getRulateWebFavorite().doFinally { progressDialog!!.setTitle(context!!.getString(R.string.Load_from_RanobeRf)) }
                            .concatWith(getRanobeRfWebFavorite().doFinally { progressDialog!!.setTitle(context!!.getString(R.string.Load_from_RanobeRf)) })
                            .concatWith(getRanobeHubWebFavorite().doFinally { progressDialog!!.setTitle(context!!.getString(R.string.Load_from_RanobeRf)) })

                            .map { ranobeList ->
                                for (ranobe in ranobeList) {
                                    ranobe.updateRanobe(mContext!!)
                                    ranobe.isFavoriteInWeb = true
                                    ranobe.isFavorite = true
                                    MyApp.database?.ranobeDao()?.insert(ranobe)
                                    MyApp.database?.chapterDao()?.insertAll(*ranobe.chapterList.toTypedArray())
                                }
                                return@map ranobeList
                            }.toObservable()

            )

        }
        //
        //        var favRanobeList = MyApp.database?.ranobeDao()?.getFavoriteBySite(Rulate.url)                ?: ArrayList()
        //        if (favRanobeList.isNotEmpty()) {
        //            val TitleRanobe = Ranobe()
        //            TitleRanobe.title = getString(R.string.tl_rulate_name)
        //            TitleRanobe.ranobeSite = Title.url
        //            newRanobeList.add(TitleRanobe)
        //            newRanobeList.addAll(favRanobeList)
        //            step--
        //        }
        //
        //        favRanobeList = MyApp.database?.ranobeDao()?.getFavoriteBySite(RanobeRf.url) ?: ArrayList()
        //        if (favRanobeList.isNotEmpty()) {
        //            val TitleRanobe = Ranobe()
        //            TitleRanobe.title = getString(R.string.ranobe_rf)
        //            TitleRanobe.ranobeSite = Title.url
        //            newRanobeList.add(TitleRanobe)
        //            newRanobeList.addAll(favRanobeList)
        //            step--
        //        }

        //        activity?.runOnUiThread {
        //            progressDialog!!.setTitle(                    context!!.getString(R.string.Load_from_Rulate))
        //        }
        //
        //        val rulateWebList = getRulateWebFavorite
        //        if (rulateWebList.isNotEmpty()) {
        //            val TitleRanobe = Ranobe()
        //            TitleRanobe.title = context!!.getString(R.string.tl_rulate_web)
        //            TitleRanobe.ranobeSite = Title.url
        //            newRanobeList.add(TitleRanobe)
        //            step--
        //        }
        //        newRanobeList.addAll(rulateWebList)
        //
        //        activity?.runOnUiThread {
        //            progressDialog!!.setTitle(                    context!!.getString(R.string.Load_from_RanobeRf))
        //
        //        }
        //        val ranoberfWebList = getRanobeRfWebFavorite
        //        if (ranoberfWebList.isNotEmpty()) {
        //            val TitleRanobe = Ranobe()
        //            TitleRanobe.title = context!!.getString(R.string.ranoberf_web)
        //            TitleRanobe.ranobeSite = Title.url
        //            newRanobeList.add(TitleRanobe)
        //            step--
        //        }
        //        newRanobeList.addAll(ranoberfWebList)

        //        activity?.runOnUiThread {
        //            progressDialog!!.setTitle(                    context!!.getString(R.string.Update_ranobe_info))
        //        }
        //
        //        progressDialog!!.max = newRanobeList.size + step
        //
        //        for (ranobe in newRanobeList) {
        //
        //            var error = false
        //
        //
        //            activity?.runOnUiThread { progressDialog!!.setMessage(ranobe.title) }
        //
        //
        //            if (!loadFromDatabase) {
        //                try {
        //                    ranobe.updateRanobe(context!!)
        //                    AsyncTask.execute {
        //
        //                        if (!ranobe.isFavoriteInWeb && ranobe.ranobeSite != Title.url) {
        //                            MyApp.database?.ranobeDao()?.update(ranobe)
        //                            MyApp.database?.chapterDao()?.insertAll(*ranobe.chapterList.toTypedArray())
        //
        //                        }
        //
        //                    }
        //                } catch (e: NullPointerException) {
        //
        //                    error = true
        //                    activity?.runOnUiThread {
        //                        Toast.makeText(context, getString(R.string.Error),
        //                                Toast.LENGTH_SHORT).show()
        //                    }
        //                } catch (e: ErrorConnectionException) {
        //
        //                    error = true
        //                    activity?.runOnUiThread {
        //                        Toast.makeText(context,
        //                                getString(R.string.ErrorConnection),
        //                                Toast.LENGTH_SHORT).show()
        //                    }
        //                }
        //
        //            }
        //
        //            if (loadFromDatabase || error) {
        //                val chapterList = MyApp.database?.chapterDao()?.getChaptersForRanobe(
        //                        ranobe.url)
        //                ranobe.chapterList = chapterList as MutableList<Chapter>
        //
        //            }
        //
        //            ranobeList.add(ranobe)
        //            if (ranobe.ranobeSite != Title.url) {
        //                progressDialog!!.incrementProgressBy(1)
        //            }
        //
        //        }
        //
        //        loadFromDatabase = false

    }

    private fun savedLoadRanobe(): Single<List<Ranobe>> {

        MyApp.ranobe = null


        return MyApp.database?.textDao()?.allText()?.map { mChapterTexts ->

            val savedList = mutableListOf<Ranobe>()

            val groupList = mChapterTexts.groupBy { it.ranobeUrl }

            for (group in groupList) {
                val newRanobe = Ranobe()
                newRanobe.url = group.key
                newRanobe.title = group.value.first().ranobeName
                val chapterList = mutableListOf<Chapter>()
                chapterList.addAll(group.value.map { it -> Chapter(it) })
                newRanobe.chapterList = chapterList

                val titleRanobe = Ranobe(Title)
                titleRanobe.title = (Constants.RanobeSite.fromUrl(group.key)?.title ?: None.title)

                savedList.add(titleRanobe)

                savedList.add(newRanobe)

            }

            return@map savedList.toList()
        } ?: Single.just(listOf<Ranobe>())

    }

    override fun onDestroy() {

        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
        request?.dispose()
        super.onDestroy()
    }

    private fun rulateLoadRanobe(): Single<List<Ranobe>> {
        return RulateRepository.getReadyBooks(page + 1)
    }

    private fun ranobeRfLoadRanobe(): Single<List<Ranobe>> {

        return RanobeRfRepository.getReadyBooks(page + 1)

    }

    private fun ranobeHubLoadRanobe(): Single<List<Ranobe>> {

        return RanobeHubRepository.getReadyBooks(page + 1)

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        mListener = null
        request?.dispose()
        super.onDetach()
    }

    override fun onDestroyView() {
        request?.dispose()
        super.onDestroyView()
    }

    interface OnListFragmentInteractionListener

    companion object {

        fun newInstance(fragmentType: String): RanobeListFragment {
            val fragment = RanobeListFragment()
            val args = Bundle()
            args.putString(fragmentBundle, fragmentType)
            MyApp.fragmentType = Constants.FragmentType.valueOf(fragmentType)
            fragment.arguments = args
            return fragment
        }
    }

}


