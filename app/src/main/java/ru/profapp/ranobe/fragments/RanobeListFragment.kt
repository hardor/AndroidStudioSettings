package ru.profapp.ranobe.fragments

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.adapters.RanobeRecyclerViewAdapter
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.common.Constants.RanobeSite.Error
import ru.profapp.ranobe.common.Constants.fragmentBundle
import ru.profapp.ranobe.common.OnLoadMoreListener
import ru.profapp.ranobe.helpers.LogType
import ru.profapp.ranobe.helpers.logError
import ru.profapp.ranobe.helpers.setVectorForPreLollipop
import ru.profapp.ranobe.models.Chapter
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.network.repositories.RanobeHubRepository
import ru.profapp.ranobe.network.repositories.RanobeRfRepository
import ru.profapp.ranobe.network.repositories.RulateRepository
import ru.profapp.ranobe.utils.GlideApp
import java.net.UnknownHostException

class RanobeListFragment : Fragment() {

    private val ranobeList = mutableListOf<Ranobe>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var ranobeRecyclerViewAdapter: RanobeRecyclerViewAdapter
    private var mContext: Context? = null
    private var fragmentType: Constants.FragmentType? = null
    private var page: Int = 0
    private var loadFromDatabase: Boolean = false
    private var oldListSize: Int = 0
    private var request: Disposable? = null

    private var checkedSortItemName: String = Constants.SortOrder.default.name

    private fun getRulateWebFavorite(): Single<List<Ranobe>> {

        val token = MyApp.preferencesManager.rulateToken
        if (token.isBlank())
            return Single.just(listOf())
        return RulateRepository.getFavoriteBooks(token)
    }

    private fun getRanobeRfWebFavorite(): Single<List<Ranobe>> {

        val token = MyApp.preferencesManager.ranoberfToken
        if (token.isBlank())
            return Single.just(listOf())
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

        if (arguments != null && arguments!!.containsKey(fragmentBundle)) {
            fragmentType = Constants.FragmentType.valueOf(arguments!!.getString(fragmentBundle)
                    ?: "")
        }

    }

