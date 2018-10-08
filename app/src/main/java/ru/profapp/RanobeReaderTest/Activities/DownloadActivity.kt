package ru.profapp.RanobeReaderTest.Activities

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.fabric.sdk.android.Fabric
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReaderTest.Adapters.ExpandableDownloadRecyclerViewAdapter
import ru.profapp.RanobeReaderTest.Models.Chapter
import ru.profapp.RanobeReaderTest.Models.Ranobe
import ru.profapp.RanobeReaderTest.MyApp
import ru.profapp.RanobeReaderTest.R

class DownloadActivity : AppCompatActivity() {
    @Volatile
    private var running = true
    @Volatile
    private var progressDialog: ProgressDialog? = null
    private var chapterList: List<Chapter> = listOf()
    private var recyclerView: RecyclerView? = null
    private lateinit var adapter: ExpandableDownloadRecyclerViewAdapter
    private var context: Context? = null

    lateinit var currentRanobe: Ranobe

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->


        when (item.itemId) {

            R.id.select_none -> {
                chapterList.forEach { it.isChecked = false }
                adapter.selectAll = false
                adapter.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
            R.id.download -> {
                val builder = AlertDialog.Builder(this@DownloadActivity)
                builder.setMessage(getString(R.string.readyToDownload))
                        .setIcon(R.drawable.ic_info_black_24dp)
                        .setCancelable(true)
                        .setNegativeButton("Cancel"

                        ) { dialog, id1 -> dialog.cancel() }.setPositiveButton("OK"

                        ) { dialog, id1 ->
                            dialog.cancel()
                            running = true
                            download()
                        }

                val alert = builder.create()
                alert.show()

                return@OnNavigationItemSelectedListener true
            }
            R.id.select_all -> {
                chapterList.forEach { it.isChecked = true }
                adapter.selectAll = true
                adapter!!.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    private fun download() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage(resources.getString(R.string.load_please_wait))
        progressDialog!!.setTitle(resources.getString(R.string.load_ranobes))
        progressDialog!!.setCancelable(false)
        progressDialog!!.setButton(BUTTON_NEGATIVE, "Cancel") { dialog, which ->
            running = false
            progressDialog!!.dismiss()
            updateList()
        }

        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog!!.progress = 0
        progressDialog!!.max = chapterList.size
        progressDialog!!.show()

        val chapterText = ChapterTextActivity()


        progressDialog!!.progress = 0
        for (chapter in chapterList) {
            if (running) {
                if (!chapter.isChecked) {


                    Completable.fromAction {
                        MyApp.database?.textDao()?.delete(chapter.url)
                    }?.doFinally {
                        chapter.text = ""
                        chapter.downloaded = false
                    }?.subscribeOn(Schedulers.io())?.blockingAwait()


                } else {
                    chapter.downloaded = chapterText.GetChapterText(chapter, context!!).subscribeOn(Schedulers.io()).blockingGet()
                }
                progressDialog!!.incrementProgressBy(1)
            } else {
                break
            }
        }


        progressDialog!!.getButton(BUTTON_NEGATIVE).setText(R.string.finish)
        progressDialog!!.setMessage(getString(R.string.task_finished))


    }

    private fun updateList() {

        for (chapter in chapterList) {
            chapter.isChecked = chapter.downloaded
        }
        adapter!!.notifyItemRangeChanged(0, chapterList.size)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setupActionBar()
        setContentView(R.layout.activity_download)
        context = applicationContext
        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        recyclerView = findViewById(R.id.chapter_list)

        currentRanobe = MyApp.ranobe!!
        chapterList = currentRanobe.chapterList
        val pDialog = ProgressDialog(this)
        pDialog.setTitle("Loading")
        pDialog.setCancelable(false)
        pDialog.show()

        MyApp.database?.textDao()?.getTextByRanobeUrl(currentRanobe.url)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ result ->

                    for (chapter in chapterList) {
                        if (!chapter.downloaded) {

                            if (!result.firstOrNull { it -> it.chapterUrl == chapter.url }?.text.isNullOrBlank()) {
                                chapter.downloaded = true
                                chapter.isChecked = true
                            } else {
                                chapter.downloaded = false
                                chapter.isChecked = false
                            }

                        } else {
                            chapter.isChecked = true
                        }
                    }

                    adapter = ExpandableDownloadRecyclerViewAdapter(this@DownloadActivity, chapterList)
                    recyclerView!!.adapter = adapter
                    pDialog.dismiss()
                }, {
                    pDialog.dismiss()
                })


    }

    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog!!.dismiss()
    }
}