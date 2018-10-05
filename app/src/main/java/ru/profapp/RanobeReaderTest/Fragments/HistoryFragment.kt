package ru.profapp.RanobeReaderTest.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TabHost
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReaderTest.Adapters.HistoryRecyclerViewAdapter
import ru.profapp.RanobeReaderTest.Adapters.RanobeRecyclerViewAdapter
import ru.profapp.RanobeReaderTest.Helpers.LogHelper
import ru.profapp.RanobeReaderTest.Models.ChapterHistory
import ru.profapp.RanobeReaderTest.Models.Ranobe
import ru.profapp.RanobeReaderTest.Models.RanobeHistory
import ru.profapp.RanobeReaderTest.MyApp
import ru.profapp.RanobeReaderTest.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HistoryFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HistoryFragment : Fragment() {
    lateinit var tabHost: TabHost
    private lateinit var chapterRecyclerView: RecyclerView
    private lateinit var ranobeRecyclerView: RecyclerView
    private var chapterHistoryViewAdapter: HistoryRecyclerViewAdapter? = null
    private var ranobeHistoryViewAdapter: RanobeRecyclerViewAdapter? = null
    private var listener: OnFragmentInteractionListener? = null
    private var mContext: Context? = null
    private var request: Disposable? = null
    private var request2: Disposable? = null
    private var chapterTextList = listOf<ChapterHistory>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_history, container, false)

        mContext = context
        chapterRecyclerView = view.findViewById(R.id.chapterListView)
        chapterRecyclerView.layoutManager = LinearLayoutManager(mContext)


        ranobeRecyclerView = view.findViewById(R.id.ranobeListView)
        ranobeRecyclerView.layoutManager = LinearLayoutManager(mContext)

        request = MyApp.database?.ranobeHistoryDao()?.allChapters()?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
                    chapterHistoryViewAdapter = HistoryRecyclerViewAdapter(mContext!!, it)
                    chapterRecyclerView.adapter = chapterHistoryViewAdapter
                }, { error ->
                    LogHelper.logError(LogHelper.LogType.ERROR, "HistoryFragment", "", error)
                })

        request2 = MyApp.database?.ranobeHistoryDao()?.allRanobes()?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())?.map {
                    val ranobeList = it.map { r -> toRanobe(r) }
                    for (ranobe in ranobeList) {
                        if (ranobe.image.isNullOrBlank()) {
                            MyApp.database?.ranobeImageDao()?.getImageByUrl(ranobe.url)?.observeOn(AndroidSchedulers.mainThread())?.subscribeOn(Schedulers.io())?.subscribe { it2 ->
                                ranobe.image = it2.image
                            }
                        }
                    }
                    return@map ranobeList

                }?.subscribe({

                    ranobeHistoryViewAdapter = RanobeRecyclerViewAdapter(mContext!!, ranobeRecyclerView, it)
                    ranobeRecyclerView.adapter = ranobeHistoryViewAdapter
                }, { error ->
                    LogHelper.logError(LogHelper.LogType.ERROR, "HistoryFragment", "", error)
                })


        tabHost = view.findViewById<TabHost>(R.id.tabHost)

        tabHost.setup()

        val tabSpec: TabHost.TabSpec = tabHost.newTabSpec("Ranobes")
        tabSpec.setContent(R.id.linearLayout)
        tabSpec.setIndicator(resources.getString(R.string.ranobes))
        tabHost.addTab(tabSpec)

        val tabSpec2: TabHost.TabSpec = tabHost.newTabSpec("Chapters")
        tabSpec2.setContent(R.id.linearLayout2)
        tabSpec2.setIndicator(resources.getString(R.string.chapters))
        tabHost.addTab(tabSpec2)


        tabHost.currentTab = 0

        return view

    }

    private fun toRanobe(ranobeHistory: RanobeHistory): Ranobe {

        val ranobe = Ranobe()
        ranobe.url = ranobeHistory.ranobeUrl
        ranobe.title = ranobeHistory.ranobeName
        ranobe.description = ranobeHistory.description
        return ranobe
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        request?.dispose()
        request2?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        request?.dispose()
        request2?.dispose()
    }

    interface OnFragmentInteractionListener

    companion object {

        @JvmStatic
        fun newInstance() = HistoryFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }
}
