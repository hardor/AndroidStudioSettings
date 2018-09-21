package ru.profapp.RanobeReader.Fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import ru.profapp.RanobeReader.Adapters.RanobeRecyclerViewAdapter
import ru.profapp.RanobeReader.Common.ErrorConnectionException
import ru.profapp.RanobeReader.Common.OnLoadMoreListener
import ru.profapp.RanobeReader.Common.RanobeConstants
import ru.profapp.RanobeReader.Common.RanobeConstants.RanobeSite.*
import ru.profapp.RanobeReader.Common.RanobeConstants.chaptersNum
import ru.profapp.RanobeReader.Common.RanobeConstants.fragmentBundle
import ru.profapp.RanobeReader.Common.StringResources
import ru.profapp.RanobeReader.Common.StringResources.is_readed_Pref
import ru.profapp.RanobeReader.Helpers.MyLog
import ru.profapp.RanobeReader.Helpers.RanobeKeeper
import ru.profapp.RanobeReader.JsonApi.*
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class RanobeRecyclerFragment : Fragment() {
    val gson = GsonBuilder().setLenient().create()!!
    val ranobeList = ArrayList<Ranobe>()
    var progressDialog: ProgressDialog? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private val ExceptionHandler = Thread.UncaughtExceptionHandler { th, ex ->
        MyLog.SendError(MyLog.LogType.WARN, "RanobeRecyclerFragment", "Uncaught exception", ex)
        progressDialog!!.dismiss()
        mSwipeRefreshLayout!!.isRefreshing = false
    }
    private var mListener: OnListFragmentInteractionListener? = null
    private lateinit var mRanobeRecyclerViewAdapter: RanobeRecyclerViewAdapter
    var mContext: Context? = null
    private var fragmentType: RanobeConstants.FragmentType? = null
    private var page: Int = 0
    var loadFromDatabase: Boolean = false
    private var oldListSize: Int = 0
    private var request: Disposable? = null
    val rulateWebFavorite: List<Ranobe>
        get() {

            val resultList = ArrayList<Ranobe>()
            val mPreferences = mContext!!.getSharedPreferences(
                    StringResources.Rulate_Login_Pref, 0)

            val token = mPreferences.getString(StringResources.KEY_Token, "")
            if (token != "") {
                try {
                    val response = JsonRulateApi.getInstance()!!.GetFavoriteBooks(token)

                    val jsonObject = JSONObject(response)
                    if (jsonObject.get("status") == "success") {
                        val jsonArray = jsonObject.getJSONArray("response")
                        for (i in 0 until jsonArray.length()) {

                            val value = jsonArray.getJSONObject(i)
                            var ranobe = Ranobe()
                            ranobe.UpdateRanobe(value,
                                    RanobeConstants.JsonObjectFrom.RulateFavorite)

                            if (!loadFromDatabase) {
                                ranobe.updateRanobe(mContext!!)
                                ranobe.isFavoriteInWeb = true
                                MyApp.database?.ranobeDao()?.insert(
                                        ranobe)
                                MyApp.database?.chapterDao()?.insertAll(*ranobe.chapterList.toTypedArray())
                            } else {
                                val dbRanobe = MyApp.database?.ranobeDao()?.getRanobeByUrl(
                                        ranobe.url)
                                if (dbRanobe != null) {
                                    ranobe = dbRanobe
                                }
                            }
                            ranobe.isFavoriteInWeb = true
                            resultList.add(ranobe)

                        }

                    }
                } catch (e: JSONException) {
                    MyLog.SendError(MyLog.LogType.WARN, "RanobeRecyclerFragment",
                            "", e)

                    activity?.runOnUiThread { onItemsLoadFailed(e) }

                } catch (e: NullPointerException) {
                    MyLog.SendError(MyLog.LogType.WARN, "RanobeRecyclerFragment", "", e)

                    activity?.runOnUiThread { onItemsLoadFailed(e) }

                } catch (e: ErrorConnectionException) {
                    MyLog.SendError(MyLog.LogType.WARN,
                            "RanobeRecyclerFragment", "", e)
                    Objects.requireNonNull<FragmentActivity>(activity).runOnUiThread { onItemsLoadFailed(e) }
                }

            }
            return resultList
        }


    private val ranoberfWebFavorite: List<Ranobe>
        get() {

            val resultList = ArrayList<Ranobe>()
            val mPreferences = mContext!!.getSharedPreferences(
                    StringResources.Ranoberf_Login_Pref, 0)

            val token = mPreferences.getString(StringResources.KEY_Token, "")
            if (token != "") {

                try {
                    val response = JsonRanobeRfApi.getInstance()!!.GetFavoriteBooks(token!!)
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getInt("status") == 200) {

                        val jsonArray = jsonObject.getJSONArray("result")
                        for (i in 0 until jsonArray.length()) {

                            val value = jsonArray.getJSONObject(i)
                            var ranobe = Ranobe()
                            ranobe.url = value.getString("bookAlias")
                            ranobe.title = value.getString("bookTitle")
                            ranobe.bookmarkIdRf = value.getInt("id")
                            ranobe.image = value.getString("bookImage")
                            ranobe.ranobeSite = RanobeRf.url

                            if (!loadFromDatabase) {
                                ranobe.updateRanobe(mContext!!)
                                ranobe.isFavoriteInWeb = true
                                MyApp.database?.ranobeDao()?.insert(
                                        ranobe)
                                MyApp.database?.chapterDao()?.insertAll(
                                        *ranobe.chapterList.toTypedArray())
                            } else {
                                val dbRanobe = MyApp.database?.ranobeDao()?.getRanobeByUrl(
                                        ranobe.url)

                                ranobe = dbRanobe!!

                            }
                            ranobe.isFavoriteInWeb = true
                            resultList.add(ranobe)

                        }

                    }
                } catch (e: JSONException) {
                    MyLog.SendError(MyLog.LogType.WARN, "RanobeRecyclerFragment",
                            "", e)

                    activity?.runOnUiThread { onItemsLoadFailed(e) }

                } catch (e: NullPointerException) {
                    MyLog.SendError(MyLog.LogType.WARN, "RanobeRecyclerFragment", "", e)

                    activity?.runOnUiThread { onItemsLoadFailed(e) }

                } catch (e: ErrorConnectionException) {
                    MyLog.SendError(MyLog.LogType.WARN,
                            "RanobeRecyclerFragment", "", e)
                    Objects.requireNonNull<FragmentActivity>(activity).runOnUiThread { onItemsLoadFailed(e) }
                }

            }
            return resultList
        }

    init {
        page = 0
        loadFromDatabase = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (arguments != null && arguments!!.containsKey(fragmentBundle)) {
            fragmentType = RanobeConstants.FragmentType.valueOf(arguments!!.getString(fragmentBundle))
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_ranobe_list, container, false)

        // Set the adapterExpandable
        if (view is SwipeRefreshLayout) {

            mContext = view.getContext()
            val recyclerView = view.findViewById<RecyclerView>(R.id.ranobeListView)

            recyclerView.layoutManager = LinearLayoutManager(mContext)

            mRanobeRecyclerViewAdapter = RanobeRecyclerViewAdapter(recyclerView, ranobeList)

            recyclerView.adapter = mRanobeRecyclerViewAdapter

            //set load more listener for the RecyclerView adapterExpandable
            if (fragmentType != RanobeConstants.FragmentType.Favorite
                    && fragmentType != RanobeConstants.FragmentType.Search
                    && fragmentType != RanobeConstants.FragmentType.Saved) {

                mRanobeRecyclerViewAdapter.onLoadMoreListener = OnLoadMoreListener { refreshItems(false) }


            }
            mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
            mSwipeRefreshLayout!!.setOnRefreshListener {
                mSwipeRefreshLayout!!.isRefreshing = false
                page = 0
                refreshItems(true)
            }
            mSwipeRefreshLayout!!.isRefreshing = true

            refreshItems(true)
        }

        return view
    }

    private fun refreshItems(remove: Boolean) {

        mSwipeRefreshLayout!!.isRefreshing = true
        oldListSize = ranobeList.size
        if (remove) {
            page = 0
            oldListSize = ranobeList.size
            ranobeList.clear()
            mRanobeRecyclerViewAdapter.notifyItemRangeRemoved(0, oldListSize)
            oldListSize = 0
        }
        //Todo: remove sPref. Move to lastIndexPref
        val sPref = mContext!!.getSharedPreferences(is_readed_Pref, MODE_PRIVATE)
        val lastIndexPref = mContext!!.getSharedPreferences(is_readed_Pref, MODE_PRIVATE)

        val loader: Observable<ArrayList<Ranobe>> = when (fragmentType) {
            RanobeConstants.FragmentType.Rulate -> rulateLoadRanobe()
            RanobeConstants.FragmentType.Ranoberf -> ranoberfLoadRanobe()
            RanobeConstants.FragmentType.RanobeHub -> ranobehubLoadRanobe()
            RanobeConstants.FragmentType.Favorite -> rulateLoadRanobe()
            RanobeConstants.FragmentType.Saved -> savedLoadRanobe()
            else -> throw NullPointerException()
        }




        request = loader
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map { ranobeList ->
                    for (ranobe in ranobeList) {
                        if (ranobe.image.isNullOrBlank()) {
                            MyApp.database?.ranobeImageDao()?.getImageByRanobeId(ranobe.id)?.observeOn(AndroidSchedulers.mainThread())?.subscribeOn(Schedulers.io())?.subscribe { it ->
                                ranobe.image = it.image
                            }
                        }
                    }
                    return@map ranobeList

                }
                .map { it ->
                    var checked = false
                    if (lastIndexPref != null) {
                        for (ranobe in it) {
                            if (lastIndexPref.contains(ranobe.url)) {
                                val lastId = lastIndexPref.getInt(ranobe.url, -1)
                                if (lastId > 0) {
                                    checked = true
                                    val sizeList = ranobe.chapterList.size
                                    for (chapter in ranobe.chapterList.subList(0, Math.min(chaptersNum, sizeList))) {
                                        if (chapter.id <= lastId) {
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
                    mRanobeRecyclerViewAdapter.setLoaded()
                    page++
                    mSwipeRefreshLayout!!.isRefreshing = false

                }, { error ->
                    MyLog.SendError(MyLog.LogType.ERROR, "refreshItems", "", error.fillInStackTrace())
                    onItemsLoadFailed(error)

                })


    }

    private fun onItemsLoadComplete(remove: Boolean) {

        //        mRanobeRecyclerViewAdapter.notifyItemRangeInserted(oldListSize,
        //                ranobeList.size() - oldListSize);


    }

    private fun onItemsLoadFailed(error: Throwable) {
        if (error is IOException)
            Toast.makeText(mContext, R.string.ErrorConnection, Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(mContext, R.string.Error, Toast.LENGTH_SHORT).show()
        mRanobeRecyclerViewAdapter.setLoaded()

        mSwipeRefreshLayout!!.isRefreshing = false
    }

    private fun favoriteLoadRanobe(remove: Boolean) {

        progressDialog = ProgressDialog(mContext)
        progressDialog!!.setMessage(resources.getString(R.string.load_please_wait))
        progressDialog!!.setTitle(resources.getString(R.string.load_ranobes))
        progressDialog!!.setCancelable(true)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        mSwipeRefreshLayout!!.isRefreshing = false
        progressDialog!!.show()

        val t = object : Thread() {

            override fun run() {

                var step = 0

                val newRanobeList = ArrayList<Ranobe>()

                activity?.runOnUiThread {
                    progressDialog!!.setTitle(
                            mContext!!.getString(R.string.Load_local_bookmarks))
                }

                var favRanobeList = MyApp.database?.ranobeDao()?.getFavoriteBySite(Rulate.url)
                        ?: ArrayList()
                if (favRanobeList.isNotEmpty()) {
                    val TitleRanobe = Ranobe()
                    TitleRanobe.title = getString(R.string.tl_rulate_name)
                    TitleRanobe.ranobeSite = Title.url
                    newRanobeList.add(TitleRanobe)
                    newRanobeList.addAll(favRanobeList)
                    step--
                }

                favRanobeList = MyApp.database?.ranobeDao()?.getFavoriteBySite(RanobeRf.url) ?: ArrayList()
                if (favRanobeList.isNotEmpty()) {
                    val TitleRanobe = Ranobe()
                    TitleRanobe.title = getString(R.string.ranobe_rf)
                    TitleRanobe.ranobeSite = Title.url
                    newRanobeList.add(TitleRanobe)
                    newRanobeList.addAll(favRanobeList)
                    step--
                }

                activity?.runOnUiThread {
                    progressDialog!!.setTitle(
                            mContext!!.getString(R.string.Load_from_Rulate))
                }

                val rulateWebList = rulateWebFavorite
                if (rulateWebList.isNotEmpty()) {
                    val TitleRanobe = Ranobe()
                    TitleRanobe.title = mContext!!.getString(R.string.tl_rulate_web)
                    TitleRanobe.ranobeSite = Title.url
                    newRanobeList.add(TitleRanobe)
                    step--
                }
                newRanobeList.addAll(rulateWebList)

                activity?.runOnUiThread {
                    progressDialog!!.setTitle(
                            mContext!!.getString(R.string.Load_from_RanobeRf))

                }
                val ranoberfWebList = ranoberfWebFavorite
                if (ranoberfWebList.isNotEmpty()) {
                    val TitleRanobe = Ranobe()
                    TitleRanobe.title = mContext!!.getString(R.string.ranoberf_web)
                    TitleRanobe.ranobeSite = Title.url
                    newRanobeList.add(TitleRanobe)
                    step--
                }
                newRanobeList.addAll(ranoberfWebList)


                activity?.runOnUiThread {
                    progressDialog!!.setTitle(
                            mContext!!.getString(R.string.Update_ranobe_info))
                }

                progressDialog!!.max = newRanobeList.size + step

                for (ranobe in newRanobeList) {

                    var error = false


                    activity?.runOnUiThread { progressDialog!!.setMessage(ranobe.title) }


                    if (!loadFromDatabase) {
                        try {
                            ranobe.updateRanobe(mContext!!)
                            AsyncTask.execute {

                                if (!ranobe.isFavoriteInWeb && ranobe.ranobeSite != Title.url) {
                                    MyApp.database?.ranobeDao()?.update(ranobe)
                                    MyApp.database?.chapterDao()?.insertAll(*ranobe.chapterList.toTypedArray())

                                }

                            }
                        } catch (e: NullPointerException) {

                            error = true
                            activity?.runOnUiThread {
                                Toast.makeText(mContext, getString(R.string.Error),
                                        Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: ErrorConnectionException) {

                            error = true
                            activity?.runOnUiThread {
                                Toast.makeText(mContext,
                                        getString(R.string.ErrorConnection),
                                        Toast.LENGTH_SHORT).show()
                            }
                        }

                    }

                    if (loadFromDatabase || error) {
                        val chapterList = MyApp.database?.chapterDao()?.getChaptersForRanobe(
                                ranobe.url)
                        ranobe.chapterList = chapterList as MutableList<Chapter>

                    }

                    ranobeList.add(ranobe)
                    if (ranobe.ranobeSite != Title.url) {
                        progressDialog!!.incrementProgressBy(1)
                    }

                }

                loadFromDatabase = false



                activity?.runOnUiThread { onItemsLoadComplete(remove) }

                progressDialog!!.dismiss()

            }

        }
        t.uncaughtExceptionHandler = ExceptionHandler
        t.start()

    }

    private fun savedLoadRanobe(): Observable<ArrayList<Ranobe>> {

        MyApp.ranobe = null


        return MyApp.database?.textDao()?.allText()?.map { mChapterTexts ->
            val newRanobeList = ArrayList<Ranobe>()
            var prevRanobeName = ""
            var newRanobe = Ranobe()
            val historyList = ArrayList<Ranobe>()
            var chapterList: MutableList<Chapter> = ArrayList()

            for (mChapterText in mChapterTexts) {

                if (mChapterText.ranobeName != prevRanobeName) {

                    prevRanobeName = mChapterText.ranobeName
                    newRanobe = Ranobe()
                    chapterList = ArrayList()
                    newRanobe.title = mChapterText.ranobeName
                    newRanobe.chapterList = chapterList
                    newRanobeList.add(newRanobe)

                }
                val chapter = Chapter()
                chapter.ranobeName = mChapterText.ranobeName
                chapter.title = mChapterText.chapterName
                chapter.text = mChapterText.text
                chapter.url = mChapterText.chapterUrl

                newRanobe.url = chapter.ranobeUrl

                chapterList.add(chapter)
            }


            val rulateList = ArrayList<Ranobe>()
            val ranobeRfList = ArrayList<Ranobe>()
            val ranobeHubList = ArrayList<Ranobe>()
            for (ranobe in newRanobeList) {
                if (ranobe.ranobeSite == RanobeRf.url) {
                    ranobeRfList.add(ranobe)
                } else if (ranobe.ranobeSite == Rulate.url) {
                    rulateList.add(ranobe)
                } else if (ranobe.ranobeSite == RanobeHub.url) {
                    ranobeHubList.add(ranobe)
                }
            }
            if (ranobeRfList.size > 0) {
                val TitleRanobe = Ranobe()
                TitleRanobe.title = getString(R.string.ranobe_rf)
                TitleRanobe.ranobeSite = Title.url
                historyList.add(TitleRanobe)
                historyList.addAll(ranobeRfList)
            }
            if (rulateList.size > 0) {
                val TitleRanobe = Ranobe()
                TitleRanobe.title = getString(R.string.tl_rulate_name)
                TitleRanobe.ranobeSite = Title.url
                historyList.add(TitleRanobe)
                historyList.addAll(rulateList)
            }
            if (ranobeHubList.size > 0) {
                val TitleRanobe = Ranobe()
                TitleRanobe.title = getString(R.string.tl_rulate_name)
                TitleRanobe.ranobeSite = Title.url
                historyList.add(TitleRanobe)
                historyList.addAll(rulateList)
            }

            return@map historyList
        }?.toObservable() as Observable<ArrayList<Ranobe>>

    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.cancel()
        }
    }

    private fun rulateLoadRanobe(): Observable<ArrayList<Ranobe>> {
        return RepositoryProvider.provideRulateRepository().getReadyBooks(page + 1)
    }

    private fun ranoberfLoadRanobe(): Observable<ArrayList<Ranobe>> {

        return RepositoryProvider.provideRanobeRfRepository().getReadyBooks(page + 1)

    }

    private fun ranobehubLoadRanobe(): Observable<ArrayList<Ranobe>> {

        return RepositoryProvider.provideRanobeHubRepository().getReadyBooks(page + 1)

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
        super.onDetach()
        mListener = null
        request?.dispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        request?.dispose()
    }

    interface OnListFragmentInteractionListener

    companion object {

        fun newInstance(fragmentType: String): RanobeRecyclerFragment {
            val fragment = RanobeRecyclerFragment()
            val args = Bundle()
            args.putString(fragmentBundle, fragmentType)
            RanobeKeeper.fragmentType = RanobeConstants.FragmentType.valueOf(fragmentType)
            fragment.arguments = args
            return fragment
        }
    }

}

object RepositoryProvider {
    fun provideRulateRepository(): RulateRepository {
        return RulateRepository
    }

    fun provideRanobeHubRepository(): RanobeHubRepository {
        return RanobeHubRepository
    }

    fun provideRanobeRfRepository(): RanobeRfRepository {
        return RanobeRfRepository
    }
}
