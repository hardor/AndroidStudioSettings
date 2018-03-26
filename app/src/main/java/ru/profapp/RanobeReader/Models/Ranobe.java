package ru.profapp.RanobeReader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.Html;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;

/**
 * Created by Ruslan on 09.02.2018.
 */
@Entity(tableName = "ranobe")
public class Ranobe {

    @PrimaryKey
    @NonNull
    private String Url;
    private int Id;
    private String EngTitle;
    private String Title;
    private String Image;
    private Date ReadyDate = null;
    private String Lang;
    private String Description;
    private String AdditionalInfo;
    private String RanobeSite;
    private int CharpterCount;
    private int LastReadedCharpter;
    private Boolean WasUpdated;
    private Boolean Favorited;
    private Boolean FavoritedInWeb;
    private String Rating;
    private String Status;
    @Ignore
    private List<Chapter> chapterList = new ArrayList<>();
    @Ignore
    private DateFormat format = new SimpleDateFormat("MM-dd HH:mm");

    @Ignore
    private DateFormat ranobeChapterFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    @Ignore
    private Calendar mCalendar = Calendar.getInstance();

    public Ranobe() {
    }

    public void UpdateRanobe(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {

        switch (enumFrom) {
            case RulateGetReady:
                fromRulateGetReady(object, enumFrom);
                break;
            case RulateGetBookInfo:
                fromRulateGetBookInfo(object, enumFrom);
                break;
            case RanobeRfGetReady:
                fromRanobeRfGetReady(object, enumFrom);
                break;
            case RanobeRfSearch:
                fromRanobeRfSearch(object, enumFrom);
                break;
            case RulateSearch:
                fromRulateSearch(object, enumFrom);
                break;

            case RulateFavorite:
                fromRulateFavorite(object, enumFrom);
                break;
            default:
                //  throw new NullPointerException();
                break;
        }
    }

    private void fromRulateFavorite(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {
        setRanobeSite(StringResources.Rulate_Site);
        setFavoritedInWeb(true);
        try {
            EngTitle = object.getString("s_title");
            Title = object.getString("t_title");
            Lang = object.optString("lang");
            CharpterCount = object.optInt("n_chapters");
            Id = object.getInt("book_id");
            Url = StringResources.Rulate_Site + "/book/" + Id;
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private void fromRulateSearch(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {
        setRanobeSite(StringResources.Rulate_Site);
        try {

            Id = object.getInt("id");
            EngTitle = object.getString("s_title");
            Title = object.getString("t_title");
            CharpterCount = object.optInt("n_chapters");
            Lang = object.getString("lang");

            ReadyDate = new java.util.Date(object.optLong("last_activity") * 1000);
            mCalendar.setTime(ReadyDate);
            mCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            ReadyDate = mCalendar.getTime();

            Status = object.getString("status");
            Rating = object.getString("rating");
            // Todo:
            //Image = object.getString("img");

        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        Url = StringResources.Rulate_Site + "/book/" + Id;

    }

    private void fromRanobeRfSearch(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {

        setRanobeSite(StringResources.RanobeRf_Site);
        try {

            Id = object.getInt("id");
            Title = object.getString("label");
            Url = object.getString("link");
            Image = object.getString("image");

            if (object.has("part")) {
                JSONObject chapterJson = object.getJSONObject("part");
                Chapter chapter = new Chapter(chapterJson, enumFrom);
                chapter.setRanobeUrl(Url);
                chapterList.add(chapter);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private void UpdateRanobe(Document object, RanobeConstans.JsonObjectFrom enumFrom) {

        switch (enumFrom) {
            case RanobeRfGetBookInfo:
                fromRanobeRfGetBookInfo(object, enumFrom);
                break;
            default:
                //  throw new NullPointerException();
                break;
        }
    }

    private void fromRanobeRfGetReady(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {
        setRanobeSite(StringResources.RanobeRf_Site);
        try {

            Title = object.getString("name");
            ReadyDate = new java.util.Date(object.getLong("last_updated_book") * 1000);

            Url = object.getString("alias");
            if (object.has("images")) {
                JSONArray jsonArray = object.optJSONArray("images");
                Image = StringResources.RanobeRf_Site + jsonArray.getString(0);
            }

            if (object.has("parts")) {
                JSONArray jsonArray = object.optJSONArray("parts");
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject value = jsonArray.optJSONObject(i);
                    Chapter chapter = new Chapter(value, enumFrom);
                    chapter.setRanobeUrl(Url);
                    chapterList.add(chapter);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void fromRanobeRfGetBookInfo(Document object, RanobeConstans.JsonObjectFrom enumFrom) {
        setRanobeSite(StringResources.RanobeRf_Site);
        Elements additionalElements = object.select(
                "div.block-cooperation div.book__description p");

        AdditionalInfo = "";
        for (Element el : additionalElements) {
            if (!el.text().isEmpty()) {
                AdditionalInfo += el.text() + "\n";
            }
        }

        Description = object.selectFirst("div.block-cooperation + div p").text();
        Rating = object.selectFirst("div.rating-text").text();

        Elements chapterElements = object.select("div.book__content-table table.table");

        for (Element el : chapterElements) {

            Chapter chapter = new Chapter();
            chapter.setRanobeUrl(Url);
            try {

                chapter.setTitle(el.selectFirst("a").text());
                chapter.setUrl(el.selectFirst("a").attr("href"));

                chapter.setTime(ranobeChapterFormat.parse(el.selectFirst("time").attr("datetime")));
            } catch (ParseException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                chapter.setTime(new Date());
            } catch (NullPointerException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }

            chapterList.add(chapter);
        }

    }

    private void fromRulateGetBookInfo(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {
        setRanobeSite(StringResources.Rulate_Site);
        try {
            Id = object.optInt("id");
            Url = StringResources.Rulate_Site + "/book/" + Id;
            EngTitle = object.getString("s_title");
            Title = object.getString("t_title");
            CharpterCount = object.optInt("n_chapters");
            Lang = object.getString("lang");

            ReadyDate = new java.util.Date(object.optLong("last_activity") * 1000);
            mCalendar.setTime(ReadyDate);
            mCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            ReadyDate = mCalendar.getTime();

            Status = object.getString("status");
            Rating = object.getString("rating");
            Image = object.getString("img");
            if (object.has("chapters")) {
                JSONArray jsonArray = object.optJSONArray("chapters");
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject value = jsonArray.optJSONObject(i);
                    Chapter chapter = new Chapter(value, enumFrom);
                    chapter.setRanobeId(Id);
                    chapter.setRanobeUrl(Url);
                    chapter.setUrl(Url + "/" + chapter.getId());
                    chapterList.add(chapter);

                }
                Collections.reverse(chapterList);
            }
            Description = "Рейтинг: " + Rating +
                    "\nСтатус: " + Status +
                    "\nПеревод: " + Lang +
                    "\nКоличество глав: " + CharpterCount;

        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private void fromRulateGetReady(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {
        setRanobeSite(StringResources.Rulate_Site);
        try {
            Id = object.getInt("book_id");
            EngTitle = object.getString("s_title");
            Title = object.getString("t_title");
            Image = object.getString("img").replace("-5050", "");

            Lang = object.getString("lang");
            ReadyDate = format.parse(object.getString("ready_date"));

            mCalendar.setTime(ReadyDate);
            mCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));

            ReadyDate = mCalendar.getTime();
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (ParseException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            ReadyDate = new Date();
        }

        Url = StringResources.Rulate_Site + "/book/" + Id;
    }

    @NonNull
    public String getUrl() {
        return Url;
    }

    public void setUrl(@NonNull String url) {
        Url = url;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getEngTitle() {
        return EngTitle;
    }

    public void setEngTitle(String engTitle) {
        EngTitle = engTitle;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public Date getReadyDate() {
        return ReadyDate;
    }

    public void setReadyDate(Date readyDate) {
        ReadyDate = readyDate;
    }

    public String getLang() {
        return Lang;
    }

    public void setLang(String lang) {
        Lang = lang;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getRanobeSite() {
        return RanobeSite;
    }

    public void setRanobeSite(String ranobeSite) {
        RanobeSite = ranobeSite;
    }

    public int getCharpterCount() {
        return CharpterCount;
    }

    public void setCharpterCount(int charpterCount) {
        CharpterCount = charpterCount;
    }

    public int getLastReadedCharpter() {
        return LastReadedCharpter;
    }

    public void setLastReadedCharpter(int lastReadedCharpter) {
        LastReadedCharpter = lastReadedCharpter;
    }

    public Boolean getWasUpdated() {
        return WasUpdated == null ? false : WasUpdated;
    }

    public void setWasUpdated(Boolean wasUpdated) {
        WasUpdated = wasUpdated;
    }

    public Boolean getFavorited() {
        return Favorited != null ? Favorited : false;
    }

    public void setFavorited(Boolean favorited) {
        Favorited = favorited;
    }

    public Boolean getFavoritedInWeb() {
        return FavoritedInWeb == null ? false : FavoritedInWeb;
    }

    public void setFavoritedInWeb(Boolean favoritedInWeb) {
        FavoritedInWeb = favoritedInWeb;
    }

    public String getRating() {
        return Rating;
    }

    public void setRating(String rating) {
        Rating = rating;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public List<Chapter> getChapterList() {
        if (chapterList == null) {
            return new ArrayList<>();
        }
        List<Chapter> newList = new ArrayList<>();
        if (RanobeKeeper.getInstance().getHideUnavailableChapters()) {
            for (Chapter item : chapterList) {
                if (item.getCanRead()) {
                    newList.add(item);
                }
            }
            return newList;
        } else {
            return chapterList;
        }

    }

    public void setChapterList(List<Chapter> chapterList) {
        this.chapterList = chapterList;
    }

    public String getAdditionalInfo() {
        return AdditionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        AdditionalInfo = additionalInfo;
    }

    private void updateRulateRanobe(Context mContext) {
        SharedPreferences mPreferences = mContext.getSharedPreferences(
                StringResources.Rulate_Login_Pref, 0);
        String token = mPreferences.getString(StringResources.KEY_Token, "");
        String response = JsonRulateApi.getInstance().GetBookInfo(getId(), token);

        response = Html.fromHtml(response).toString();

//        response = response.replace("\"\\&quot;", "\\\"");
//        response = response.replace("\\&quot;\"", "\\\"");
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.get("status").equals("success")) {

                UpdateRanobe(jsonObject.getJSONObject("response"),
                        RanobeConstans.JsonObjectFrom.RulateGetBookInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private void updateRanobeRfRanobe() {

        Document response = JsonRanobeRfApi.getInstance().GetBookInfo(getUrl());

        UpdateRanobe(response, RanobeConstans.JsonObjectFrom.RanobeRfGetBookInfo);

    }

    public void updateRanobe(Context mContext) {

        try {
            if (getRanobeSite().equals(StringResources.Rulate_Site) || getUrl().contains(StringResources.Rulate_Site)) {
                updateRulateRanobe(mContext);
            } else if (getRanobeSite().equals(StringResources.RanobeRf_Site)|| getUrl().contains(StringResources.RanobeRf_Site)) {
                updateRanobeRfRanobe();
            }
            WasUpdated = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
