package ru.profapp.RanobeReader;

import static ru.profapp.RanobeReader.Common.StringResources.is_readed_Pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Common.ThemeUtils;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

public class RanobeInfoActivity extends AppCompatActivity {

    private final List<Chapter> recycleChapterList = new ArrayList<>();
    private int loadedChapterCount;
    private RecyclerView recyclerView;
    private Drawable downloadDoneImage;
    private SharedPreferences preferences;
    private Ranobe mCurrentRanobe;
    private Context mContext;
    private FloatingActionButton favoriteButton;
    private Drawable borderImage;
    private Drawable fillImage;
    private ChapterRecyclerViewAdapter adapter;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(this, crashlyticsKit);
        ThemeUtils.onActivityCreateSetTheme(this, true);
        setContentView(R.layout.activity_ranobe_info);

        loadedChapterCount = RanobeConstans.chapterCount;

        MobileAds.initialize(this, getString(R.string.app_admob_id));

        AdView adView = findViewById(R.id.adView);
        AdRequest.Builder adRequest = new AdRequest.Builder();

        if (BuildConfig.DEBUG) {
            adRequest.addTestDevice("sdfsdf");
        }

        adView.loadAd(adRequest.build());
        mContext = getApplicationContext();
        Button loadButton = findViewById(R.id.loadButton);

