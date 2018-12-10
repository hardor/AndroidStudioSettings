package ru.profapp.ranobe.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.adapters.HistoryFragmentPagerAdapter

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HistoryFragment : Fragment() {

    private var mContext: Context? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val pager: ViewPager = view.findViewById(R.id.pager)
        val pagerAdapter = HistoryFragmentPagerAdapter(childFragmentManager, mContext!!.applicationContext)
        pager.adapter = pagerAdapter

        val btnCleanData: Button = view.findViewById(R.id.btN_CleanData)

        btnCleanData.setOnClickListener {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle(getString(R.string.clear_history))
                    .setMessage(getString(R.string.clear_history_summary))
                    .setIcon(R.drawable.ic_info_black_24dp)
                    .setCancelable(true)
                    .setPositiveButton("OK") { _, _ ->
                        Completable.fromAction { MyApp.database.ranobeHistoryDao().cleanHistory() }
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.subscribeOn(Schedulers.io())
                                ?.subscribe({
                                    Toast.makeText(context, resources.getText(R.string.history_removed), Toast.LENGTH_SHORT).show()
                                    pagerAdapter.notifyDataSetChanged()
                                }, {
                                    Toast.makeText(context, resources.getText(R.string.error), Toast.LENGTH_SHORT).show()
                                })
                        // dialog.cancel()
                    }.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }

            builder.create().show()

        }
        return view

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context

    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mContext = null
        MyApp.refWatcher?.watch(this)
    }

    companion object {
        fun newInstance() = HistoryFragment()
    }
}


