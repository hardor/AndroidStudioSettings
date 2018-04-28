package ru.profapp.RanobeReader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.Helpers.StringHelper;
import ru.profapp.RanobeReader.JsonApi.JsonRanobeRfApi;
import ru.profapp.RanobeReader.JsonApi.JsonRulateApi;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.Genre;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.ResultBookInfo;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfBook;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfBookInfoGson;
import ru.profapp.RanobeReader.JsonApi.Rulate.BookInfoGson;
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateBook;
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateComment;

/**
 * Created by Ruslan on 09.02.2018.
 */
@Entity(tableName = "ranobe")
public class Ranobe {

    @Ignore
    private final Gson gson = new GsonBuilder().setLenient().disableHtmlEscaping().create();
    @Ignore
    private final DateFormat format = new SimpleDateFormat("MM-dd HH:mm");

    @Ignore
    private final Calendar mCalendar = Calendar.getInstance();
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
    private String Genres="";
    @Ignore
    private List<Chapter> chapterList = new ArrayList<>();
    @Ignore
    private List<RulateComment> mRulateComments = new ArrayList<>();

    public Ranobe() {
    }

    public static boolean empty(final String s) {
        // Null-safe, short-circuit evaluation.
        return s == null || s.trim().isEmpty();
    }

    public void UpdateRulateRanobe(RulateBook book) {

        setRanobeSite(StringResources.Rulate_Site);

        if (book.getBookId() != null) {
            Id = book.getBookId();
        } else if (book.getId() != null) {
            Id = book.getId();
        }

        EngTitle = book.getSTitle() != null ? book.getSTitle() : EngTitle;
        Title = book.getTTitle() != null ? book.getTTitle() : Title;
        Image = book.getImg() != null ? book.getImg().replace("-5050", "") : Image;

        Lang = book.getLang() != null ? book.getLang() : Lang;
        try {

            if(ReadyDate == null) {
                if(book.getReadyDate() != null){
                    mCalendar.setTime(format.parse(book.getReadyDate()));
                    mCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                    ReadyDate = mCalendar.getTime();
                }
                ReadyDate = book.getLastActivity() != null ? new java.util.Date(
                        book.getLastActivity() * 1000) : ReadyDate;
            }


        } catch (ParseException e) {
            MyLog.SendError(StringResources.LogType.WARN, Ranobe.class.toString(), "", e);
        }

        Url = StringResources.Rulate_Site + "/book/" + Id;

        CharpterCount = book.getChaptersTotal() != null ? book.getChaptersTotal() : CharpterCount;

        Status = book.getStatus() != null ? book.getStatus() : Status;
        Rating = book.getRating() != null ? book.getRating() : Rating;

        if (book.getChapters() != null) {

            for (int i = 0; i < book.getChapters().size(); i++) {
                Chapter chapter = new Chapter(book.getChapters().get(i));
                chapter.setRanobeId(Id);
                chapter.setRanobeUrl(Url);
                chapter.setUrl(Url + "/" + chapter.getId());
                chapter.setRanobeName(Title);
                chapter.setIndex(i);
                chapterList.add(chapter);
            }
            Collections.reverse(chapterList);
        }
        if (book.getComments() != null) {
            mRulateComments = book.getComments();
            Collections.reverse(mRulateComments);
        }
        Description = "Рейтинг: " + Rating +
                "\nСтатус: " + Status +
                "\nПеревод: " + Lang +
                "\nКоличество глав: " + CharpterCount;

    }

    public void UpdateRanobeRfRanobe(RfBook book) {

        setRanobeSite(StringResources.RanobeRf_Site);

        Id = book.getId() != null ? book.getId() : Id;

        Title = empty(Title) ? (book.getName() != null ? book.getName() : Title) : Title;
        Title = empty(Title) ? (book.getTitle() != null ? book.getTitle() : Title) : Title;

        Url = empty(Url) ? (book.getAlias() != null ? book.getAlias() : Url) : Url;
        Url = empty(Url) ? (book.getUrl() != null ?  book.getUrl(): Url) : Url;

        Description = empty(Description) ? (book.getDescription() != null
                ? StringHelper.getInstance().removeTags(book.getDescription())
                : Description) : Description;
        AdditionalInfo = empty(AdditionalInfo) ? (book.getInfo() != null
                ? StringHelper.getInstance().removeTags(book.getInfo())
                : AdditionalInfo) : AdditionalInfo;

        ReadyDate = ReadyDate == null ? (book.getLastUpdatedBook() != null ? new Date(
                book.getLastUpdatedBook() * 1000) : ReadyDate) : ReadyDate;

        ReadyDate = ReadyDate == null ? (book.getPublishedAt() != null ? new Date(
                book.getPublishedAt() * 1000)
                : ReadyDate) : ReadyDate;

        Image = empty(Image) ? (book.getImages() != null ? StringResources.RanobeRf_Site
                + book.getImages().get(0)
                : Image) : Image;
        Image = empty(Image) ? (book.getImage() != null ? book.getImage().getDesktop().getImage()
                : Image) : Image;

        Rating = empty(Rating) ? (book.getLikes() != null ? "Likes: " + book.getLikes() + (
                book.getDislikes() != null ? Rating + "\nDislikes: " + book.getDislikes() : "")
                : Rating) : Rating;

        if (book.getParts() != null) {

            for (int i = 0; i < book.getParts().size(); i++) {

                Chapter chapter = new Chapter(book.getParts().get(i));
                chapter.setRanobeUrl(Url);
                chapter.setRanobeName(Title);
                chapter.setIndex(i);
                chapterList.add(chapter);
            }
        }

    }

