package ru.profapp.RanobeReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.profapp.RanobeReader.Common.Constans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;
import ru.profapp.RanobeReader.RanobeRf.JsonRanobeRfApi;
import ru.profapp.RanobeReader.Rulate.JsonRulateApi;

public class ChapterText extends AppCompatActivity {

    WebView mWebView;
    Context mContext;
    SharedPreferences mPreferences;
    Chapter mChapter;
    Ranobe mCurrentRanobe;
    Integer mIndex;
    Integer mChapterCount;
    List<Chapter> mChapterList;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_prev:
                    OnClicked(+1);
                    return true;
                case R.id.navigation_next:
                    OnClicked(-1);
                    return true;
                case R.id.navigation_dashboard:

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setMessage("dsfdsfsdf")
                            .setTitle("fdsfsdfs");
                    builder.create();

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_chapter_text);
        mContext = getApplicationContext();
        mWebView = (WebView) findViewById(R.id.textWebview);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                view.scrollTo(0, 0);
                setTitle(mChapter.getTitle());
            }
        });

        mChapter = (Chapter) getIntent().getParcelableExtra("ChapterInfo");
        mCurrentRanobe = getIntent().getParcelableExtra("RanobeInfo");

        mChapterList = mCurrentRanobe.getChapterList();

        mChapterCount = mChapterList.size();

        int currentPosition = 0;

        for (Chapter chapter : mChapterList) {

            if (chapter.getUrl().equals(mChapter.getUrl())) {
                mIndex = currentPosition;
                break;
            }
            currentPosition++;
        }

        GetChapterText(mChapter);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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

        if (mChapter.getText() == null || mChapter.getText().isEmpty()) {
            mChapter = chapter;
            String url = mChapter.getRanobeUrl();

            if (url.contains(StringResources.RanobeRf_Site)) {
                GetRanobeRfChapterText();
            } else if (url.contains(StringResources.Rulate_Site)) {
                GetRulateChapterText();
            }
        }
        String summary = "<html><body>" + mChapter.getText() + "points.</body></html>";
        mWebView.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);
    }

    private void GetRanobeRfChapterText() {
        Document doc = JsonRanobeRfApi.getInstance().GetChapterText(mChapter);
        final Pattern pattern = Pattern.compile("<body>([\\S*\\.*\\W*]+)<\\/body>");

        String res = "";
        try {
            final Matcher matcher = pattern.matcher(doc.html());
            matcher.find();
            res = matcher.group(1);
        } catch (NullPointerException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        // Todo: маг 144 150
        res = res.replace("\"\\&quot;", "\\\"");
        res = res.replace("\\&quot;\"", "\\\"");
        res = res.replace("width:\"", "width:\\\"");

        try {
            JSONObject jsonObject = new JSONObject(res);
            if (jsonObject.getInt("status") == 200) {

                mChapter.UpdateChapter(jsonObject.getJSONObject("result"),
                        Constans.JsonObjectFrom.RanobeRfGetChapterText, mContext);
            }
        } catch (JSONException e) {
            try {
                JSONObject jsonObject = new JSONObject(doc.text());
                if (jsonObject.getInt("status") == 200) {

                    mChapter.UpdateChapter(jsonObject.getJSONObject("result"),
                            Constans.JsonObjectFrom.RanobeRfGetChapterText, mContext);
                }
            } catch (JSONException e2) {
                e2.printStackTrace();
                FirebaseCrash.report(e);
            }

        }
    }

    private void GetRulateChapterText() {
        mPreferences = mContext.getSharedPreferences(StringResources.Rulate_Login_Pref, 0);
        String token = mPreferences.getString(StringResources.KEY_Token, "");
        String response = JsonRulateApi.getInstance().GetChapterText(mChapter.getRanobeId(),
                mChapter.getId(),
                token);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.get("status").equals("success")) {

                mChapter.UpdateChapter(jsonObject.getJSONObject("response"),
                        Constans.JsonObjectFrom.RulateGetChapterText, mContext);
            }
        } catch (JSONException e) {

        }
    }

    private void OnClicked(int i) {
        mIndex += i;
        try {
            GetChapterText(mChapterList.get(mIndex));
        } catch (ArrayIndexOutOfBoundsException e) {
            mIndex -= i;
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

    }
}
