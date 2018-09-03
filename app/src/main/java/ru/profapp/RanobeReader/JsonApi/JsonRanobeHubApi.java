package ru.profapp.RanobeReader.JsonApi;

import java.util.HashMap;
import java.util.Map;

import ru.profapp.RanobeReader.Common.ErrorConnectionException;
import ru.profapp.RanobeReader.Common.StringResources;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class JsonRanobeHubApi extends JsonBaseClass {

    private static volatile JsonRanobeHubApi instance;
    private final String ApiString = "";

    private JsonRanobeHubApi() {
    }

    public static JsonRanobeHubApi getInstance() {
        if (instance == null) {
            synchronized (JsonRanobeHubApi.class) {
                if (instance == null) {
                    instance = new JsonRanobeHubApi();
                }
            }
        }
        return instance;
    }

    public String Login(String login, String pass) throws ErrorConnectionException {
        return null;
    }

    public String GetReadyBooks(int page) throws ErrorConnectionException {
        String request = StringResources.RanobeHub_Site + "/search?page=" + String.valueOf(page);

        Map<String, String> header = new HashMap<>();
        header.put("X-Requested-With", "XMLHttpRequest");
        header.put("X-Csrf-Token", "bUiRubkaLYvzrYV9IiexSeCAbAQ8xd5OXNDGWzIA");

        return getUrlText(request, header);
    }

    public String SearchBooks(String search) throws ErrorConnectionException {
        String request = StringResources.RanobeHub_Site + "/api/ranobe/getByName/" + search;

        Map<String, String> header = new HashMap<>();
       // header.put("X-Requested-With", "XMLHttpRequest");

        return getUrlText(request, header);
    }

    public String GetFavoriteBooks(String token) throws ErrorConnectionException {
        return null;
    }

    public String GetChapterText(int book_id, int chapter_id, String token)
            throws ErrorConnectionException {
        return null;
    }

    public String AddBookmark(int book_id, String token) throws ErrorConnectionException {
        return null;
    }

    public String RemoveBookmark(int book_id, String token) throws ErrorConnectionException {
        return null;
    }

    public String GetBookInfo(int book_id, String token) throws ErrorConnectionException {
        return null;
    }
}
