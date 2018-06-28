package ru.profapp.RanobeReader;

import static ru.profapp.RanobeReader.Common.StringResources.Chapter_Position;
import static ru.profapp.RanobeReader.Common.StringResources.Chapter_Url;
import static ru.profapp.RanobeReader.Common.StringResources.CleanString;
import static ru.profapp.RanobeReader.Common.StringResources.is_readed_Pref;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Common.ThemeUtils;
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
    private WebView mWebView;
    private Context mContext;
    private Integer mIndex;
    private float mProgress;
    private Integer mChapterCount;
    private List<Chapter> mChapterList;
    private SharedPreferences sPref, sChapterPref;
    private ImageButton nextMenu, prevMenu, bookmarkMenu;

    private ProgressBar progressUrl;

    private void set_web_colors() {

        SharedPreferences settingPref = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());
        boolean oldColor = settingPref.getBoolean(
                getResources().getString(R.string.pref_general_app_theme), false);
        settingPref.edit().putBoolean(getResources().getString(R.string.pref_general_app_theme),
                !oldColor).commit();
        ThemeUtils.setTheme(!oldColor);
        ThemeUtils.onActivityCreateSetTheme();
        this.recreate();

    }

    private void set_bookmark() {

        sChapterPref = mContext.getSharedPreferences(CleanString(mCurrentChapter.getRanobeUrl()),
                MODE_PRIVATE);

        sChapterPref.edit().putFloat(Chapter_Position, this.calculateProgression()).commit();
        sChapterPref.edit().putString(Chapter_Url, mCurrentChapter.getUrl()).commit();
        Toast.makeText(mContext, getString(R.string.bookmark_saved), Toast.LENGTH_SHORT).show();
    }

    private float calculateProgression() {
        float positionTopView = mWebView.getTop();
        float contentHeight = mWebView.getContentHeight();
        float currentScrollPosition = mWebView.getScrollY();
        return (currentScrollPosition - positionTopView) / contentHeight;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setupActionBar();
        setContentView(R.layout.activity_chapter_text);

        if (savedInstanceState != null) {
            mIndex = savedInstanceState.getInt("ChapterIndex",
                    getIntent().getIntExtra("ChapterIndex", 0));
            mProgress = savedInstanceState.getFloat("Progress", -1);
        } else {
            mIndex = getIntent().getIntExtra("ChapterIndex", 0);
            mProgress = getIntent().getFloatExtra("Progress", -1);
        }

        mContext = ChapterTextActivity.this;

        Ranobe currentRanobe = RanobeKeeper.getInstance().getRanobe();
        mChapterList = currentRanobe.getChapterList();
        mCurrentChapter = mChapterList.get(mIndex);
        mChapterCount = mChapterList.size();

        if (currentRanobe.getReversed()) {
            Collections.reverse(mChapterList);
            mIndex = mChapterCount - mIndex - 1;
        }

        progressUrl = findViewById(R.id.progressBar2);

        mWebView = findViewById(R.id.textWebview);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setTitle(mCurrentChapter.getTitle());
                progressUrl.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                final WebView newView = view;

                newView.postDelayed(new Runnable() {
                    public void run() {
                        if (newView.getProgress() == 100) {
                            newView.postDelayed(() -> {

                                int mScrollY = 0;
                                if (mProgress > -1) {
                                    float webviewsize =
                                            newView.getContentHeight() - newView.getTop();
                                    float positionInWV = webviewsize * mProgress;
                                    mScrollY = Math.round(newView.getTop() + positionInWV);
                                    mProgress = -1;
                                }

                                newView.scrollTo(0, mScrollY);

                            }, 300);
                        } else {
                            newView.post(this);
                        }
                    }
                }, 300);

                progressUrl.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

        });

        if (getIntent().getBooleanExtra("Bookmark", false)) {
            sChapterPref = mContext.getSharedPreferences(
                    CleanString(mCurrentChapter.getRanobeUrl()),
                    MODE_PRIVATE);
            if (sChapterPref != null) {

                for (Map.Entry<String, ?> key : sChapterPref.getAll().entrySet()) {
                    if (key.getKey().equals(Chapter_Position)) {
                        Object result = key.getValue();

                        if (result instanceof Integer) {
                            mProgress = ((Integer) result).floatValue();
                        } else if (result instanceof Float) {
                            mProgress = (Float) result;
                        }

                        break;
                    }
                }

            } else {
                mProgress = -1;
            }
        }

        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        sPref = getSharedPreferences(is_readed_Pref,
                MODE_PRIVATE);

        Boolean loadResult = GetChapterText(mCurrentChapter, false);

        prevMenu = findViewById(R.id.navigation_prev);
        nextMenu = findViewById(R.id.navigation_next);
        bookmarkMenu = findViewById(R.id.navigation_bookmark);

        prevMenu.setVisibility(mIndex < mChapterCount - 1 ? View.VISIBLE : View.INVISIBLE);
        nextMenu.setVisibility(mIndex > 0 ? View.VISIBLE : View.INVISIBLE);

        nextMenu.setOnClickListener(v ->
                OnClicked(-1)
        );

        prevMenu.setOnClickListener(v ->
                OnClicked(+1)
        );
        bookmarkMenu.setOnClickListener(v ->
                set_bookmark()
        );

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
                "<html><body " + style + ">" + "<b>" + mCurrentChapter.getTitle() + "</b>" + "</br>"
                        + mCurrentChapter.getText() + "</body></html>";

        mWebView.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.navigation_day_night:
                set_web_colors();
                break;
            case R.id.navigation_open_in_browser:
                String url = mCurrentChapter.getUrl();

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }

                if (url.contains(StringResources.Rulate_Site)) {
                    url = url + "/ready";
                }
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(browserIntent);
                } catch (Exception ignored) {

                }
                break;
        }

        return true;

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
                if (readyGson.getResult().getPart().getPayment()
                        && mCurrentChapter.getText().equals("")) {
                    mCurrentChapter.setText("Даннная страница находится на платной подписке");
                    return false;
                }
                return true;
            } else {
                mCurrentChapter.setText(readyGson.getMessage());

            }
        } catch (JsonParseException e) {
            MyLog.SendError(StringResources.LogType.WARN, ChapterTextActivity.class.toString(),
                    mCurrentChapter.getUrl(), e);
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
                    mCurrentChapter.getUrl(),
                    e);
            return false;

        }

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
            Toast.makeText(mContext, R.string.not_exist, Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.chaptermain, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        outState.putInt("ChapterIndex", mIndex);
        outState.putFloat("Progress", calculateProgression());
    }

}
