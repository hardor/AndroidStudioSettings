package ru.profapp.RanobeReader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Common.ThemeUtils;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.Rulate.JsonRulateApi;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

public class ChapterText extends AppCompatActivity {

    Chapter mCurrentChapter;
    SharedPreferences settingPref;
    private WebView mWebView;
    private Context mContext;
    private Integer mIndex;
    private Integer mChapterCount;
    private List<Chapter> mChapterList;
    private SharedPreferences sPref;

    BottomNavigationItemView nextMenu, prevMenu;
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

        mWebView = findViewById(R.id.textWebview);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                view.scrollTo(0, 0);
                setTitle(mCurrentChapter.getTitle());
            }
        });

        sPref = getSharedPreferences(currentRanobe.getUrl().replaceAll("[^a-zA-Z0-9]", ""),
                MODE_PRIVATE);

        GetChapterText(mCurrentChapter);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        prevMenu = findViewById( R.id.navigation_prev);
        nextMenu = findViewById( R.id.navigation_next);

        prevMenu.setVisibility ( mIndex < mChapterCount - 1? View.VISIBLE : View.INVISIBLE );
        nextMenu.setVisibility( mIndex > 0? View.VISIBLE : View.INVISIBLE);
        initWebView();

    }

    private void initWebView() {
        putToReaded(mCurrentChapter.getUrl());

        String style = "style = \"text-align: justify; text-indent: 20px;font-size: "
                + RanobeKeeper.getInstance().getChapterTextSize().toString()
                + "px;\"";

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

    public void GetChapterText(Chapter chapter, Context context) {
        mContext = context;
        GetChapterText(chapter);
    }

    private void GetChapterText(Chapter chapter) {

        mCurrentChapter = chapter;
        if (mCurrentChapter.getText() == null || mCurrentChapter.getText().isEmpty()) {
            String url = mCurrentChapter.getRanobeUrl();

            if (url.contains(StringResources.RanobeRf_Site)) {
                GetRanobeRfChapterText();
            } else if (url.contains(StringResources.Rulate_Site)) {
                GetRulateChapterText();
            }
        }

    }

    private void GetRanobeRfChapterText() {
        Document doc = JsonRanobeRfApi.getInstance().GetChapterText(mCurrentChapter);
        final Pattern pattern = Pattern.compile("<body>([\\S*\\.*\\W*]+)<\\/body>");

        String res = "";
        try {
            final Matcher matcher = pattern.matcher(doc.html());
            matcher.find();
            res = matcher.group(1);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        // Todo: маг 144 150
        res = res.replace("\"\\&quot;", "\\\"");
        res = res.replace("\\&quot;\"", "\\\"");

        res = res.replace("width:\"", "width:\\\"");

        try {
            JSONObject jsonObject = new JSONObject(res);
            if (jsonObject.getInt("status") == 200) {

                mCurrentChapter.UpdateChapter(jsonObject.getJSONObject("result"),
                        RanobeConstans.JsonObjectFrom.RanobeRfGetChapterText, mContext);
                mCurrentChapter.setReaded(true);
            }
        } catch (JSONException e) {
            try {
                JSONObject jsonObject = new JSONObject(doc.text());
                if (jsonObject.getInt("status") == 200) {

                    mCurrentChapter.UpdateChapter(jsonObject.getJSONObject("result"),
                            RanobeConstans.JsonObjectFrom.RanobeRfGetChapterText, mContext);
                    mCurrentChapter.setReaded(true);
                }
            } catch (JSONException e2) {
                e2.printStackTrace();
                Crashlytics.logException(e);
            }

        }
    }

    private void GetRulateChapterText() {
        SharedPreferences preferences = mContext.getSharedPreferences(
                StringResources.Rulate_Login_Pref, 0);
        String token = preferences.getString(StringResources.KEY_Token, "");
        String response = JsonRulateApi.getInstance().GetChapterText(mCurrentChapter.getRanobeId(),
                mCurrentChapter.getId(),
                token);

        response = response.replace("\"\\&quot;", "\\\"");
        response = response.replace("\\&quot;\"", "\\\"");
//        response = response.replace("&lt;", "<");
//        response = response.replace("&gt;", ">");

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.get("status").equals("success")) {

                mCurrentChapter.UpdateChapter(jsonObject.getJSONObject("response"),
                        RanobeConstans.JsonObjectFrom.RulateGetChapterText, mContext);
                mCurrentChapter.setReaded(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void OnClicked(int i) {

        mIndex += i;
        if (mIndex >= 0 && mIndex <= mChapterCount - 1) {
            try {
                GetChapterText(mChapterList.get(mIndex));
                initWebView();
            } catch (ArrayIndexOutOfBoundsException e) {
                mIndex -= i;
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        } else {
            mIndex -= i;
        }

        prevMenu.setVisibility ( mIndex < mChapterCount - 1? View.VISIBLE : View.INVISIBLE );
        nextMenu.setVisibility( mIndex > 0? View.VISIBLE : View.INVISIBLE);

    }

    private void putToReaded(String ChapterUrl) {
        if (sPref == null) {
            sPref = mContext.getSharedPreferences(
                    mCurrentChapter.getRanobeUrl().replaceAll("[^a-zA-Z0-9]", ""), MODE_PRIVATE);
        }

        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(ChapterUrl, true);
        ed.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
