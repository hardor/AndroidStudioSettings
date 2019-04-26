package ru.profapp.ranobe.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val pager: ViewPager = view.findViewById(R.id.pager)
        val pagerAdapter = HistoryFragmentPagerAdapter(childFragmentManager,
            mContext!!.applicationContext)
        pager.adapter = pagerAdapter

        val btnCleanData: MaterialButton = view.findViewById(R.id.btN_CleanData)

        btnCleanData.setOnClickListener {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle(getString(R.string.clear_history))
                .setMessage(getString(R.string.clear_history_summary))
                .setIcon(R.drawable.ic_info_black_24dp).setCancelable(true)
                .setPositiveButton("OK") { _, _ ->
                    GlobalScope.launch(Dispatchers.IO) {

                      val result = try{
                            MyApp.database.ranobeHistoryDao().cleanHistory()
                            resources.getString(R.string.history_removed)
                        } catch (ex:Exception){
                            resources.getString(R.string.error)
                        }

                        withContext(Dispatchers.Main){
                            Toast.makeText(context,
                                result,
                                Toast.LENGTH_SHORT).show()
                        }
                    }

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

        mContext = null
        super.onDetach()
    }

    override fun onDestroy() {
        mContext = null
        MyApp.refWatcher?.watch(this)
        super.onDestroy()
    }

    companion object {

        private val TAG = "History Fragment"

        fun newInstance() = HistoryFragment()
    }
}


