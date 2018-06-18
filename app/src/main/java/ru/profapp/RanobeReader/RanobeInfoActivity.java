package ru.profapp.RanobeReader;

import static ru.profapp.RanobeReader.Common.StringResources.Chapter_Url;
import static ru.profapp.RanobeReader.Common.StringResources.CleanString;
import static ru.profapp.RanobeReader.Common.StringResources.is_readed_Pref;
import static ru.profapp.RanobeReader.Models.Ranobe.empty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.Objects;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

public class RanobeInfoActivity extends AppCompatActivity {

    private List<Chapter> recycleChapterList = new ArrayList<>();
    private int loadedChapterCount;
    private RecyclerView recyclerView;
    private SharedPreferences preferences;
    private SharedPreferences rfpreferences;
    private Ranobe mCurrentRanobe;
    private Context mContext;
    private Drawable borderImage;
    private Drawable fillImage;
    private ChapterRecyclerViewAdapter adapter;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(this, crashlyticsKit);
        setContentView(R.layout.activity_ranobe_info);
        if (!BuildConfig.PAID_VERSION) {
            MobileAds.initialize(this, getString(R.string.app_admob_id));
            AdView adView = findViewById(R.id.adView);
            AdRequest.Builder adRequest = new AdRequest.Builder();

            if (BuildConfig.DEBUG) {
                adRequest.addTestDevice("sdfsdf");
            }

            adView.loadAd(adRequest.build());

        }

