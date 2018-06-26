package ru.profapp.RanobeReader.Helpers;

import android.os.AsyncTask;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ruslan on 08.02.2018.
 */

public class UrlParser extends AsyncTask<String, Void, String> {


    public UrlParser() {
    }



    @Override
    protected String doInBackground(String... params) {

        StringBuilder result = new StringBuilder();
        try {

            URL url = new URL(params[0]);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();


        } catch (IOException ignored) {

        }
        return result.toString();

    }

    @Override
    protected void onPostExecute(String result) {

    }

}