    private fun sortValuesAndNotify(sortOrderName: String) {

        val tempList = when (sortOrderName) {
            Constants.SortOrder.ByTitle.name -> ranobeList.sortedBy { r -> r.title }
            Constants.SortOrder.ByDate.name -> ranobeList.sortedByDescending { r -> r.readyDate }
            Constants.SortOrder.ByUpdates.name -> ranobeList.sortedByDescending { r -> r.newChapters }
            else -> ranobeList.sortedBy { r -> r.ranobeSite }.sortedBy { h -> h.title }
        }
        ranobeList.clear()
        ranobeList.addAll(tempList)

        ranobeRecyclerViewAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_ranobe_list, container, false)
        progressDialog = ProgressDialog(mContext)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rV_ranobeList_ranobe)

        recyclerView.layoutManager = LinearLayoutManager(mContext)

        ranobeRecyclerViewAdapter = RanobeRecyclerViewAdapter(GlideApp.with(mContext!!), recyclerView, ranobeList)

        recyclerView.adapter = ranobeRecyclerViewAdapter

        val iBrLFragmentSync: ImageButton = view.findViewById(R.id.iB_rL_fragment_sync)
        val tVSortOrder: TextView = view.findViewById(R.id.tV_SortOrder)
        if (fragmentType == Constants.FragmentType.Favorite && mContext != null) {
            iBrLFragmentSync.visibility = View.VISIBLE
            iBrLFragmentSync.setOnClickListener {

                val builder = AlertDialog.Builder(mContext!!)
                builder.setTitle(getString(R.string.load_web_bookmarks))
                        .setMessage(getString(R.string.load_web_bookmarks_message))
                        .setIcon(R.drawable.ic_info_black_24dp)
                        .setCancelable(true)
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.cancel()
                        }
                        .setPositiveButton("OK") { dialog, _ ->

                            swipeRefreshLayout.isRefreshing = true

                            ranobeList.clear()
                            ranobeRecyclerViewAdapter.notifyDataSetChanged()

                            Single.zip(
                                    getRulateWebFavorite().onErrorReturn {
                                        listOf(Ranobe(Error))
                                    },
                                    getRanobeRfWebFavorite().onErrorReturn {
                                        return@onErrorReturn listOf(Ranobe(Error))
                                    },

                                    getRanobeHubWebFavorite().onErrorReturn {
                                        return@onErrorReturn listOf(Ranobe(Error))
                                    },
                                    MyApp.database.ranobeDao().getLocalFavoriteRanobes().onErrorReturn {
                                        return@onErrorReturn listOf<Ranobe>()
                                    },
                                    io.reactivex.functions.Function4<List<Ranobe>, List<Ranobe>, List<Ranobe>, List<Ranobe>, List<Ranobe>>
                                    { Rulate, RanobeRf, RanobeHub, local ->

                                        val newList = mutableListOf<Ranobe>()

                                        val isRulateError = Rulate.firstOrNull()?.ranobeSite == Error.url
                                        val isRanobeRfError = RanobeRf.firstOrNull()?.ranobeSite == Error.url
                                        val isRanobeHubError = RanobeHub.firstOrNull()?.ranobeSite == Error.url

                                        if (!isRulateError) newList.addAll(Rulate)
                                        if (!isRanobeRfError) newList.addAll(RanobeRf)
                                        if (!isRanobeHubError) newList.addAll(RanobeHub)

                                        MyApp.database.ranobeDao().insertAll(*newList.toTypedArray())

                                        //                                        if (isRanobeHubError || isRulateError || isRanobeRfError) {
                                        //                                            val errorString: String = (if (isRulateError) " Rulate " else "") + (if (isRanobeRfError) " Ранобэ.рф " else "") + (if (isRanobeHubError) " RanobeHub " else "")
                                        //                                                                                       runOnUiThread {
                                        //                                                                                            Toast.makeText(this, getString(R.string.no_connection_to) + errorString, Toast.LENGTH_SHORT).show()
                                        //                                                                                        }
                                        //                                        }

                                        for (ranobe in local) {
                                            if (!newList.any { its -> its.url == ranobe.url }) {
                                                if (ranobe.isFavoriteInWeb) {
                                                    if ((!isRulateError && ranobe.ranobeSite == Constants.RanobeSite.Rulate.url)
                                                            || (!isRanobeRfError && ranobe.ranobeSite == Constants.RanobeSite.RanobeRf.url)
                                                            || (!isRanobeHubError && ranobe.ranobeSite == Constants.RanobeSite.RanobeHub.url))
                                                        MyApp.database.ranobeDao().deleteByUrl(ranobe.url)
                                                } else if (!ranobe.isFavoriteInWeb) {
                                                    newList.add(ranobe)
                                                }
                                            }
                                        }




                                        return@Function4 newList
                                    }

                            )
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ result ->
                                        if (result.any()) {
                                            ranobeList.clear()
                                            ranobeList.addAll(result)
                                            sortValuesAndNotify(checkedSortItemName)

                                        }
                                        onItemsLoadFinished()
                                    }, { error ->
                                        logError(LogType.ERROR, "sync favorite", "", error.fillInStackTrace(), true)
                                        onItemsLoadFinished(error)
                                    })

                            dialog.cancel()
                        }

                builder.create()?.show()
            }
            checkedSortItemName = MyApp.preferencesManager.sortOrder
            tVSortOrder.text = getString(Constants.SortOrder.valueOf(checkedSortItemName).resId)
            setVectorForPreLollipop(tVSortOrder, R.drawable.ic_sort_by_alpha_black_24dp, mContext!!, Constants.ApplicationConstants.DRAWABLE_LEFT)
            tVSortOrder.visibility = View.VISIBLE
            tVSortOrder.setOnClickListener {

                val items = Constants.SortOrder.toArray(mContext!!)
                val stringItems = items.map {
                    return@map resources.getString(it)
                }.toTypedArray()
                val currentItem = items.indexOf(Constants.SortOrder.valueOf(checkedSortItemName).resId)

                val builder = AlertDialog.Builder(mContext!!)
                builder.setTitle(getString(R.string.load_web_bookmarks))
                        .setIcon(R.drawable.ic_info_black_24dp)
                        .setSingleChoiceItems(stringItems, currentItem) { dialog, which ->
                            val selectItem = items[which]
                            checkedSortItemName = Constants.SortOrder.fromResId(selectItem).name

                            tVSortOrder.text = getString(selectItem)
                            sortValuesAndNotify(checkedSortItemName)
                            recyclerView.scrollToPosition(0)
                            MyApp.preferencesManager.sortOrder = checkedSortItemName
                            dialog.dismiss()
                        }
                        .setCancelable(true)
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.cancel()
                        }

                builder.create()?.show()
            }

        } else {
            iBrLFragmentSync.visibility = View.GONE
            tVSortOrder.visibility = View.GONE
            iBrLFragmentSync.setOnClickListener(null)
            tVSortOrder.setOnClickListener(null)
        }

        //set load more listener for the RecyclerView adapterExpandable
        if (fragmentType != Constants.FragmentType.Favorite
                && fragmentType != Constants.FragmentType.Search
                && fragmentType != Constants.FragmentType.Saved) {

            ranobeRecyclerViewAdapter.onLoadMoreListener = object : OnLoadMoreListener {
                override fun onLoadMore() {
                    refreshItems(false)
                }
            }
        }
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        swipeRefreshLayout.setOnRefreshListener {
            page = 0
            refreshItems(true)
        }
        swipeRefreshLayout.isRefreshing = true

        refreshItems(true)
        return view
    }

    private fun refreshItems(remove: Boolean) {

        swipeRefreshLayout.isRefreshing = true
        oldListSize = ranobeList.size


        val loader: Observable<List<Ranobe>> = when (fragmentType) {
            Constants.FragmentType.Rulate -> rulateLoadRanobe().toObservable()
            Constants.FragmentType.Ranoberf -> ranobeRfLoadRanobe().toObservable()
            Constants.FragmentType.RanobeHub -> ranobeHubLoadRanobe().toObservable()
            Constants.FragmentType.Favorite -> {
                favoriteLoadRanobe()
            }
            Constants.FragmentType.Saved -> savedLoadRanobe().toObservable()
            else -> favoriteLoadRanobe()
        }



        request?.dispose()
        request = loader
                // Find images
                .map { ranobeList ->
                    for (ranobe in ranobeList) {
                        if (ranobe.image.isNullOrBlank()) {
                            MyApp.database.ranobeImageDao().getImageByUrl(ranobe.url).subscribeOn(Schedulers.io())?.subscribe { it ->
                                ranobe.image = it.image
                            }
                        }
                    }
                    return@map ranobeList

                } //Check lastReaded
                .map { ranobes ->
                    if (fragmentType == Constants.FragmentType.Favorite) {
                        for (ranobe in ranobes) {
                            ranobe.newChapters = 0
                            val chapterList = ranobe.chapterList

                            if (chapterList.isNotEmpty()) {
                                var templist = chapterList.filter { it.canRead }
                                if (!templist.any()) {
                                    templist = chapterList.take(Constants.chaptersNum)
                                }
                                val chapterlist2 = templist.take(Constants.chaptersNum)

                                val lastChapterUrl = MyApp.preferencesManager.getLastChapterUrl(chapterlist2.first().ranobeUrl)

                                if (lastChapterUrl.isNotEmpty()) {
                                    val chapterIndex = chapterlist2.firstOrNull { c -> c.url == lastChapterUrl }?.index
                                    if (chapterIndex != null) {
                                        for (chapter in chapterlist2) {
                                            chapter.isRead = chapter.index <= chapterIndex
                                            ranobe.newChapters += if (chapter.isRead) 0 else 1

                                        }
                                    }
                                } else {
                                    val lastId = MyApp.preferencesManager.getLastChapterId(chapterlist2.first().ranobeUrl)
                                    if (lastId > 0) {
                                        for (chapter in chapterlist2) {
                                            if (chapter.id != null) {
                                                chapter.isRead = chapter.id!! <= lastId
                                                ranobe.newChapters += if (chapter.isRead) 0 else 1
                                            }
                                        }
                                    }
                                }


                            }
                        }
                    }
                    return@map ranobes
                }

                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->

                    if (remove) {
                        page = 0
                        oldListSize = ranobeList.size
                        ranobeList.clear()
                        ranobeRecyclerViewAdapter.notifyItemRangeRemoved(0, oldListSize)
                        oldListSize = 0
                    }

                    if (fragmentType == Constants.FragmentType.Saved) {

                        Snackbar.make(swipeRefreshLayout, R.string.saved_info, Snackbar.LENGTH_SHORT).show()
                    }

                    if (result.any()) {
                        ranobeList.addAll(result)
                        if (fragmentType == Constants.FragmentType.Favorite) sortValuesAndNotify(checkedSortItemName)
                        else ranobeRecyclerViewAdapter.notifyItemRangeInserted(oldListSize, ranobeList.size)
                        oldListSize = ranobeList.size
                    }
                    page++
                    onItemsLoadFinished()

                }, { error ->
                    logError(LogType.ERROR, "refreshItems", "", error.fillInStackTrace(), false)
                    onItemsLoadFinished(error)
                })

    }

    private fun onItemsLoadFinished(error: Throwable? = null) {

        if (error != null) {

            if (error is UnknownHostException) {
                Toast.makeText(mContext, R.string.error_connection, Toast.LENGTH_SHORT).show()
            } else if (error is CompositeException) {
                var errorConnection = false
                for (err in error.exceptions) {
                    if (err is UnknownHostException) {
                        Toast.makeText(mContext, R.string.error_connection, Toast.LENGTH_SHORT).show()
                        errorConnection = true
                        break
                    }
                }
                if (!errorConnection)
                    Toast.makeText(mContext, R.string.error, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, R.string.error, Toast.LENGTH_SHORT).show()
            }

        }

        ranobeRecyclerViewAdapter.setLoaded()
        swipeRefreshLayout.isRefreshing = false
        progressDialog.dismiss()
    }

    private fun favoriteLoadRanobe(): Observable<List<Ranobe>> {

        progressDialog.setMessage(resources.getString(R.string.load_please_wait))

        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        swipeRefreshLayout.isRefreshing = false
        progressDialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->

            request?.dispose()
            dialog.dismiss()
        }

        progressDialog.incrementProgressBy(-progressDialog.progress)

        if (loadFromDatabase) {

            progressDialog.setTitle(resources.getString(R.string.load_ranobes_from_db))
            progressDialog.show()
            return (MyApp.database.ranobeDao().getFavoriteRanobes().map { it ->

                val newRanobeList = mutableListOf<Ranobe>()
                newRanobeList.addAll(it.map map1@{ gr ->
                    gr.ranobe.chapterList = gr.chapterList
                    return@map1 gr.ranobe
                })
                return@map newRanobeList.toList()
            }?.doOnSuccess { loadFromDatabase = false }?.toObservable()
                    ?: Observable.just(listOf()))

        } else {
            progressDialog.setTitle(resources.getString(R.string.Load_from_Rulate))
            progressDialog.show()

            return MyApp.database.ranobeDao().getLocalFavoriteRanobes().map { ranobeList ->
                progressDialog.max = ranobeList.size
                for (ranobe in ranobeList) {
                    activity?.runOnUiThread {
                        progressDialog.setTitle(ranobe.title)
                        progressDialog.incrementProgressBy(1)
                    }
                    ranobe.updateRanobe(mContext!!).blockingGet()
                }
                return@map ranobeList
            }.map { result ->
                for (ranobe in result) {
                    MyApp.database.ranobeDao().insert(ranobe)
                    MyApp.database.chapterDao().insertAll(*ranobe.chapterList.toTypedArray())
                }
                return@map result
            }.toObservable()

        }

    }

    private fun savedLoadRanobe(): Single<List<Ranobe>> {

        MyApp.ranobe = null


        return MyApp.database.textDao().allText().map { mChapterTexts ->

            val savedList = mutableListOf<Ranobe>()

            val groupList = mChapterTexts.groupBy { it.ranobeUrl }

            for (group in groupList) {
                val newRanobe = Ranobe()
                newRanobe.url = group.key
                newRanobe.title = group.value.first().ranobeName
                val chapterList = mutableListOf<Chapter>()

                chapterList.addAll(group.value.asSequence().map { it -> Chapter(it) }.sortedBy { ch -> ch.index })

                newRanobe.chapterList = chapterList
                newRanobe.wasUpdated = true
                savedList.add(newRanobe)
            }

            return@map savedList.toList()
        } ?: Single.just(listOf())

    }

    override fun onDestroy() {
        super.onDestroy()
        mContext = null
        request?.dispose()
        progressDialog.dismiss()
        MyApp.refWatcher?.watch(this)
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
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
        progressDialog.dismiss()

    }

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


