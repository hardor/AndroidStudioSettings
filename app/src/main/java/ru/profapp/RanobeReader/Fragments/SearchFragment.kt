package ru.profapp.RanobeReader.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.Adapters.RanobeRecyclerViewAdapter
import ru.profapp.RanobeReader.Common.Constants.RanobeSite.Title
import ru.profapp.RanobeReader.JsonApi.RanobeHubRepository
import ru.profapp.RanobeReader.JsonApi.RanobeRfRepository
import ru.profapp.RanobeReader.JsonApi.RulateRepository
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private var mRanobeRecyclerViewAdapter: RanobeRecyclerViewAdapter? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var mRanobeList: MutableList<Ranobe> = ArrayList()
    private var mContext: Context? = null
    lateinit var resultLabel: TextView
    lateinit var progressBar: ProgressBar
    private var request: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
        mRanobeList = ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        mContext = context

        val simpleSearchView = view.findViewById<SearchView>(R.id.search)
        progressBar = view.findViewById(R.id.progressBar)
        resultLabel = view.findViewById(R.id.search_result_label)

        simpleSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                resultLabel.visibility = View.GONE
                findRanobe(query)

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        recyclerView = view.findViewById(R.id.ranobeListView)

        recyclerView.layoutManager = LinearLayoutManager(mContext)

        mRanobeRecyclerViewAdapter = RanobeRecyclerViewAdapter(mContext!!, recyclerView, mRanobeList)
        recyclerView.adapter = mRanobeRecyclerViewAdapter
        return view
    }

    override fun onAttach(context: Context?) {

        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun findRanobe(searchString: String) {

        progressBar.visibility = VISIBLE

        val size = mRanobeList.size
        mRanobeList.clear()
        mRanobeRecyclerViewAdapter!!.notifyItemRangeRemoved(0, size)

        request = Single.mergeDelayError(findRulateRanobe(searchString), findRanobeRfRanobe(searchString), findRanobeHubRanobe(searchString)).map { ranobeList ->
            for (ranobe in ranobeList) {
                if (ranobe.image.isNullOrBlank()) {
                    MyApp.database?.ranobeImageDao()?.getImageByUrl(ranobe.url)?.observeOn(AndroidSchedulers.mainThread())?.subscribeOn(Schedulers.io())?.subscribe { it ->
                        ranobe.image = it.image
                    }
                }
            }
            return@map ranobeList

        }.observeOn(AndroidSchedulers.mainThread(), true).subscribeOn(Schedulers.io())

                .subscribe({ result ->
                    val prevSize = mRanobeList.size
                    mRanobeList.addAll(result)
                    mRanobeRecyclerViewAdapter!!.notifyItemRangeInserted(prevSize, mRanobeList.size)
                    if (mRanobeList.size == 0) {
                        resultLabel.visibility = View.VISIBLE
                    }

                    recyclerView.scrollToPosition(0)

                    progressBar.visibility = GONE
                }, { error ->
                    Toast.makeText(mContext, getString(R.string.ErrorConnection), Toast.LENGTH_SHORT).show()
                    progressBar.visibility = GONE
                })

    }

    private fun findRulateRanobe(searchString: String): Single<ArrayList<Ranobe>> {

        return RulateRepository.searchBooks(searchString).map {
            val or: ArrayList<Ranobe> = ArrayList()
            if (it.isNotEmpty()) {

                val ranobet: Ranobe = Ranobe()
                ranobet.ranobeSite = Title.url
                ranobet.title = this.getString(R.string.tl_rulate_name)
                or.add(ranobet)
                or.addAll(it)
            }
            return@map or
        }

    }

    private fun findRanobeHubRanobe(searchString: String): Single<ArrayList<Ranobe>> {

        return RanobeHubRepository.searchBooks(searchString).map {
            val or: ArrayList<Ranobe> = ArrayList()
            if (it.isNotEmpty()) {

                val ranobet = Ranobe()
                ranobet.ranobeSite = Title.url
                ranobet.title = getString(R.string.ranobe_hub)
                or.add(ranobet)
                or.addAll(it)
            }
            return@map or
        }

    }

    private fun findRanobeRfRanobe(searchString: String): Single<ArrayList<Ranobe>> {

        return RanobeRfRepository.searchBooks(searchString).map {
            val or: ArrayList<Ranobe> = ArrayList()
            if (it.size > 0) {

                val ranobet = Ranobe()
                ranobet.ranobeSite = Title.url
                ranobet.title = getString(R.string.ranobe_rf)
                or.add(ranobet)
                or.addAll(it)
            }
            return@map or
        }

    }

    interface OnFragmentInteractionListener

    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }

    override fun onDestroy() {
        request?.dispose()
        super.onDestroy()
    }
}

