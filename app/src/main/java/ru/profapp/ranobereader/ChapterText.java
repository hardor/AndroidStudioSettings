package ru.profapp.ranobereader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import ru.profapp.ranobereader.Common.AlertDialogManager;
import ru.profapp.ranobereader.Common.Constans;
import ru.profapp.ranobereader.Common.StringResources;
import ru.profapp.ranobereader.Models.Chapter;
import ru.profapp.ranobereader.Rulate.JsonRulateApi;

public class ChapterText extends AppCompatActivity {


    WebView mWebView;
    Context mContext;
    SharedPreferences mPreferences;
    Chapter mChapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                case R.id.navigation_dashboard:
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Пора покормить кота!", Toast.LENGTH_SHORT);
                    toast.show();
                    //item.setChecked()
                    return false;
                case R.id.navigation_notifications:
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

        mChapter = (Chapter) getIntent().getParcelableExtra("ChapterInfo");
        GetChapterText();
        String summary = "<html><body><b>" + mChapter.getText() + "</b> points.</body></html>";
        mWebView.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);

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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void GetChapterText(Chapter chapter, Context context) {
        mChapter = chapter;
        mContext =context;
        GetChapterText();
    }

    private void GetChapterText() {
        mPreferences = mContext.getSharedPreferences(StringResources.Rulate_Login_Pref, 0);
        String token = mPreferences.getString(StringResources.KEY_Token, "");
        String response = JsonRulateApi.getInstance().GetChapterText(mChapter.getRanobeId(), mChapter.getId(),
                token);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.get("status").equals("success")) {

                mChapter.UpdateChapter(jsonObject.getJSONObject("response"),
                        Constans.JsonObjectFrom.RulateGetChapterText);
            }
        } catch (JSONException e) {


        }
    }

}
