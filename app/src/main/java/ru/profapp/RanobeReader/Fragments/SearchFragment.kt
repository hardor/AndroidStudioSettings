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
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.Network.Repositories.RanobeHubRepository
import ru.profapp.RanobeReader.Network.Repositories.RanobeRfRepository
import ru.profapp.RanobeReader.Network.Repositories.RulateRepository
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
    private var ranobeRecyclerViewAdapter: RanobeRecyclerViewAdapter? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var adapterRanobeList: MutableList<Ranobe> = mutableListOf()
    private var mContext: Context? = null
    lateinit var resultLabel: TextView
    private lateinit var progressBar: ProgressBar
    private var searhRequest: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
        adapterRanobeList = ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val simpleSearchView = view.findViewById<SearchView>(R.id.sV_search)
        progressBar = view.findViewById(R.id.progressBar_search)
        resultLabel = view.findViewById(R.id.tV_search_resultLabel)

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

        recyclerView = view.findViewById(R.id.rV_search_ranobe)

        recyclerView.layoutManager = LinearLayoutManager(mContext)

        ranobeRecyclerViewAdapter = RanobeRecyclerViewAdapter(mContext!!, recyclerView, adapterRanobeList)
        recyclerView.adapter = ranobeRecyclerViewAdapter
        return view
    }

    override fun onAttach(context: Context?) {

        super.onAttach(context)
        mContext = context
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        mContext = null
    }

    private fun findRanobe(searchString: String) {

        progressBar.visibility = VISIBLE

        val size = adapterRanobeList.size
        adapterRanobeList.clear()
        ranobeRecyclerViewAdapter!!.notifyItemRangeRemoved(0, size)

        searhRequest = Single.zip(findRulateRanobe(searchString), findRanobeRfRanobe(searchString), findRanobeHubRanobe(searchString),
                io.reactivex.functions.Function3<List<Ranobe>, List<Ranobe>, List<Ranobe>, List<Ranobe>>
                { Rulate, RanobeRfW, RanobeHub ->
                    val newList = mutableListOf<Ranobe>()
                    newList.addAll(Rulate)
                    newList.addAll(RanobeRfW)
                    newList.addAll(RanobeHub)
                    return@Function3 newList
                }
        ).map { ranobeList ->
            for (ranobe in ranobeList) {
                if (ranobe.image.isNullOrBlank()) {
                    MyApp.database.ranobeImageDao().getImageByUrl(ranobe.url).observeOn(AndroidSchedulers.mainThread())?.subscribeOn(Schedulers.io())?.subscribe { it ->
                        ranobe.image = it.image
                    }
                }
            }
            return@map ranobeList

        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

                .subscribe({ result ->
                    val prevSize = adapterRanobeList.size
                    adapterRanobeList.addAll(result)
                    ranobeRecyclerViewAdapter!!.notifyItemRangeInserted(prevSize, adapterRanobeList.size)
                    if (adapterRanobeList.size == 0) {
                        resultLabel.visibility = View.VISIBLE
                    }

                    recyclerView.scrollToPosition(0)

                    progressBar.visibility = GONE
                }, { error ->
                    Toast.makeText(mContext, getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
                    progressBar.visibility = GONE
                })

    }

    private fun findRulateRanobe(searchString: String): Single<List<Ranobe>> {

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

    private fun findRanobeHubRanobe(searchString: String): Single<List<Ranobe>> {

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

    private fun findRanobeRfRanobe(searchString: String): Single<List<Ranobe>> {

        return RanobeRfRepository.searchBooks(searchString).map {
            val or: ArrayList<Ranobe> = ArrayList()
            if (it.isNotEmpty()) {

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
        fun newInstance() = SearchFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searhRequest?.dispose()
        mContext = null
        MyApp.refWatcher?.watch(this)
    }
}

