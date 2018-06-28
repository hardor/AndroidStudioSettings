package ru.profapp.RanobeReader;

import static android.content.DialogInterface.BUTTON_NEGATIVE;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.TextChapter;

public class DownloadActivity extends AppCompatActivity {
    private volatile boolean running = true;
    private volatile ProgressDialog progressDialog;
    private List<Chapter> mChapterList;
    private RecyclerView recyclerView;
    private DownloadRecyclerViewAdapter adapter;
    private Context mContext;
    private final BottomNavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener
            = item -> {

        switch (item.getItemId()) {

            case R.id.select_none:
                for (Chapter chapter : mChapterList) {
                    chapter.setChecked(false);
                }
                adapter.notifyItemRangeChanged(0, mChapterList.size());
                return true;
            case R.id.download:
                AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.this);
                builder.setMessage(getString(R.string.readyToDownload))
                        .setIcon(R.drawable.ic_info_black_24dp)
                        .setCancelable(true)
                        .setNegativeButton("Cancel",
                                (dialog, id1) -> dialog.cancel()

                        ).setPositiveButton("OK",
                        (dialog, id1) -> {
                            dialog.cancel();
                            running = true;
                            download();
                        }

                );

                AlertDialog alert = builder.create();
                alert.show();

                return true;
            case R.id.select_all:
                for (Chapter chapter : mChapterList) {
                    chapter.setChecked(true);
                }
                adapter.notifyItemRangeChanged(0, mChapterList.size());
                return true;

        }

        return false;
    };

    private void download() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.load_please_wait));
        progressDialog.setTitle(getResources().getString(R.string.load_ranobes));
        progressDialog.setCancelable(false);
        progressDialog.setButton(BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            running = false;
            progressDialog.dismiss();
            updateList();
        });

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(mChapterList.size());
        progressDialog.show();

        ChapterTextActivity chapterText = new ChapterTextActivity();

        new Thread() {

            @Override
            public void run() {
                progressDialog.setProgress(0);
                for (Chapter chapter : mChapterList) {
                    if (running) {
                        if (!chapter.getChecked()) {
                            DatabaseDao.getInstance(
                                    mContext).getTextDao().delete(chapter.getUrl());
                            chapter.setText("");
                            chapter.setDownloaded(false);
                        } else {
                            chapter.setDownloaded(chapterText.GetChapterText(chapter, mContext));
                        }
                        progressDialog.incrementProgressBy(1);
                    } else {
                        break;
                    }
                }

                runOnUiThread(()-> {
                    progressDialog.getButton(BUTTON_NEGATIVE).setText(R.string.finish);
                    progressDialog.setMessage(getString(R.string.task_finished));
                        }
                );

            }
        }.start();
    }

    private void updateList() {

        for (Chapter chapter : mChapterList) {
            chapter.setChecked(chapter.getDownloaded());
        }
        adapter.notifyItemRangeChanged(0, mChapterList.size());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setupActionBar();
        setContentView(R.layout.activity_download);
        mContext = getApplicationContext();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        recyclerView = findViewById(R.id.chapter_list);

        mChapterList = RanobeKeeper.getInstance().getRanobe().getChapterList();
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setTitle("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        new Thread() {
            @Override
            public void run() {
                for (Chapter chapter : mChapterList) {
                    if(!chapter.getDownloaded()) {
                        TextChapter textChapter = DatabaseDao.getInstance(
                                mContext).getTextDao().getTextByChapterUrl(chapter.getUrl());
                        if (textChapter != null && textChapter.getText() != null
                                && !textChapter.getText().equals("")) {
                            chapter.setDownloaded(true);
                            chapter.setChecked(true);
                        } else {
                            chapter.setDownloaded(false);
                            chapter.setChecked(false);
                        }
                    }else{
                        chapter.setChecked(true);
                    }
                }
                adapter = new DownloadRecyclerViewAdapter(mChapterList, DownloadActivity.this);
                runOnUiThread(() -> {
                            recyclerView.setAdapter(adapter);
                            pDialog.dismiss();
                        }
                );

            }
        }.start();




    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}
