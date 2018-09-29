package ru.profapp.RanobeReader.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.Adapters.HistoryRecyclerViewAdapter
import ru.profapp.RanobeReader.Helpers.MyLog
import ru.profapp.RanobeReader.Models.ChapterHistory
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
    private lateinit var recyclerView: RecyclerView
    private var chapterHistoryViewAdapter: HistoryRecyclerViewAdapter? = null
    private var listener: OnFragmentInteractionListener? = null
    private var mContext: Context? = null
    private var request: Disposable? = null
    private var chapterTextList = listOf<ChapterHistory>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_history, container, false)

        mContext = context
        recyclerView = view.findViewById(R.id.ranobeListView)

        recyclerView.layoutManager = LinearLayoutManager(mContext)

        MyApp.database?.chapterHistoryDao()?.allChapters()?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
                    chapterHistoryViewAdapter = HistoryRecyclerViewAdapter(it)
                    recyclerView.adapter = chapterHistoryViewAdapter
                }, { error ->
                    MyLog.SendError(MyLog.LogType.ERROR, "HistoryFragment", "", error)
                })

        return view


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


    interface OnFragmentInteractionListener

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
                HistoryFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
