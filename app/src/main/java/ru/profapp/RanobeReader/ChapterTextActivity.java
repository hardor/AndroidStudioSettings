package ru.profapp.RanobeReader;

import static ru.profapp.RanobeReader.Common.StringResources.Chapter_Position;
import static ru.profapp.RanobeReader.Common.StringResources.Chapter_Url;
import static ru.profapp.RanobeReader.Common.StringResources.CleanString;
import static ru.profapp.RanobeReader.Common.StringResources.is_readed_Pref;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.jsoup.Jsoup;

import java.util.Collections;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.CustomElements.ObservableWebView;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfChapterTextGson;
import ru.profapp.RanobeReader.JsonApi.Rulate.ChapterTextGson;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

public class ChapterTextActivity extends AppCompatActivity {

    private final Gson gson = new GsonBuilder().setLenient().create();
    Chapter mCurrentChapter;
    private ObservableWebView mWebView;
    private Context mContext;
    private Integer mIndex;
    private Integer mScrollY;
    private Integer mChapterCount;
    private List<Chapter> mChapterList;
    private SharedPreferences sPref;
    private SharedPreferences sChapterPref;
    private BottomNavigationItemView nextMenu;
    private BottomNavigationItemView prevMenu;
    private final BottomNavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener
            = item -> {

        switch (item.getItemId()) {

            case R.id.navigation_prev:
                OnClicked(+1);
                return true;
            case R.id.navigation_next:
                OnClicked(-1);
                return true;
            case R.id.navigation_bookmark:
                set_bookmark();
                return true;

        }

        return false;
    };

    private ProgressBar progressUrl;

