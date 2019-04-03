package ru.profapp.ranobe.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.adapters.RanobeRecyclerViewAdapter
import ru.profapp.ranobe.flex.ChapterHistoryItem
import ru.profapp.ranobe.flex.ProgressItem
import ru.profapp.ranobe.helpers.GlideApp
import ru.profapp.ranobe.helpers.logError
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.models.RanobeHistory


class HistoryPageFragment : Fragment() {


    private val mChapterProgressItem = ProgressItem()
    private var mContext: Context? = null

    private var pageNumber: Int = 0
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var ranobeRecyclerView: RecyclerView? = null
    private var chapterRecyclerView: RecyclerView? = null
    private var mChapterHistoryAdapter: FlexibleAdapter<IFlexible<*>>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            pageNumber = it.getInt(ARGUMENT_PAGE_NUMBER)
        }
    }


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View?


        when (pageNumber) {

            1 -> {
                view = inflater.inflate(R.layout.fragment_history_chapters, container, false)

                mChapterHistoryAdapter = FlexibleAdapter(listOf())
                mChapterHistoryAdapter?.apply {
                    setLoadingMoreAtStartUp(savedInstanceState == null)
                    setEndlessScrollListener(object : FlexibleAdapter.EndlessScrollListener {
                        override fun noMoreLoad(newItemsSize: Int) {
                        }

                        override fun onLoadMore(lastPosition: Int, currentPage: Int) {

                            mChapterHistoryAdapter?.let {
                                if (it.hasFilter()) {
                                    mChapterHistoryAdapter?.onLoadMoreComplete(null);
                                    return;
                                }
                            }

                            val chapterRequest = MyApp.database.ranobeHistoryDao()
                                .getChapters(PAGE_SIZE, currentPage * PAGE_SIZE)
                                .subscribeOn(Schedulers.io())
                                .observeOn(mainThread())
                                .subscribe({ newItems ->
                                    mChapterHistoryAdapter?.onLoadMoreComplete(newItems.map { ch ->
                                        ChapterHistoryItem(ch)
                                    },
                                        if (newItems.isEmpty()) -1 else DELAY)
                                }, { error ->
                                    mChapterProgressItem.status = ProgressItem.StatusEnum.ON_ERROR
                                    mChapterHistoryAdapter?.onLoadMoreComplete(null, DELAY)
                                    logError(TAG, "load chapters from database: $currentPage", error)
                                })
                            compositeDisposable.add(chapterRequest)

                        }


                    }, mChapterProgressItem)
                    endlessPageSize = PAGE_SIZE
                    setAnimationOnForwardScrolling(true)

                }

                chapterRecyclerView = view.findViewById(R.id.rV_history_chapters)
                chapterRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mContext)
                    setHasFixedSize(true)
                    adapter = mChapterHistoryAdapter
                    itemAnimator = DefaultItemAnimator()
                }


            }
            else -> {
                pageNumber = 0
                view = inflater.inflate(R.layout.fragment_history_ranobe, container, false)
                ranobeRecyclerView = view.findViewById(R.id.rV_history_ranobe)
                ranobeRecyclerView!!.layoutManager = LinearLayoutManager(mContext)

                val ranobeRequest = MyApp.database.ranobeHistoryDao().allRanobes().map {
                    val ranobeList = it.map { r -> toRanobe(r) }
                    for (ranobe in ranobeList) {
                        if (ranobe.image.isNullOrBlank()) {
                            MyApp.database.ranobeImageDao().getImageByUrl(ranobe.url)
                                .subscribeOn(Schedulers.io()).subscribe { it2 ->
                                    ranobe.image = it2.image
                                }
                        }
                    }
                    return@map ranobeList

                }.subscribeOn(Schedulers.io()).observeOn(mainThread()).subscribe({
                    ranobeRecyclerView!!.adapter = RanobeRecyclerViewAdapter(GlideApp.with(
                        mContext!!), ranobeRecyclerView!!, it)
                }, { error ->
                    logError(TAG, "", error)
                })

                compositeDisposable.add(ranobeRequest)

            }
        }

        return view

    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDestroy() {

        mContext = null
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun toRanobe(ranobeHistory: RanobeHistory): Ranobe {

        val ranobe = Ranobe()
        ranobe.url = ranobeHistory.ranobeUrl
        ranobe.title = ranobeHistory.ranobeName
        ranobe.description = ranobeHistory.description
        return ranobe
    }

    companion object {

        private val TAG = "History Page Fragment"

        private val PAGE_SIZE = 15
        private val DELAY = 3000L

        const val ARGUMENT_PAGE_NUMBER = "arg_page_number"

        fun newInstance(page: Int): HistoryPageFragment {
            val pageFragment = HistoryPageFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_PAGE_NUMBER, page)
            pageFragment.arguments = arguments
            return pageFragment
        }

    }
}
