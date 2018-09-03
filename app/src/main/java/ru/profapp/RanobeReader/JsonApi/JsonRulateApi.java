package ru.profapp.RanobeReader.JsonApi;

import ru.profapp.RanobeReader.Common.ErrorConnectionException;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class JsonRulateApi extends JsonBaseClass  {

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
    
   
    public String Login(String login, String pass) throws ErrorConnectionException {
        String request = String.format(ApiString, "auth");

        request += "&login=" + login;
        request += "&pass=" + pass;

        return getUrlText(request);
    }

   
    public String GetReadyBooks(int page) throws ErrorConnectionException {
        String request = String.format(ApiString, "getReady");

        request += "&page=" + String.valueOf(page);

        return getUrlText(request);
    }

   
    public String SearchBooks(String search) throws ErrorConnectionException {
        if (search.isEmpty()) {
            return "";
        }

        String request = String.format(ApiString, "searchBooks");
        request += "&search=" + search;

        return getUrlText(request);
    }

    public String GetFavoriteBooks(String token) throws ErrorConnectionException {
        if (token.isEmpty()) {
            return "";
        }

        String request = String.format(ApiString, "bookmarks");
        request += "&token=" + token;

        return getUrlText(request);
    }

   
    public String GetChapterText(int book_id, int chapter_id, String token)
            throws ErrorConnectionException {
        String request = String.format(ApiString, "chapter");
        if (!token.isEmpty()) {
            request += "&token=" + token;
        }

        request += "&chapter_id=" + chapter_id;
        request += "&book_id=" + book_id;

        String str = getUrlText(request);
        return str;
    }

    public String AddBookmark(int book_id, String token) throws ErrorConnectionException {
        String request = String.format(ApiString, "addBookmark");
        if (!token.isEmpty()) {
            request += "&token=" + token;
        } else {
            return "";
        }
        request += "&book_id=" + book_id;

        return getUrlText(request);
    }

    public String RemoveBookmark(int book_id, String token) throws ErrorConnectionException {
        String request = String.format(ApiString, "removeBookmark");
        if (!token.isEmpty()) {
            request += "&token=" + token;
        } else {
            return "";
        }
        request += "&book_id=" + book_id;

        return getUrlText(request);
    }

   
    public String GetBookInfo(int book_id, String token) throws ErrorConnectionException {
        String request = String.format(ApiString, "book");

        if (!token.isEmpty()) {
            request += "&token=" + token;
        }
        request += "&book_id=" + book_id;

        return getUrlText(request).replace("comments\":\"\",", "\":null,");
    }

}
