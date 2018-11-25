package ru.profapp.ranobe.Fragments

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
import ru.profapp.ranobe.Adapters.HistoryChaptersRecyclerViewAdapter
import ru.profapp.ranobe.Adapters.RanobeRecyclerViewAdapter
import ru.profapp.ranobe.Helpers.LogHelper
import ru.profapp.ranobe.Models.Ranobe
import ru.profapp.ranobe.Models.RanobeHistory
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R

class HistoryPageFragment : Fragment() {

    private var pageNumber: Int = 0
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    var ranobeRecyclerView: RecyclerView? = null
    var chapterRecyclerView: RecyclerView? = null
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
                chapterRecyclerView!!.layoutManager = LinearLayoutManager(context)
                val chapterRequest = MyApp.database.ranobeHistoryDao().allChapters().subscribeOn(Schedulers.io())
                        .observeOn(mainThread()).subscribe({
                            chapterRecyclerView!!.adapter = HistoryChaptersRecyclerViewAdapter(context!!, it)
                        }, { error ->
                            LogHelper.logError(LogHelper.LogType.ERROR, "HistoryFragment", "", error)
                        })
                compositeDisposable.add(chapterRequest)
            }
            else -> {
                pageNumber = 0
                view = inflater.inflate(R.layout.fragment_history_ranobe, container, false)
                ranobeRecyclerView = view.findViewById(R.id.rV_history_ranobe)
                ranobeRecyclerView!!.layoutManager = LinearLayoutManager(context)

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
                            ranobeRecyclerView!!.adapter = RanobeRecyclerViewAdapter(context!!, ranobeRecyclerView!!, it)
                        }, { error ->
                            LogHelper.logError(LogHelper.LogType.ERROR, "HistoryFragment", "", error)
                        })

                compositeDisposable.add(ranobeRequest)

            }
        }

        return view

    }

    override fun onDestroy() {
        super.onDestroy()
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
            it.adapter = RanobeRecyclerViewAdapter(context!!, ranobeRecyclerView!!, listOf())
            it.adapter?.notifyDataSetChanged()
        }

        chapterRecyclerView?.let { it ->
            it.adapter = HistoryChaptersRecyclerViewAdapter(context!!, listOf())
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
