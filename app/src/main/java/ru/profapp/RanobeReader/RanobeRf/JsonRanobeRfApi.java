package ru.profapp.RanobeReader.RanobeRf;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Helpers.HtmlParser;
import ru.profapp.RanobeReader.Models.Chapter;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class JsonRanobeRfApi {
    private static volatile JsonRanobeRfApi instance;

    Map<String, String> Cookies = new HashMap<String, String>();

    public JsonRanobeRfApi() {
    }

    public static JsonRanobeRfApi getInstance() {
        if (instance == null) {
            synchronized (JsonRanobeRfApi.class) {
                if (instance == null) {
                    instance = new JsonRanobeRfApi();
                }
            }
        }
        return instance;
    }

    public String GetReadyBooks(int page) {
        String request = StringResources.RanobeRf_Site + "/v1/book/parts-main/?page=" + page;

        String response = getDocumentText(request, Cookies);
        try {
            while (true) {

                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.getInt("status") == 422) {

                    Connection.Response res = Jsoup
                            .connect(StringResources.RanobeRf_Site)
                            .cookies(Cookies)
                            .method(Connection.Method.POST)
                            .execute();

                    Cookies = res.cookies();
                    response = getDocumentText(request, Cookies);
                    continue;
                }
                break;
            }

            return response;
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            return "";
        }
    }

    public String SearchBooks(String search) {
        String request = "http://xn--80ac9aeh6f.xn--p1ai/v1/book/search/?q=" + search;

        return getDocumentText(request, Cookies);
    }

    public Document GetChapterText(Chapter charpter) {
        //http://xn--80ac9aeh6f.xn--p1ai/v1/part/load/?bookAlias=povelitel-lyubi-menya-nezhno
        // &partAlias=glava-23-ne-zainteresovana-v-veshchakh-kotorie-kto-to-uzhe-ispolzoval-ranshe
        String ranobeName = charpter.getRanobeUrl().replace("http://xn--80ac9aeh6f.xn--p1ai/", "");

        String chapterName = charpter.getUrl().replace("http://xn--80ac9aeh6f.xn--p1ai/",
                "").replace(
                ranobeName, "");

        ranobeName = ranobeName.substring(0, ranobeName.length() - 1);
        chapterName = chapterName.substring(0, chapterName.length() - 1);
        String request = "http://xn--80ac9aeh6f.xn--p1ai/v1/part/load/?bookAlias=" + ranobeName
                + "&partAlias=" + chapterName;
        Document doc = getDocument(request, Cookies);
        return doc;

    }

    public Document GetBookInfo(String request) {

        return getDocument(request, Cookies);
    }

    private String getDocumentText(String request, Map<String, String> Cookies) {

        Document doc = getDocument(request, Cookies);

        String result = "";

        try {
            result = doc.body().text();

        } catch (NullPointerException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return result;
    }

    private Document getDocument(String request, Map<String, String> Cookies) {

        Document html = null;
        try {
            html = new HtmlParser(Cookies).execute(request).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return html;
    }

}