    private void set_bookmark() {

        sChapterPref = mContext.getSharedPreferences(CleanString(mCurrentChapter.getRanobeUrl()),
                MODE_PRIVATE);

        sChapterPref.edit().putInt(Chapter_Position, mWebView.getScrollY()).commit();
        sChapterPref.edit().putString(Chapter_Url, mCurrentChapter.getUrl()).commit();
        Toast.makeText(mContext, getString(R.string.bookmark_saved), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setupActionBar();
        setContentView(R.layout.activity_chapter_text);

        mContext = ChapterTextActivity.this;
        mIndex = getIntent().getIntExtra("ChapterIndex", 0);

        Ranobe currentRanobe = RanobeKeeper.getInstance().getRanobe();
        mChapterList = currentRanobe.getChapterList();
        mCurrentChapter = mChapterList.get(mIndex);
        mChapterCount = mChapterList.size();

        if (currentRanobe.getReversed()) {
            Collections.reverse(mChapterList);
            mIndex=mChapterCount-mIndex-1;
        }




        progressUrl = findViewById(R.id.progressBar2);

        mWebView = findViewById(R.id.textWebview);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressUrl.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setTitle(mCurrentChapter.getTitle());
                final WebView newView = view;

                newView.postDelayed(new Runnable() {
                    public void run() {
                        if (newView.getProgress() == 100) {
                            newView.postDelayed(() -> {
                                if (mScrollY == null) {
                                    mScrollY = 0;
                                }
                                newView.scrollTo(0, mScrollY);
                            }, 100);
                        } else {
                            newView.post(this);
                        }
                    }
                }, 100);

                progressUrl.setVisibility(View.GONE);
            }

        });

        if (getIntent().getBooleanExtra("Bookmark", false)) {
            sChapterPref = mContext.getSharedPreferences(
                    CleanString(mCurrentChapter.getRanobeUrl()),
                    MODE_PRIVATE);
            if (sChapterPref != null) {
                mScrollY = sChapterPref.getInt(Chapter_Position, 0);
            } else {
                mScrollY = 0;
            }
        }

        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        sPref = getSharedPreferences(is_readed_Pref,
                MODE_PRIVATE);

        Boolean loadResult = GetChapterText(mCurrentChapter, false);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // the scroll listener:
        mWebView.setOnScrollChangedCallback((l, t, oldl, oldt) -> {
            if (t > oldt) {
                navigation.animate().translationY(navigation.getHeight());
            } else if (t < oldt) {
                navigation.animate().translationY(0);
            }
        });

        prevMenu = findViewById(R.id.navigation_prev);
        nextMenu = findViewById(R.id.navigation_next);

        prevMenu.setVisibility(mIndex < mChapterCount - 1 ? View.VISIBLE : View.INVISIBLE);
        nextMenu.setVisibility(mIndex > 0 ? View.VISIBLE : View.INVISIBLE);

        initWebView(loadResult);

    }

    private void initWebView(Boolean loadResult) {
        if (!mCurrentChapter.getReaded() && loadResult) {
            putToReaded(mCurrentChapter.getUrl());
        }

        @ColorInt int color = getResources().getColor(R.color.webViewText);
        @ColorInt int color2 = getResources().getColor(R.color.webViewBackground);
        String style = "style = \"text-align: justify; text-indent: 20px;font-size: "
                + RanobeKeeper.getInstance().getChapterTextSize().toString() + "px;"
                + "color: " + String.format("#%06X", (0xFFFFFF & color))
                + "; background-color: " + String.format("#%06X", (0xFFFFFF & color2))
                + "\"";

        String summary =
                "<html><body " + style + ">" + mCurrentChapter.getText() + "</body></html>";

        mWebView.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean GetChapterText(Chapter chapter, Context context) {
        mContext = context;
        return GetChapterText(chapter, true);
    }

    private boolean GetChapterText(Chapter chapter, boolean isButton) {

        mCurrentChapter = chapter;
        if (mCurrentChapter.getText() == null || mCurrentChapter.getText().isEmpty()) {
            String url = mCurrentChapter.getRanobeUrl();
            try {
                if (url.contains(StringResources.RanobeRf_Site)) {
                    return GetRanobeRfChapterText(isButton);
                } else if (url.contains(StringResources.Rulate_Site)) {
                    return GetRulateChapterText(isButton);
                }
            } catch (Exception ignored) {
                return false;
            }

        }
        return true;
    }

    private boolean GetRanobeRfChapterText(boolean isButton) {
        String response = JsonRanobeRfApi.getInstance().GetChapterText(mCurrentChapter);
        try {
            RfChapterTextGson readyGson = gson.fromJson(response, RfChapterTextGson.class);
            if (readyGson.getStatus() == 200) {

                mCurrentChapter.UpdateChapter(readyGson.getResult(), mContext, isButton);
                return true;
            } else {
                mCurrentChapter.setText(readyGson.getMessage());

            }
        } catch (JsonParseException e) {
            MyLog.SendError(StringResources.LogType.WARN, ChapterTextActivity.class.toString(),
                    response,
                    e);
            return false;
        }
        return false;
    }

    private boolean GetRulateChapterText(boolean isButton) {
        SharedPreferences preferences = mContext.getSharedPreferences(
                StringResources.Rulate_Login_Pref, 0);
        String token = preferences.getString(StringResources.KEY_Token, "");
        String response = JsonRulateApi.getInstance().GetChapterText(mCurrentChapter.getRanobeId(),
                mCurrentChapter.getId(),
                token);
        try {
            ChapterTextGson readyGson = gson.fromJson(response, ChapterTextGson.class);

            if (readyGson.getStatus().equals("success")) {

                mCurrentChapter.UpdateChapter(readyGson.getResponse(), mContext, isButton);
                return true;
            } else {
                mCurrentChapter.setText(readyGson.getMsg());
                return false;
            }
        } catch (Exception e) {
            MyLog.SendError(StringResources.LogType.WARN, ChapterTextActivity.class.toString(),
                    response,
                    e);

            try {
                ChapterTextGson readyGson = gson.fromJson(Jsoup.parse(response).text(),
                        ChapterTextGson.class);

                if (readyGson.getStatus().equals("success")) {

                    mCurrentChapter.UpdateChapter(readyGson.getResponse(), mContext, isButton);
                    return true;
                } else {
                    mCurrentChapter.setText(readyGson.getMsg());
                }
            } catch (Exception e2) {

                MyLog.SendError(StringResources.LogType.WARN, ChapterTextActivity.class.toString(),
                        response,
                        e2);
                return false;
            }
        }
        return false;

    }

    private void OnClicked(int i) {

        mIndex += i;
        if (mIndex >= 0 && mIndex <= mChapterCount - 1) {
            try {

                Boolean loadResult = GetChapterText(mChapterList.get(mIndex), false);
                initWebView(loadResult);
            } catch (ArrayIndexOutOfBoundsException e) {
                mIndex -= i;
                MyLog.SendError(StringResources.LogType.WARN, ChapterTextActivity.class.toString(),
                        "", e);

            }
        } else {
            mIndex -= i;
        }

        prevMenu.setVisibility(mIndex < mChapterCount - 1 ? View.VISIBLE : View.INVISIBLE);
        nextMenu.setVisibility(mIndex > 0 ? View.VISIBLE : View.INVISIBLE);

    }

    private void putToReaded(String ChapterUrl) {
        if (sPref == null) {
            sPref = mContext.getSharedPreferences(
                    is_readed_Pref, MODE_PRIVATE);
        }

        mCurrentChapter.setReaded(true);
        sPref.edit().putBoolean(ChapterUrl, true).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