    private void UpdateRanobeRfRanobe(ResultBookInfo result) {
        UpdateRanobeRfRanobe(result.getBook());



        for (Genre genre : result.getGenres()) {
            Genres = Genres.concat(genre.getTitle() + ", ");
        }

        chapterList.clear();

        for (int i = 0; i < result.getParts().size(); i++) {
            Chapter chapter = new Chapter(result.getParts().get(i));
            chapter.setRanobeUrl(Url);
            chapter.setRanobeName(Title);
            chapter.setIndex(i);
            chapterList.add(chapter);
        }
        Collections.reverse(chapterList);
    }

    public List<RulateComment> getRulateComments() {
        return mRulateComments;
    }

    public void setRulateComments(
            List<RulateComment> rulateComments) {
        mRulateComments = rulateComments;
    }

    public void UpdateRanobe(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {

        switch (enumFrom) {

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
            MyLog.SendError(StringResources.LogType.WARN, Ranobe.class.toString(), "", e);

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

            if (object.optLong("last_activity") == 0) {
                ReadyDate = null;
            } else {

                ReadyDate = new java.util.Date(object.optLong("last_activity") * 1000);
                mCalendar.setTime(ReadyDate);

                ReadyDate = mCalendar.getTime();
            }
            Status = object.getString("status");
            Rating = object.getString("rating");
            // Todo:
            //Image = object.getString("img");

        } catch (JSONException e) {
            MyLog.SendError(StringResources.LogType.WARN, Ranobe.class.toString(), "", e);

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
                chapter.setRanobeName(Title);
                chapter.setIndex(0);
                chapterList.add(chapter);
            }

        } catch (JSONException e) {
            MyLog.SendError(StringResources.LogType.WARN, Ranobe.class.toString(), "", e);

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
                    chapter.setRanobeName(Title);
                    chapter.setIndex(i);
                    chapterList.add(chapter);

                }
            }

        } catch (JSONException e) {
            MyLog.SendError(StringResources.LogType.WARN, Ranobe.class.toString(), "", e);

        }
    }

    @NonNull
    public String getUrl() {
        return Url == null ? "" : Url;
    }

    public void setUrl(@NonNull String url) {
        Url = url;
    }

    public int getId() {

        if (Id != 0 || !getRanobeSite().equals(StringResources.Rulate_Site)) {
            return Id;
        } else if (getUrl() != null && getRanobeSite().equals(StringResources.Rulate_Site)) {
            return Integer.parseInt(getUrl().replace(StringResources.Rulate_Site + "/book/", ""));
        }
        throw new NullPointerException();
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
        if (RanobeSite != null) {
            return RanobeSite;
        }
        if (getUrl().contains(StringResources.Rulate_Site)) {
            return StringResources.Rulate_Site;
        }
        if (getUrl().contains(StringResources.RanobeRf_Site)) {
            return StringResources.RanobeRf_Site;
        }
        return "";
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

        try {
            BookInfoGson bookGson = gson.fromJson(response, BookInfoGson.class);

            if (bookGson.getStatus().equals("success")) {
                UpdateRulateRanobe(bookGson.getResponse());
            }
        } catch (JsonParseException e) {
            MyLog.SendError(StringResources.LogType.WARN, Ranobe.class.toString(), "", e);

        }

    }

    private void updateRanobeRfRanobe() {

        String ranobeName = getUrl().replace(StringResources.RanobeRf_Site, "");
        ranobeName = ranobeName.substring(1, ranobeName.length() - 1);
        String response = JsonRanobeRfApi.getInstance().GetBookInfo(ranobeName);

        try {
            RfBookInfoGson bookGson = gson.fromJson(response, RfBookInfoGson.class);

            if (bookGson.getStatus() == 200) {
                UpdateRanobeRfRanobe(bookGson.getResult());
            }
        } catch (JsonParseException e) {
            MyLog.SendError(StringResources.LogType.WARN, RfBookInfoGson.class.toString(), "", e);

        }

        //    UpdateRanobe(response, RanobeConstans.JsonObjectFrom.RanobeRfGetBookInfo);

    }

    public void updateRanobe(Context mContext) throws NullPointerException {

        try {
            if (getRanobeSite().equals(StringResources.Rulate_Site) || (getUrl() != null
                    && getUrl().contains(StringResources.Rulate_Site))) {
                updateRulateRanobe(mContext);
                WasUpdated = true;
            } else if (getRanobeSite().equals(StringResources.RanobeRf_Site) || (getUrl() != null
                    && getUrl().contains(
                    StringResources.RanobeRf_Site))) {
                updateRanobeRfRanobe();
                WasUpdated = true;
            } else if (!getRanobeSite().equals(StringResources.Title_Site)) {
                throw new NullPointerException();
            }

        } catch (NullPointerException e) {
            WasUpdated = false;
            MyLog.SendError(StringResources.LogType.WARN, Ranobe.class.toString(), "", e);
            throw new NullPointerException();
        }

    }

    public String getGenres() {
        return Genres;
    }

    public void setGenres(String genres) {
        Genres = genres;
    }
}
