package ru.profapp.ranobereader.Helpers;

import android.os.AsyncTask;

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

        try {
            //Todo: time out .timeout(5000
            doc = Jsoup.connect(params[0]).cookies(Cookies).ignoreContentType(true).get();

            return doc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Document result) {

    }


}