        borderImage = mContext.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp);
        fillImage = mContext.getResources().getDrawable(
                R.drawable.ic_favorite_black_24dp);
        downloadDoneImage = mContext.getResources().getDrawable(R.drawable.ic_cloud_done_black_24dp);

        NestedScrollView nestedScrollView = findViewById(R.id.ranobe_info_NestedScrollView);

        FloatingActionButton fab = findViewById(R.id.fav_toTop_fab);
        fab.setOnClickListener(view -> nestedScrollView.scrollTo(0, 0));

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        favoriteButton = findViewById(R.id.fav_fab);
        TextView aboutTextView = findViewById(R.id.text_about);
        TextView additionalInfoTextView = findViewById(R.id.text_info);
        CardView infoCard = findViewById(R.id.ranobe_info_card);
        CardView descriptionCard = findViewById(R.id.ranobe_description_card);

        mCurrentRanobe = RanobeKeeper.getInstance().getRanobe();
        getSupportActionBar().setTitle(mCurrentRanobe.getTitle());
        initFavoriteButton();
        preferences = mContext.getSharedPreferences(
                StringResources.Rulate_Login_Pref, 0);

        favoriteButton.setOnClickListener(v -> {

            if (!mCurrentRanobe.getFavorited()) {
                favoriteButton.setImageDrawable(fillImage);

                new Thread() {
                    @Override
                    public void run() {
                        boolean wasadded = false;
                        if (mCurrentRanobe.getRanobeSite().equals(StringResources.Rulate_Site)) {

                            String token = preferences.getString(StringResources.KEY_Token, "");
                            if (!token.equals("")) {

                                String response = JsonRulateApi.getInstance().AddBookmark(
                                        mCurrentRanobe.getId(), token);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.getString("status").equals("success")) {
                                        wasadded = true;
                                        mCurrentRanobe.setFavoritedInWeb(true);
                                        runOnUiThread(() -> Toast.makeText(mContext,
                                                mCurrentRanobe.getTitle() + " "
                                                        + mContext.getString(
                                                        R.string.added_to_web),
                                                Toast.LENGTH_SHORT).show());
                                    }
                                } catch (JSONException e) {
                                    MyLog.SendError(StringResources.LogType.WARN,
                                            RanobeInfoActivity.class.toString(), "", e);

                                }

                            }

                        }

                        if (!wasadded) {
                            mCurrentRanobe.setFavorited(true);
                            AsyncTask.execute(
                                    () -> {
                                        DatabaseDao.getInstance(mContext).getRanobeDao().insert(
                                                mCurrentRanobe);
                                        DatabaseDao.getInstance(mContext).getChapterDao().insertAll(
                                                mCurrentRanobe.getChapterList().toArray(
                                                        new Chapter[mCurrentRanobe.getChapterList
                                                                ().size()]));

                                    });

                            runOnUiThread(() -> Toast.makeText(mContext,
                                    mCurrentRanobe.getTitle() + " " + mContext.getString(
                                            R.string.added_to_local), Toast.LENGTH_SHORT).show());

                        }
                    }
                }.run();

            } else {
                favoriteButton.setImageDrawable(borderImage);
                if (mCurrentRanobe.getFavoritedInWeb()) {

                    String token = preferences.getString(StringResources.KEY_Token, "");
                    if (!token.equals("")) {

                        String response = JsonRulateApi.getInstance().RemoveBookmark(
                                mCurrentRanobe.getId(), token);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("success")) {
                                mCurrentRanobe.setFavoritedInWeb(false);
                            }
                        } catch (JSONException e) {
                            MyLog.SendError(StringResources.LogType.WARN,
                                    RanobeInfoActivity.class.toString(), "", e);

                        }
                    }

                } else {
                    AsyncTask.execute(
                            () -> DatabaseDao.getInstance(mContext).getRanobeDao().delete(mCurrentRanobe.getUrl()));
                    mCurrentRanobe.setFavorited(false);
                }

            }

        });

        if (!mCurrentRanobe.getWasUpdated()) {
            try {
                mCurrentRanobe.updateRanobe(mContext);
            } catch (Exception ignored) {

            }

        }

        if (mCurrentRanobe.getDescription() != null) {
            descriptionCard.setVisibility(View.VISIBLE);
        }
        if (mCurrentRanobe.getAdditionalInfo() != null) {
            infoCard.setVisibility(View.VISIBLE);
        }
        ImageView imageView = findViewById(R.id.main_logoimage);
        RequestOptions myOptions = new RequestOptions()
                .placeholder(R.drawable.ic_adb_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .fitCenter();

        Glide.with(mContext).load(mCurrentRanobe.getImage())
                .apply(myOptions)
                .into(imageView);

        aboutTextView.setText(
                String.format("%s\n%s", mCurrentRanobe.getDescription(),
                        mCurrentRanobe.getGenres()));
        additionalInfoTextView.setText(mCurrentRanobe.getAdditionalInfo());

        sPref = mContext.getSharedPreferences(
                is_readed_Pref,
                MODE_PRIVATE);

        int size = mCurrentRanobe.getChapterList().size();

        RecyclerView commentRecycleView = findViewById(R.id.comment_list);
        commentRecycleView.setLayoutManager(new LinearLayoutManager(mContext));

        CommentsRecyclerViewAdapter commentsAdapter = new CommentsRecyclerViewAdapter(
                mCurrentRanobe.getRulateComments(), mContext);
        commentRecycleView.setAdapter(commentsAdapter);

        recyclerView = findViewById(R.id.chapter_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChapterRecyclerViewAdapter(recycleChapterList, this, mCurrentRanobe);
        adapter.setDownloadDoneImage(downloadDoneImage);
        recyclerView.setAdapter(adapter);

        loadChapters(true);
        if (size > loadedChapterCount) {
            loadButton.setVisibility(View.VISIBLE);
            loadButton.setOnClickListener(v -> {
                loadButton.setVisibility(View.GONE);
                loadChapters(false);
                loadButton.setVisibility(View.VISIBLE);
            });
        }

        TabHost tabHost = findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");

        tabSpec.setContent(R.id.linearLayout);
        tabSpec.setIndicator(getResources().getString(R.string.chapters));
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.linearLayout2);
        tabSpec.setIndicator(getResources().getString(R.string.comments));
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);
    }

    private void loadChapters(boolean clean) {

        if (clean) {
            loadedChapterCount = RanobeConstans.chapterCount;
            recycleChapterList.clear();
        }

        int size_prev = recycleChapterList.size();
        loadedChapterCount = Math.min(size_prev + RanobeConstans.chapterCount,
                mCurrentRanobe.getChapterList().size());
        List<Chapter> newList = mCurrentRanobe.getChapterList().subList(size_prev,
                loadedChapterCount);

        if (sPref != null) {
            Object[] allReadedChapters = sPref.getAll().keySet().toArray();

            for (Object readed : allReadedChapters) {
                for (Chapter chapter : newList) {
                    if (chapter.getUrl().equals(readed.toString())) {
                        chapter.setReaded(true);
                        break;
                    }
                }
            }
        }
        recycleChapterList.addAll(newList);

        adapter = new ChapterRecyclerViewAdapter(recycleChapterList, this, mCurrentRanobe);
        adapter.setDownloadDoneImage(downloadDoneImage);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFavoriteButton() {
        AsyncTask.execute(() -> {
            if (mCurrentRanobe.getFavoritedInWeb() || DatabaseDao.getInstance(
                    mContext).getRanobeDao().IsRanobeFavorite(
                    mCurrentRanobe.getUrl())
                    != null) {
                favoriteButton.setImageDrawable(fillImage);
            } else {
                favoriteButton.setImageDrawable(borderImage);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