        mContext = RanobeInfoActivity.this;
        Button loadButton = findViewById(R.id.loadButton);
        ImageButton sortButton = findViewById(R.id.sortButton);
        sortButton.setOnClickListener(v -> {
            mCurrentRanobe.ReversChapters();
            loadChapters(true);

            if (mCurrentRanobe.getChapterList().size() > loadedChapterCount) {
                loadButton.setVisibility(View.VISIBLE);
            }

        });
        borderImage = mContext.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp);
        fillImage = mContext.getResources().getDrawable(
                R.drawable.ic_favorite_black_24dp);

        NestedScrollView nestedScrollView = findViewById(R.id.ranobe_info_NestedScrollView);

        FloatingActionButton fab = findViewById(R.id.fav_toTop_fab);
        fab.setOnClickListener(view -> nestedScrollView.scrollTo(0, 0));

        FloatingActionButton bookmarkFab = findViewById(R.id.bookmark_fab);
        bookmarkFab.setOnClickListener(view -> {

            SharedPreferences sChapterPref = mContext.getSharedPreferences(
                    CleanString(mCurrentRanobe.getUrl()), MODE_PRIVATE);

            String url = sChapterPref.getString(Chapter_Url, null);
            Intent intent = new Intent(mContext, ChapterTextActivity.class);
            int tempIndex = mCurrentRanobe.getChapterList().size() - 1;
            if (url != null) {
                List<Chapter> tempList = mCurrentRanobe.getChapterList();
                for (int i = 0; i < tempList.size(); i++) {
                    Chapter chapter = tempList.get(i);
                    if (chapter.getUrl().equals(url)) {
                        tempIndex = i;
                        break;
                    }

                }
            }
            intent.putExtra("ChapterIndex", tempIndex);
            intent.putExtra("Bookmark", true);

            mContext.startActivity(intent);

        });

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        TextView aboutTextView = findViewById(R.id.text_about);
        TextView additionalInfoTextView = findViewById(R.id.text_info);
        CardView infoCard = findViewById(R.id.ranobe_info_card);
        CardView descriptionCard = findViewById(R.id.ranobe_description_card);

        mCurrentRanobe = RanobeKeeper.getInstance().getRanobe();
        try {
            getSupportActionBar().setTitle(mCurrentRanobe.getTitle());
        } catch (Exception ignore) {
        }
        preferences = mContext.getSharedPreferences(
                StringResources.Rulate_Login_Pref, 0);

        rfpreferences = mContext.getSharedPreferences(
                StringResources.Ranoberf_Login_Pref, 0);

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

        String aboutText = String.format("%s / %s \n\n%s", mCurrentRanobe.getTitle(),
                mCurrentRanobe.getEngTitle(), mCurrentRanobe.getDescription());

        if (!empty(mCurrentRanobe.getGenres())) {
            aboutText = aboutText + "\n\n" + mCurrentRanobe.getGenres();
        }

        aboutTextView.setText(aboutText);
        additionalInfoTextView.setText(mCurrentRanobe.getAdditionalInfo());

        sPref = mContext.getSharedPreferences(
                is_readed_Pref,
                MODE_PRIVATE);

        int size = mCurrentRanobe.getChapterList().size();

        recyclerView = findViewById(R.id.chapter_list);

        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                recyclerView.dispatchNestedFling(velocityX, velocityY, false);
                return false;
            }
        });

        adapter = new ChapterRecyclerViewAdapter(recycleChapterList, this, mCurrentRanobe);

        recyclerView.setAdapter(adapter);

        loadChapters(true);
        if (size > loadedChapterCount) {
            loadButton.setVisibility(View.VISIBLE);
            loadButton.setOnClickListener(v -> {
                loadButton.setVisibility(View.GONE);
                loadChapters(false);
            });
        }

        TabHost tabHost = findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");

        tabSpec.setContent(R.id.linearLayout);
        tabSpec.setIndicator(getResources().getString(R.string.chapters));
        tabHost.addTab(tabSpec);

        if (mCurrentRanobe.getRulateComments().size() > 0) {
            RecyclerView commentRecycleView = findViewById(R.id.comment_list);
            commentRecycleView.setLayoutManager(new LinearLayoutManager(mContext));

            commentRecycleView.setOnFlingListener(
                    new RecyclerView.OnFlingListener() {
                        @Override
                        @RequiresApi(Build.VERSION_CODES.KITKAT)
                        public boolean onFling(int velocityX, int velocityY) {
                            commentRecycleView.dispatchNestedFling(velocityX, velocityY, false);
                            return false;
                        }
                    });

            CommentsRecyclerViewAdapter commentsAdapter = new CommentsRecyclerViewAdapter(
                    mCurrentRanobe.getRulateComments(), mContext);
            commentRecycleView.setAdapter(commentsAdapter);

            tabSpec = tabHost.newTabSpec("tag2");
            tabSpec.setContent(R.id.linearLayout2);
            tabSpec.setIndicator(getResources().getString(R.string.comments));
            tabHost.addTab(tabSpec);
        }
        tabHost.setCurrentTab(0);
    }

    private void loadChapters(boolean clean) {

        int size_prev = recycleChapterList.size();
        if (clean) {
            RanobeKeeper.getInstance();
            loadedChapterCount = RanobeKeeper.chapterCount;
            recycleChapterList.clear();
            adapter.notifyItemRangeRemoved(0, size_prev);
            size_prev = 0;
        } else {
            loadedChapterCount = mCurrentRanobe.getChapterList().size();
        }

        List<Chapter> newList = mCurrentRanobe.getChapterList().subList(size_prev,
                Math.min(loadedChapterCount, mCurrentRanobe.getChapterList().size()));
        if (sPref != null) {
            for (Chapter chapter : newList) {
                if (!chapter.getReaded()) {
                    chapter.setReaded(sPref.getBoolean(chapter.getUrl(), false));
                }
            }
        }

        recycleChapterList.addAll(newList);
        adapter.notifyItemRangeInserted(size_prev, newList.size());

    }

    private void getFavoriteIcon(MenuItem item) {
        AsyncTask.execute(() -> {
            if (mCurrentRanobe.getFavorited() || mCurrentRanobe.getFavoritedInWeb()
                    || DatabaseDao.getInstance(
                    mContext).getRanobeDao().IsRanobeFavorite(
                    mCurrentRanobe.getUrl())
                    != null) {
                runOnUiThread(() -> item.setIcon(fillImage));
            } else {
                runOnUiThread(() -> item.setIcon(borderImage));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action, menu);

        if (menu != null) {
            MenuItem item = menu.findItem(R.id.fav_button);
            if (item != null) {
                getFavoriteIcon(item);
            }
        }

        return true;
    }

    private void SetToFavorite(MenuItem item) {

        if (!mCurrentRanobe.getFavorited() && !mCurrentRanobe.getFavoritedInWeb()) {
            item.setIcon(fillImage);

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

                    } else if (mCurrentRanobe.getRanobeSite().equals(
                            StringResources.RanobeRf_Site)) {

                        String token = rfpreferences.getString(StringResources.KEY_Token, "");
                        if (!token.equals("")) {

                            String response = JsonRanobeRfApi.getInstance().AddBookmark(
                                    mCurrentRanobe.getId(),
                                    mCurrentRanobe.getChapterList().get(0).getId(), token);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getInt("status") == 200) {
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

            if (mCurrentRanobe.getFavoritedInWeb()) {

                if (mCurrentRanobe.getRanobeSite().contains(StringResources.Rulate_Site)) {
                    String token = preferences.getString(StringResources.KEY_Token, "");
                    if (!token.equals("")) {

                        String response = JsonRulateApi.getInstance().RemoveBookmark(
                                mCurrentRanobe.getId(), token);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("success")) {
                                mCurrentRanobe.setFavoritedInWeb(false);
                                AsyncTask.execute(
                                        () -> DatabaseDao.getInstance(
                                                mContext).getRanobeDao().deleteWeb(
                                                mCurrentRanobe.getUrl()));
                                item.setIcon(borderImage);
                            }
                        } catch (JSONException e) {
                            MyLog.SendError(StringResources.LogType.WARN,
                                    RanobeInfoActivity.class.toString(), "", e);

                        }
                    }

                } else if (mCurrentRanobe.getRanobeSite().contains(
                        StringResources.RanobeRf_Site)) {
                    String token = rfpreferences.getString(StringResources.KEY_Token, "");
                    if (!token.equals("")) {

                        Thread thread = new Thread(() -> {

                            String response = JsonRanobeRfApi.getInstance().RemoveBookmark(
                                    mCurrentRanobe.getBookmarkIdRf(), token);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getInt("status") == 200) {
                                    mCurrentRanobe.setFavoritedInWeb(false);
                                    AsyncTask.execute(
                                            () -> DatabaseDao.getInstance(
                                                    mContext).getRanobeDao().deleteWeb(
                                                    mCurrentRanobe.getUrl()));
                                    runOnUiThread(
                                            () -> item.setIcon(
                                                    borderImage));

                                }
                            } catch (JSONException e) {

                                runOnUiThread(() -> Toast.makeText(mContext,
                                        mContext.getString(
                                                R.string.update_to_remove),
                                        Toast.LENGTH_SHORT).show());

                            }

                        });

                        thread.start();

                    }
                }

            } else {
                AsyncTask.execute(
                        () -> DatabaseDao.getInstance(mContext).getRanobeDao().delete(
                                mCurrentRanobe.getUrl()));
                mCurrentRanobe.setFavorited(false);
                item.setIcon(borderImage);
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.fav_button:
                SetToFavorite(item);
                break;
            case R.id.download_chapters:
                Intent intent = new Intent(mContext, DownloadActivity.class);
                if (RanobeKeeper.getInstance().getRanobe() != null) {
                    mContext.startActivity(intent);
                }
                break;
        }
        return true;
    }

}
