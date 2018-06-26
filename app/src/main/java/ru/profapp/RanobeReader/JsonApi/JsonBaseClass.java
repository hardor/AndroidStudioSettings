package ru.profapp.RanobeReader.JsonApi;

import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ru.profapp.RanobeReader.Helpers.HtmlParser;
import ru.profapp.RanobeReader.Helpers.StringHelper;
import ru.profapp.RanobeReader.Helpers.UrlParser;

class JsonBaseClass {

    Map<String, String> Cookies = new HashMap<>();

    String getUrlText(String request) {

        try {
            return new UrlParser().execute(request).get();
        } catch (InterruptedException | NullPointerException | ExecutionException ignored) {

        }
        return "";
    }

    String getDocumentText(Map<String, String> Cookies, String... params) {

        Document html;
        try {
            html = new HtmlParser(Cookies).execute(params).get();
            String result = html.body().html();
            return StringHelper.getInstance().cleanJson(result);
        } catch (InterruptedException | NullPointerException | ExecutionException ignored) {
        }
        return "";
    }



    String getDocumentText(Map<String, String> Cookies, Map<String, String> Data,
            Map<String, String> Header,
            String... params) {

        Document html;
        try {
            html = new HtmlParser(Cookies, Data, Header).execute(params).get();

            String result = html.body().html();
            return StringHelper.getInstance().cleanJson(result);
        } catch (InterruptedException | NullPointerException | ExecutionException ignored) {

        }
        return "";
    }
}
