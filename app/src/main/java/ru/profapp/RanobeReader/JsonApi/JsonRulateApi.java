package ru.profapp.RanobeReader.JsonApi;

import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Helpers.HtmlParser;
import ru.profapp.RanobeReader.Helpers.StringHelper;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class JsonRulateApi  extends JsonBaseClass {

    private static volatile JsonRulateApi instance;
    private final String ApiString = "https://tl.rulate.ru/api/%s?key=fpoiKLUues81werht039";

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

        return getUrlText(request);
    }

    public String GetReadyTranslates(String limit, String page) {
        String request = String.format(ApiString, "getReady");

        if (limit != null) {
            request += "&limit=" + limit;
        }
        if (page != null) {
            request += "&page=" + page;
        }

        return getUrlText(request);
    }

    public String SearchBooks(String search) {
        if (search.isEmpty()) {
            return "";
        }

        String request = String.format(ApiString, "searchBooks");
        request += "&search=" + search;

        return getUrlText(request);
    }

    public String GetFavoriteBooks(String token) {
        if (token.isEmpty()) {
            return "";
        }

        String request = String.format(ApiString, "bookmarks");
        request += "&token=" + token;

        return getUrlText(request);
    }

    public String GetChapterText(int book_id, int chapter_id, String token) {
        String request = String.format(ApiString, "chapter");
        if (!token.isEmpty()) {
            request += "&token=" + token;
        }

        request += "&chapter_id=" + chapter_id;
        request += "&book_id=" + book_id;

        String str = getUrlText(request);

        return str.replaceAll("(src=\\\\\\\")\\\\\\/","$1https:\\\\\\/\\\\\\/tl.rulate.ru\\\\\\/");
    }

    public String AddBookmark(int book_id, String token) {
        String request = String.format(ApiString, "addBookmark");
        if (!token.isEmpty()) {
            request += "&token=" + token;
        } else {
            return "";
        }
        request += "&book_id=" + book_id;

        return getUrlText(request);
    }

    public String RemoveBookmark(int book_id, String token) {
        String request = String.format(ApiString, "removeBookmark");
        if (!token.isEmpty()) {
            request += "&token=" + token;
        } else {
            return "";
        }
        request += "&book_id=" + book_id;

        return getUrlText(request);
    }

    public String GetBookInfo(int book_id, String token) {
        String request = String.format(ApiString, "book");

        if (!token.isEmpty()) {
            request += "&token=" + token;
        }
        request += "&book_id=" + book_id;

        return getUrlText(request);
    }


}
