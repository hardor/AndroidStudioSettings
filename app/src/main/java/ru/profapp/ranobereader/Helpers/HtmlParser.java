package ru.profapp.ranobereader.Helpers;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by Ruslan on 08.02.2018.
 */

public class HtmlParser extends AsyncTask<String, Void, Document> {

    Document doc = null;

    @Override
    protected Document doInBackground(String... params) {

        try {
            doc = Jsoup.connect(params[0]).get();
            return doc;
        } catch (IOException e) {

        }
        return null;
    }

    @Override
    protected void onPostExecute(Document result) {

    }


}
