package ru.profapp.RanobeReader.Fragments

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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.Adapters.HistoryRecyclerViewAdapter
import ru.profapp.RanobeReader.Adapters.RanobeRecyclerViewAdapter
import ru.profapp.RanobeReader.Helpers.LogHelper
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.Models.RanobeHistory
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R

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
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_history, container, false)

        chapterRecyclerView = view.findViewById(R.id.rV_history_chapters)
        ranobeRecyclerView = view.findViewById(R.id.rV_history_ranobe)
        tabHost = view.findViewById(R.id.tH_history)

        chapterRecyclerView.layoutManager = LinearLayoutManager(mContext)


        ranobeRecyclerView.layoutManager = LinearLayoutManager(mContext)

        val chapterRequest = MyApp.database.ranobeHistoryDao().allChapters().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    chapterHistoryViewAdapter = HistoryRecyclerViewAdapter(mContext!!, it)
                    chapterRecyclerView.adapter = chapterHistoryViewAdapter
                }, { error ->
                    LogHelper.logError(LogHelper.LogType.ERROR, "HistoryFragment", "", error)
                })

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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    ranobeHistoryViewAdapter = RanobeRecyclerViewAdapter(mContext!!, ranobeRecyclerView, it)
                    ranobeRecyclerView.adapter = ranobeHistoryViewAdapter
                }, { error ->
                    LogHelper.logError(LogHelper.LogType.ERROR, "HistoryFragment", "", error)
                })
        compositeDisposable.add(chapterRequest)
        compositeDisposable.add(ranobeRequest)



        tabHost.setup()

        val tabSpec: TabHost.TabSpec = tabHost.newTabSpec("Ranobes")
        tabSpec.setContent(R.id.rV_history_ranobe)
        tabSpec.setIndicator(resources.getString(R.string.ranobes))
        tabHost.addTab(tabSpec)

        val tabSpec2: TabHost.TabSpec = tabHost.newTabSpec("Chapters")
        tabSpec2.setContent(R.id.rV_history_chapters)
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
        mContext = context
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        mContext = null
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        mContext = null
        MyApp.refWatcher?.watch(this)
    }

    interface OnFragmentInteractionListener

    companion object {

        fun newInstance() = HistoryFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }
}
