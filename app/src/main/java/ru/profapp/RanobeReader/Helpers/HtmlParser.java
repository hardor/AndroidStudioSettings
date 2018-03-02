package ru.profapp.RanobeReader.Helpers;

import android.os.AsyncTask;

import com.google.firebase.crash.FirebaseCrash;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ruslan on 08.02.2018.
 */

public class HtmlParser extends AsyncTask<String, Void, Document> {

    Document doc = null;
    Map<String, String> Cookies = new HashMap<String, String>();

    public HtmlParser() {
    }

    public HtmlParser(Map<String, String> cookies) {
        Cookies = cookies;
    }

    @Override
    protected Document doInBackground(String... params) {

        //Todo: time out .timeout(5000
        try {
            doc = Jsoup.connect(params[0])
                    .cookies(Cookies)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML,"
                                    + " like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .header("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,"
                                    + "image/apng,*/*;q=0.8")
                    .ignoreContentType(true)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        return doc;

    }

    @Override
    protected void onPostExecute(Document result) {

    }

}
