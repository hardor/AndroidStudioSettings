package ru.profapp.RanobeReader.Helpers;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ruslan on 08.02.2018.
 */

public class UrlParser extends AsyncTask<String, Void, String> {

    private Map<String, String> Cookies = new HashMap<>();
    private Map<String, String> Data = new HashMap<>();
    private Map<String, String> Header = new HashMap<>();

    public UrlParser() {
    }

    public UrlParser(  Map<String, String> header) {
        Header = header;
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

        } catch (IOException e) {
            return null;
        }
        return result.toString();

    }
/*
*
        StringBuilder result = new StringBuilder();
        try {
            String data ="";

            HttpURLConnection urlConnection;

            URL url = new URL(params[0]);
            urlConnection = (HttpURLConnection) ((url.openConnection()));

            //urlConnection.setUseCaches(false);
           // urlConnection.setChunkedStreamingMode(1024);


            urlConnection.setDoOutput(true);
            for(Map.Entry<String, String> entry : Header.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(),entry.getValue());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(data);
            writer.close();
            outputStream.close();

            InputStream response = urlConnection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));


            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();

        } catch (IOException e) {
            return null;
        }
        return result.toString();*/
    @Override
    protected void onPostExecute(String result) {

    }

}
