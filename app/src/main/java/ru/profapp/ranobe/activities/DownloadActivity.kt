package ru.profapp.ranobe.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.fabric.sdk.android.Fabric
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.profapp.ranobe.adapters.ExpandableDownloadRecyclerViewAdapter
import ru.profapp.ranobe.common.MyExceptionHandler
import ru.profapp.ranobe.helpers.LogType
import ru.profapp.ranobe.helpers.logError

import ru.profapp.ranobe.models.Chapter
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R

class DownloadActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private var chapterList: List<Chapter> = listOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpandableDownloadRecyclerViewAdapter
    private lateinit var context: Context

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private lateinit var currentRanobe: Ranobe

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
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.cancel()
                            download()
                        }

                val alert = builder.create()
                alert.show()

                return@OnNavigationItemSelectedListener true
            }
            R.id.select_all -> {
                chapterList.forEach { it.isChecked = true }
                adapter.selectAll = true

                adapter.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    private fun download() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(resources.getString(R.string.download_chapters_please_wait))
        progressDialog.setTitle(resources.getString(R.string.download_chapters))
        progressDialog.setCancelable(false)
        progressDialog.setButton(BUTTON_NEGATIVE, "Cancel") { _, _ ->
            compositeDisposable.dispose()
            progressDialog.dismiss()
            updateList()
        }

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.progress = 0
        progressDialog.max = chapterList.size
        progressDialog.show()

        val chapterText = ChapterTextActivity()


        progressDialog.progress = 0

        val running = Observable.fromIterable(chapterList.asReversed())
                .map { chapter ->
                    if (!chapter.isChecked) {

                        MyApp.database.textDao().delete(chapter.url)
                        chapter.text = ""
                        chapter.downloaded = false

                    } else {
                        chapter.downloaded = chapterText.GetChapterText(chapter, context).onErrorReturn { error ->
                            logError(LogType.ERROR, "download", "", error, false)
                            return@onErrorReturn false
                        }.subscribeOn(Schedulers.io()).blockingGet()
                    }
                    return@map true
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    progressDialog.incrementProgressBy(1)
                    return@map it
                }
                .doOnComplete { progressDialog.setMessage(getString(R.string.task_finished)) }
                .doOnError { progressDialog.setMessage(getString(R.string.error)) }
                .doFinally {
                    progressDialog.getButton(BUTTON_NEGATIVE).setText(R.string.finish)
                }
                .subscribe({}, { error ->
                    logError(LogType.ERROR, "download", "", error)
                })
        compositeDisposable.add(running)

    }

    private fun updateList() {

        for (chapter in chapterList) {
            chapter.isChecked = chapter.downloaded
        }

        adapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!MyApp.isApplicationInitialized || MyApp.ranobe == null) {
            val firstIntent = Intent(this, MainActivity::class.java)

            firstIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // So all other activities will be dumped
            startActivity(firstIntent)

            // We are done, so finish this activity and get out now
            finish()
            return
        }
        Fabric.with(this, Crashlytics())
        setupActionBar()
        setContentView(R.layout.activity_download)
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))
        context = this@DownloadActivity
        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        recyclerView = findViewById(R.id.chapter_list)
        progressBar = findViewById(R.id.progressBar_download)

        currentRanobe = MyApp.ranobe!!
        chapterList = currentRanobe.chapterList



        progressBar.visibility = View.VISIBLE

        val request = MyApp.database.textDao().getTextByRanobeUrl(currentRanobe.url)
                .map { result ->
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

                    adapter = ExpandableDownloadRecyclerViewAdapter(chapterList)
                    return@map true
                }.onErrorReturn { false }

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    progressBar.visibility = View.GONE
                }
                .subscribe({
                    if (it)
                        recyclerView.adapter = adapter
                }, {

                })
        compositeDisposable.add(request)
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

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        Thread.setDefaultUncaughtExceptionHandler(null)
        compositeDisposable.dispose()
    }
}
