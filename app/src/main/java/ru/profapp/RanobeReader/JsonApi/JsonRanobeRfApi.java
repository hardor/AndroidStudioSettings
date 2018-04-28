package ru.profapp.RanobeReader.JsonApi;

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
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.StringHelper;
import ru.profapp.RanobeReader.Models.Chapter;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class JsonRanobeRfApi {
    private static volatile JsonRanobeRfApi instance;

    private Map<String, String> Cookies = new HashMap<>();

    private JsonRanobeRfApi() {
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

        for (int i = 0; i < 3; i++) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
            } catch (JSONException ignore) {
                jsonObject = null;
                ignore.printStackTrace();
            }
            if (jsonObject == null || jsonObject.optInt("status") == 422) {

                Connection.Response res = null;
                try {
                    res = Jsoup
                            .connect(StringResources.RanobeRf_Site)
                            .cookies(Cookies)
                            .method(Connection.Method.GET)
                            .execute();
                } catch (IOException ignore) {

                }

                Cookies = res != null ? res.cookies() : Cookies;

               response = getDocumentText(request, Cookies);

            } else {
                break;
            }

        }
        return response;
    }

    public String SearchBooks(String search) {
        String request = StringResources.RanobeRf_Site +"/v1/book/search/?q=" + search;

        return getDocumentText(request, Cookies);
    }

    public String GetChapterText(Chapter charpter) {

        String ranobeName = charpter.getRanobeUrl().replace(StringResources.RanobeRf_Site, "");

        String chapterName = charpter.getUrl().replace(StringResources.RanobeRf_Site,"").replace( ranobeName, "");

        ranobeName = ranobeName.replace("/","");
        chapterName = chapterName.replace("/","");
        String request = StringResources.RanobeRf_Site +"/v1/part/load/?bookAlias=" + ranobeName
                + "&partAlias=" + chapterName;
        return getDocumentText(request, Cookies);

    }

    public String GetBookInfo(String ranobeName) {

        String request = StringResources.RanobeRf_Site+"/v1/book/load/?book_alias=" + ranobeName;

        return getDocumentText(request, Cookies);
    }

    private String getDocumentText(String request, Map<String, String> Cookies) {

        Document html;
        try {
            html = new HtmlParser(Cookies).execute(request).get();
            String result = html.body().html();
            return StringHelper.getInstance().cleanJson(result);
        } catch (InterruptedException | NullPointerException | ExecutionException e) {
            MyLog.SendError(StringResources.LogType.WARN, JsonRulateApi.class.toString(), "", e);
        }
        return "";
    }

}
