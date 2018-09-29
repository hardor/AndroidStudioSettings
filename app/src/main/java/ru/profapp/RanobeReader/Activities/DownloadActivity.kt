package ru.profapp.RanobeReader.Activities

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
import ru.profapp.RanobeReader.Adapters.DownloadRecyclerViewAdapter
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R

class DownloadActivity : AppCompatActivity() {
    @Volatile
    private var running = true
    @Volatile
    private var progressDialog: ProgressDialog? = null
    private var chapterList: List<Chapter> = arrayListOf()
    private var recyclerView: RecyclerView? = null
    private var adapter: DownloadRecyclerViewAdapter? = null
    private var context: Context? = null
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {

            R.id.select_none -> {
                for (chapter in chapterList) {
                    chapter.isChecked = false
                }
                adapter!!.notifyItemRangeChanged(0, chapterList.size)
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
                for (chapter in chapterList) {
                    chapter.isChecked = true
                }
                adapter!!.notifyItemRangeChanged(0, chapterList.size)
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

        object : Thread() {

            override fun run() {
                progressDialog!!.progress = 0
                for (chapter in chapterList) {
                    if (running) {
                        if (!chapter.isChecked) {

                            MyApp.database?.textDao()?.delete(chapter.url)
                            //
                            //                                MyApp.database?.personDao()?.insert(person)
                            //                            ?.subscribeOn(Schedulers.io())
                            //                                    .subscribe()
                            //                            MyApp.database?.textDao()?.delete(chapter.getUrl());
                            chapter.text = ""
                            chapter.downloaded = false
                        } else {
                            chapter.downloaded = chapterText.GetChapterText(chapter, context!!)
                        }
                        progressDialog!!.incrementProgressBy(1)
                    } else {
                        break
                    }
                }

                runOnUiThread {
                    progressDialog!!.getButton(BUTTON_NEGATIVE).setText(R.string.finish)
                    progressDialog!!.setMessage(getString(R.string.task_finished))
                }

            }
        }.start()
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

        chapterList = MyApp.ranobe!!.chapterList
        val pDialog = ProgressDialog(this)
        pDialog.setTitle("Loading")
        pDialog.setCancelable(false)
        pDialog.show()
        object : Thread() {
            override fun run() {
                for (chapter in chapterList) {
                    if (!chapter.downloaded) {

                        val textChapter = MyApp.database?.textDao()?.getTextByChapterUrl(chapter.url)
                        //Todo
//                        if (textChapter?.text != null
//                                && textChapter.text != "") {
//                            chapter.downloaded = true
//                            chapter.isChecked = true
//                        } else {
//                            chapter.downloaded = false
//                            chapter.isChecked = false
//                        }
                    } else {
                        chapter.isChecked = true
                    }
                }
                adapter = DownloadRecyclerViewAdapter(chapterList)
                runOnUiThread {
                    recyclerView!!.adapter = adapter
                    pDialog.dismiss()
                }

            }
        }.start()


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
}
