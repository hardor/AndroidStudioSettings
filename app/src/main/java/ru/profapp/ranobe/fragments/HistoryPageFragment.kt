package ru.profapp.ranobe.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.profapp.ranobe.adapters.HistoryChaptersRecyclerViewAdapter
import ru.profapp.ranobe.adapters.RanobeRecyclerViewAdapter
import ru.profapp.ranobe.helpers.LogType
import ru.profapp.ranobe.helpers.logError

import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.models.RanobeHistory
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.utils.GlideApp

class HistoryPageFragment : Fragment() {

    private var mContext: Context? = null

    private var pageNumber: Int = 0
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var ranobeRecyclerView: RecyclerView? = null
    private var chapterRecyclerView: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments!!.getInt(ARGUMENT_PAGE_NUMBER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var view: View? = null


        when (pageNumber) {

            1 -> {
                view = inflater.inflate(R.layout.fragment_history_chapters, container, false)
                chapterRecyclerView = view.findViewById(R.id.rV_history_chapters)
                chapterRecyclerView!!.layoutManager = LinearLayoutManager(mContext)
                val chapterRequest = MyApp.database.ranobeHistoryDao().allChapters().subscribeOn(Schedulers.io())
                        .observeOn(mainThread()).subscribe({
                            chapterRecyclerView!!.adapter = HistoryChaptersRecyclerViewAdapter(it)
                        }, { error ->
                            logError(LogType.ERROR, "HistoryFragment", "", error)
                        })
                compositeDisposable.add(chapterRequest)
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
                            MyApp.database.ranobeImageDao().getImageByUrl(ranobe.url).subscribeOn(Schedulers.io()).subscribe { it2 ->
                                ranobe.image = it2.image
                            }
                        }
                    }
                    return@map ranobeList

                }.subscribeOn(Schedulers.io())
                        .observeOn(mainThread())
                        .subscribe({
                            ranobeRecyclerView!!.adapter = RanobeRecyclerViewAdapter(GlideApp.with(mContext!!), ranobeRecyclerView!!, it)
                        }, { error ->
                            logError(LogType.ERROR, "HistoryFragment", "", error)
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
        super.onDestroy()
        mContext = null
        compositeDisposable.dispose()
    }

    private fun toRanobe(ranobeHistory: RanobeHistory): Ranobe {

        val ranobe = Ranobe()
        ranobe.url = ranobeHistory.ranobeUrl
        ranobe.title = ranobeHistory.ranobeName
        ranobe.description = ranobeHistory.description
        return ranobe
    }

    fun update() {
        ranobeRecyclerView?.let { it ->
            it.adapter = RanobeRecyclerViewAdapter(GlideApp.with(context!!), ranobeRecyclerView!!, listOf())
            it.adapter?.notifyDataSetChanged()
        }

        chapterRecyclerView?.let { it ->
            it.adapter = HistoryChaptersRecyclerViewAdapter(listOf())
            it.adapter?.notifyDataSetChanged()
        }
    }

    companion object {
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
