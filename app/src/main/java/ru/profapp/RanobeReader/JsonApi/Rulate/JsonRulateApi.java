package ru.profapp.RanobeReader.JsonApi.Rulate;

import com.crashlytics.android.Crashlytics;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import ru.profapp.RanobeReader.Helpers.HtmlParser;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class JsonRulateApi {

    private static volatile JsonRulateApi instance;
    private String ApiString = "http://tl.rulate.ru/api/%s?key=fpoiKLUues81werht039";

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

    public String Login(String login, String pass) throws IOException {
        String request = String.format(ApiString, "auth");

        request += "&login=" + login;
        request += "&pass=" + pass;

        return getDocumentText(request);
    }

    public String GetReadyTranslatesHtml(String limit, String page) {
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

        return getDocumentText(request);
    }

    private String getDocumentText(String request) {

        Document html = null;
        try {
            html = new HtmlParser().execute(request).get();
//            String test = StringEscapeUtils.unescapeJava(html.body().html());
            return html.body().html();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Crashlytics.logException(e);

        } catch (NullPointerException e) {
            e.printStackTrace();
            Crashlytics.logException(e);

        }
        return "";
    }

}
