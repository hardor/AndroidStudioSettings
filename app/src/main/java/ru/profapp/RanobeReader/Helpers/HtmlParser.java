package ru.profapp.RanobeReader.Helpers;

import android.os.AsyncTask;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ru.profapp.RanobeReader.Common.StringResources;

/**
 * Created by Ruslan on 08.02.2018.
 */

public class HtmlParser extends AsyncTask<String, Void, Document> {

    private Map<String, String> Cookies = new HashMap<>();
    private Map<String, String> Data = new HashMap<>();
    private Map<String, String> Header = new HashMap<>();

    public HtmlParser() {
    }

    public HtmlParser(Map<String, String> cookies) {
        Cookies = cookies;
    }

    public HtmlParser(Map<String, String> cookies, Map<String, String> data) {
        Cookies = cookies;
        Data = data;
    }

    public HtmlParser(Map<String, String> cookies, Map<String, String> data,
            Map<String, String> header) {
        Cookies = cookies;
        Data = data;
        Header = header;
    }

    @Override
    protected Document doInBackground(String... params) {

        try {
            Connection.Method method = Connection.Method.GET;

            if (params.length > 1 && params[1] != null) {
                method = Connection.Method.valueOf(params[1]);
            }
            Connection connection = Jsoup.connect(params[0])
                    .cookies(Cookies)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML,"
                                    + " like Gecko) Chrome/65.0.3325.162 Safari/537.36")
                    .header("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,"
                                    + "image/apng,*/*;q=0.8")
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Accept-Encoding", "gzip, deflate")
                    .data(Data)
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .timeout(20 * 1000)
                    .method(method);
            for(Map.Entry<String, String> entry : Header.entrySet()) {
                 connection.header(entry.getKey(),entry.getValue());
            }

            if (method.compareTo(Connection.Method.POST) == 0) {
                return connection.post();
            }
            else {
                return connection.get();
            }


        } catch (IOException e) {
           // MyLog.SendError(StringResources.LogType.WARN, HtmlParser.class.toString(), "", e);
            return null;
        }

    }

    @Override
    protected void onPostExecute(Document result) {

    }

}
