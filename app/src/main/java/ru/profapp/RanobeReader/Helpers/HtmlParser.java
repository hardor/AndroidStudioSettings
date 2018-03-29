package ru.profapp.RanobeReader.Helpers;

import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;

import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ruslan on 08.02.2018.
 */

public class HtmlParser extends AsyncTask<String, Void, Document> {

    private Document doc = null;
    private Map<String, String> Cookies = new HashMap<>();

    public HtmlParser() {
    }

    public HtmlParser(Map<String, String> cookies) {
        Cookies = cookies;
    }

    @Override
    protected Document doInBackground(String... params) {

        try {
            doc = Jsoup.connect(params[0])
                    .cookies(Cookies)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML,"
                                    + " like Gecko) Chrome/65.0.3325.162 Safari/537.36")
                    .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Content-Type","text/html; charset=UTF-8")
                    .header("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Accept-Encoding","gzip, deflate")


                    //.ignoreContentType(true)
                   // .timeout(5000)
                    .get();

        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return doc;

    }

    @Override
    protected void onPostExecute(Document result) {

    }

}
