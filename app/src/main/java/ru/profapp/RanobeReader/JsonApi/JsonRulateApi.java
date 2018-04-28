package ru.profapp.RanobeReader.JsonApi;

import org.jsoup.nodes.Document;

import java.util.concurrent.ExecutionException;

import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Helpers.HtmlParser;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.StringHelper;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class JsonRulateApi {

    private static volatile JsonRulateApi instance;
    private final String ApiString = "http://tl.rulate.ru/api/%s?key=fpoiKLUues81werht039";

    private JsonRulateApi() {
    }

    public static JsonRulateApi getInstance() {
        if (instance == null) {
            synchronized (JsonRulateApi.class) {
                if (instance == null) {
                    instance = new JsonRulateApi();
                }
            }
        }
        return instance;
    }

    public String Login(String login, String pass) {
        String request = String.format(ApiString, "auth");

        request += "&login=" + login;
        request += "&pass=" + pass;

        return getDocumentText(request);
    }

    public String GetReadyTranslates(String limit, String page) {
        String request = String.format(ApiString, "getReady");

        if (limit != null) {
            request += "&limit=" + limit;
        }
        if (page != null) {
            request += "&page=" + page;
        }

        return getDocumentText(request);
    }

    public String SearchBooks(String search) {
        if (search.isEmpty()) {
            return "";
        }

        String request = String.format(ApiString, "searchBooks");
        request += "&search=" + search;

        return getDocumentText(request);
    }

    public String GetFavoriteBooks(String token) {
        if (token.isEmpty()) {
            return "";
        }

        String request = String.format(ApiString, "bookmarks");
        request += "&token=" + token;

        return getDocumentText(request);
    }

    public String GetChapterText(int book_id, int chapter_id, String token) {
        // http://tl.rulate.ru/api/chapter?key=fpoiKLUues81werht039&chapter_id=&book_id=
        String request = String.format(ApiString, "chapter");
        if (!token.isEmpty()) {
            request += "&token=" + token;
        }

        request += "&chapter_id=" + chapter_id;
        request += "&book_id=" + book_id;

        return getDocumentText(request);
    }

    public String AddBookmark(int book_id, String token) {
        String request = String.format(ApiString, "addBookmark");
        if (!token.isEmpty()) {
            request += "&token=" + token;
        } else {
            return "";
        }
        request += "&book_id=" + book_id;

        return getDocumentText(request);
    }

    public String RemoveBookmark(int book_id, String token) {
        String request = String.format(ApiString, "removeBookmark");
        if (!token.isEmpty()) {
            request += "&token=" + token;
        } else {
            return "";
        }
        request += "&book_id=" + book_id;

        return getDocumentText(request);
    }

    public String GetBookInfo(int book_id, String token) {
        String request = String.format(ApiString, "book");

        if (!token.isEmpty()) {
            request += "&token=" + token;
        }
        request += "&book_id=" + book_id;

        return getDocumentText(request).replace("comments\":\"\",", "\":null,");
    }

    private String getDocumentText(String request){

        Document html;
        try {
            html = new HtmlParser().execute(request).get();
            String result = html.body().html();
            return StringHelper.getInstance().cleanJson(result);
        } catch (InterruptedException | NullPointerException | ExecutionException  e) {
            MyLog.SendError(StringResources.LogType.WARN, JsonRulateApi.class.toString(), "", e);

        }
        return "";
    }

}
