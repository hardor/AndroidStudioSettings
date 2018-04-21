package ru.profapp.RanobeReader;

import static ru.profapp.RanobeReader.Common.StringResources.is_readed_Pref;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.jsoup.Jsoup;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Common.ThemeUtils;
import ru.profapp.RanobeReader.CustomElements.ObservableWebView;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfChapterTextGson;
import ru.profapp.RanobeReader.JsonApi.Rulate.ChapterTextGson;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

public class ChapterText extends AppCompatActivity {

    final Gson gson = new GsonBuilder().setLenient().create();
    Chapter mCurrentChapter;
    ObservableWebView mWebView;
    Context mContext;
    Integer mIndex;
    Integer mScrollY;
    Integer mChapterCount;
    List<Chapter> mChapterList;
    SharedPreferences sPref;
    SharedPreferences sChapterPref;
    BottomNavigationItemView nextMenu, prevMenu;
    ProgressBar progressUrl;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {

        switch (item.getItemId()) {

            case R.id.navigation_prev:
                OnClicked(+1);

                return true;
            case R.id.navigation_next:
                OnClicked(-1);

                return true;

        }

        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        ThemeUtils.onActivityCreateSetTheme(this, false);
        setupActionBar();
        setContentView(R.layout.activity_chapter_text);
        mContext = getApplicationContext();

        mIndex = getIntent().getIntExtra("ChapterIndex", 0);

        Ranobe currentRanobe = RanobeKeeper.getInstance().getRanobe();
        mChapterList = currentRanobe.getChapterList();
        mChapterCount = mChapterList.size();
        mCurrentChapter = mChapterList.get(mIndex);

        progressUrl = findViewById(R.id.progressBar2);

        mWebView = findViewById(R.id.textWebview);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                progressUrl.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.scrollTo(0, mScrollY);
                setTitle(mCurrentChapter.getTitle());
                progressUrl.setVisibility(View.GONE);
            }
        });

        sChapterPref = mContext.getSharedPreferences(StringResources.Last_readed_Pref,
                MODE_PRIVATE);

        mWebView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
            public void onScroll(int l, int t, int oldl, int oldt) {
                sChapterPref.edit().putInt(mCurrentChapter.getUrl(), t).commit();
            }
        });

        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        sPref = getSharedPreferences(is_readed_Pref,
                MODE_PRIVATE);

        GetChapterText(mCurrentChapter,false);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        prevMenu = findViewById(R.id.navigation_prev);
        nextMenu = findViewById(R.id.navigation_next);

        prevMenu.setVisibility(mIndex < mChapterCount - 1 ? View.VISIBLE : View.INVISIBLE);
        nextMenu.setVisibility(mIndex > 0 ? View.VISIBLE : View.INVISIBLE);

        initWebView();

    }

    private void initWebView() {
        if (!mCurrentChapter.getReaded()) {
            putToReaded(mCurrentChapter.getUrl());
        }

        String style = "style = \"text-align: justify; text-indent: 20px;font-size: "
                + RanobeKeeper.getInstance().getChapterTextSize().toString()
                + "px;\"";

        String summary =
                "<html><body " + style + ">" + mCurrentChapter.getText() + "</body></html>";

        sChapterPref = mContext.getSharedPreferences(StringResources.Last_readed_Pref,
                MODE_PRIVATE);
        if (sChapterPref != null) {
            mScrollY = sChapterPref.getInt(mCurrentChapter.getUrl(), 0);
        } else {
            mScrollY = 0;
        }

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
      return  GetChapterText(chapter,true);
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
        return false;
    }

    private boolean GetRanobeRfChapterText( boolean isButton) {
        String response = JsonRanobeRfApi.getInstance().GetChapterText(mCurrentChapter);
        try {
            RfChapterTextGson readyGson = gson.fromJson(response, RfChapterTextGson.class);
            if (readyGson.getStatus() == 200) {

                mCurrentChapter.UpdateChapter(readyGson.getResult(), mContext,  isButton);
                return true;
            } else {
                mCurrentChapter.setText(readyGson.getMessage());

            }
        } catch (JsonParseException e) {
            MyLog.SendError(StringResources.LogType.WARN, ChapterText.class.toString(), response,
                    e);
            return false;
        }
        return false;
    }

    private boolean GetRulateChapterText(boolean  isButton) {
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
            MyLog.SendError(StringResources.LogType.WARN, ChapterText.class.toString(), response,
                    e);

            try {
                ChapterTextGson readyGson = gson.fromJson(Jsoup.parse(response).text(),
                        ChapterTextGson.class);

                if (readyGson.getStatus().equals("success")) {

                    mCurrentChapter.UpdateChapter(readyGson.getResponse(), mContext,isButton);
                    return true;
                } else {
                    mCurrentChapter.setText(readyGson.getMsg());
                }
            } catch (Exception e2) {

                MyLog.SendError(StringResources.LogType.WARN, ChapterText.class.toString(),
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

                GetChapterText(mChapterList.get(mIndex),false);
                initWebView();
            } catch (ArrayIndexOutOfBoundsException e) {
                mIndex -= i;
                MyLog.SendError(StringResources.LogType.WARN, ChapterText.class.toString(), "", e);

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
