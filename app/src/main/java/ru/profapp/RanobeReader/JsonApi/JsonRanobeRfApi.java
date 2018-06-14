package ru.profapp.RanobeReader.JsonApi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Helpers.HtmlParser;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.StringHelper;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.Sequence;
import ru.profapp.RanobeReader.Models.Chapter;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class JsonRanobeRfApi {
    private static volatile JsonRanobeRfApi instance;

    private Map<String, String> Cookies = new HashMap<>();
    private String sequence = "";

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

    public String getSequence() {
        return sequence;
    }

    public void setSequence(List<Sequence> sequence) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Sequence>>() {
        }.getType();
        this.sequence = gson.toJson(sequence, listType);
    }

    public String GetReadyBooks(int page) {
        String request = StringResources.RanobeRf_Site + "/v1/book/main-page-books/";

        Map<String, String> data = new HashMap<>();
        data.put("page", String.valueOf(page + 1));
        data.put("sequence", sequence);

        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/x-www-form-urlencoded");
        header.put("Accept", "*/*");

        String response = getDocumentText(Cookies, data, header, request,
                Connection.Method.POST.name());

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

                response = getDocumentText(Cookies, data, header, request,
                        Connection.Method.POST.name());

            } else {
                break;
            }

        }
        return response;
    }

    public String GetAllBooks() {
        String request = StringResources.RanobeRf_Site
                + "/v1/book/list/?country=&limit=500&offset=0&order=popular";

        String response = getDocumentText(Cookies, request);

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

                response = getDocumentText(Cookies, request);

            } else {
                break;
            }

        }
        return response;
    }

    public String SearchBooks(String search) {
        String request = StringResources.RanobeRf_Site + "/v1/book/search/?q=" + search;

        return getDocumentText(Cookies, request);
    }

    public String GetChapterText(Chapter chapter) {

        String ranobeName = chapter.getRanobeUrl().replace(StringResources.RanobeRf_Site, "");

        String chapterName = chapter.getUrl().replace(StringResources.RanobeRf_Site, "").replace(
                ranobeName, "");

        ranobeName = ranobeName.replace("/", "");
        chapterName = chapterName.replace("/", "");
        String request = StringResources.RanobeRf_Site + "/v1/part/load/?bookAlias=" + ranobeName
                + "&partAlias=" + chapterName;
        return getDocumentText(Cookies, request);

    }

    public String GetBookInfo(String ranobeName) {

        String request = StringResources.RanobeRf_Site + "/v1/book/load/?book_alias=" + ranobeName;

        return getDocumentText(Cookies, request);
    }

    public String RemoveBookmark(int bookmark_id, String token) {

        if (!token.isEmpty()) {

            try {

                String request = StringResources.RanobeRf_Site + "/v1/bookmark/delete/";

                String rawData = "id=" + Integer.toString(bookmark_id);

                URL obj = new URL(request);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                //add reuqest header
                con.setRequestMethod("DELETE"); //e.g POST
                con.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded"); //e.g key = Accept, value =
                // application/json
                con.setRequestProperty("Authorization", "Bearer " + token);
                //e.g key = Accept, value = application/json
                con.setRequestProperty("Accept",
                        "*/*"); //e.g key = Accept, value = application/json

                con.setDoOutput(true);

                OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");

                w.write(rawData);
                w.close();

                int responseCode = con.getResponseCode();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();

                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return "";

    }

    public String AddBookmark(int book_id, int part_id, String token) {
        if (!token.isEmpty()) {
            String request = StringResources.RanobeRf_Site + "/v1/bookmark/add/";

            Map<String, String> header = new HashMap<>();
            header.put("Content-Type", "application/x-www-form-urlencoded");
            header.put("Authorization", "Bearer " + token);
            header.put("Accept", "*/*");
            Map<String, String> data = new HashMap<>();
            data.put("book_id", Integer.toString(book_id));
            data.put("part_id", Integer.toString(part_id));
            return getDocumentText(Cookies, data, header, request, Connection.Method.POST.name());
        } else {
            return "";
        }
    }

    private String getDocumentText(Map<String, String> Cookies, String... params) {

        Document html;
        try {
            html = new HtmlParser(Cookies).execute(params).get();
            String result = html.body().html();
            return StringHelper.getInstance().cleanJson(result);
        } catch (InterruptedException | NullPointerException | ExecutionException e) {
          //  MyLog.SendError(StringResources.LogType.WARN, JsonRulateApi.class.toString(), params[0], e);
        }
        return "";
    }

    private String getDocumentText(Map<String, String> Cookies, Map<String, String> Data,
            String... params) {

        Document html;
        try {
            html = new HtmlParser(Cookies, Data).execute(params).get();
            String result = html.body().html();
            return StringHelper.getInstance().cleanJson(result);
        } catch (InterruptedException | NullPointerException | ExecutionException e) {
            MyLog.SendError(StringResources.LogType.WARN, JsonRulateApi.class.toString(), "", e);
        }
        return "";
    }

    private String getDocumentText(Map<String, String> Cookies, Map<String, String> Data,
            Map<String, String> Header,
            String... params) {

        Document html;
        try {
            html = new HtmlParser(Cookies, Data, Header).execute(params).get();
            String result = html.body().html();
            return StringHelper.getInstance().cleanJson(result);
        } catch (InterruptedException | NullPointerException | ExecutionException e) {
            MyLog.SendError(StringResources.LogType.WARN, JsonRulateApi.class.toString(), "", e);
        }
        return "";
    }

    public String Login(String name, String password) {
        String request = StringResources.RanobeRf_Site + "/v1/auth/login/";

        Map<String, String> data = new HashMap<>();
        data.put("email", name);
        data.put("password", password);

        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/x-www-form-urlencoded");
        header.put("Accept", "*/*");

        return getDocumentText(Cookies, data, header, request, Connection.Method.POST.name());
    }

    public String GetFavoriteBooks(String token) {
        String request = StringResources.RanobeRf_Site + "/v1/bookmark/index/";

        Map<String, String> data = new HashMap<>();
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + token);
        header.put("Accept", "*/*");

        return getDocumentText(Cookies, data, header, request);
    }
}
