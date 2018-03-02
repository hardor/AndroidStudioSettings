package ru.profapp.RanobeReader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.crash.FirebaseCrash;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ru.profapp.RanobeReader.Common.Constans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.RanobeRf.JsonRanobeRfApi;
import ru.profapp.RanobeReader.Rulate.JsonRulateApi;

/**
 * Created by Ruslan on 09.02.2018.
 */
@Entity(tableName = "ranobe")
public class Ranobe implements Parcelable {

    public static final Creator<Ranobe> CREATOR = new Creator<Ranobe>() {
        @Override
        public Ranobe createFromParcel(Parcel in) {
            return new Ranobe(in);
        }

        @Override
        public Ranobe[] newArray(int size) {
            return new Ranobe[size];
        }
    };

    @PrimaryKey
    @NonNull
    private String Url;
    private int Id;
    private String EngTitle;
    private String Title;
    private String Image;
    private Date ReadyDate = new Date();
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

    public Ranobe() {
    }

    protected Ranobe(Parcel in) {
        Url = in.readString();
        Id = in.readInt();
        EngTitle = in.readString();
        Title = in.readString();
        Image = in.readString();
        Lang = in.readString();
        Description = in.readString();
        AdditionalInfo = in.readString();
        RanobeSite = in.readString();
        CharpterCount = in.readInt();
        LastReadedCharpter = in.readInt();
        byte tmpWasUpdated = in.readByte();
        WasUpdated = tmpWasUpdated == 0 ? null : tmpWasUpdated == 1;
        byte tmpIsFavorited = in.readByte();
        Favorited = tmpIsFavorited == 0 ? null : tmpIsFavorited == 1;
        byte tmpIsFavoritedInWeb = in.readByte();
        FavoritedInWeb = tmpIsFavoritedInWeb == 0 ? null : tmpIsFavoritedInWeb == 1;
        Rating = in.readString();
        chapterList = in.createTypedArrayList(Chapter.CREATOR);
    }

    public void UpdateRanobe(JSONObject object, Constans.JsonObjectFrom enumFrom) {

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
            default:
                //  throw new NullPointerException();
                break;
        }
    }

    public void UpdateRanobe(Document object, Constans.JsonObjectFrom enumFrom) {

        switch (enumFrom) {
            case RanobeRfGetBookInfo:
                fromRanobeRfGetBookInfo(object, enumFrom);
                break;
            default:
                //  throw new NullPointerException();
                break;
        }
    }

    private void fromRanobeRfGetReady(JSONObject object, Constans.JsonObjectFrom enumFrom) {
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
            FirebaseCrash.report(e);
        }
    }

    private void fromRanobeRfGetBookInfo(Document object, Constans.JsonObjectFrom enumFrom) {
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
                FirebaseCrash.report(e);
                chapter.setTime(new Date());
            } catch (NullPointerException e) {
                e.printStackTrace();
                FirebaseCrash.report(e);
            }

            chapterList.add(chapter);
        }

    }

    private void fromRulateGetBookInfo(JSONObject object, Constans.JsonObjectFrom enumFrom) {
        setRanobeSite(StringResources.Rulate_Site);
        try {
            Id = object.optInt("id");
            EngTitle = object.getString("s_title");
            Title = object.getString("t_title");
            CharpterCount = object.optInt("n_chapters");
            Lang = object.getString("lang");

            //last_activity
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
                    chapter.setUrl(chapter.getRanobeUrl() + "/" + chapter.getId());
                    chapterList.add(chapter);

                }
                Collections.reverse(chapterList);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
    }

    private void fromRulateGetReady(JSONObject object, Constans.JsonObjectFrom enumFrom) {
        setRanobeSite(StringResources.Rulate_Site);
        try {
            Id = object.getInt("book_id");
            EngTitle = object.getString("s_title");
            Title = object.getString("t_title");
            Image = object.getString("img").replace("-5050", "");

            Lang = object.getString("lang");
            ReadyDate = format.parse(object.getString("ready_date"));

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        } catch (ParseException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            ReadyDate = new Date();
        }

        Url = StringResources.Rulate_Site + "/book/" + Id;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Url);
        dest.writeInt(Id);
        dest.writeString(EngTitle);
        dest.writeString(Title);
        dest.writeString(Image);
        dest.writeString(Lang);
        dest.writeString(Description);
        dest.writeString(AdditionalInfo);
        dest.writeString(RanobeSite);
        dest.writeInt(CharpterCount);
        dest.writeInt(LastReadedCharpter);
        dest.writeByte((byte) (WasUpdated == null ? 0 : WasUpdated ? 1 : 2));
        dest.writeByte((byte) (Favorited == null ? 0 : Favorited ? 1 : 2));
        dest.writeByte((byte) (FavoritedInWeb == null ? 0 : FavoritedInWeb ? 1 : 2));
        dest.writeString(Rating);
        dest.writeTypedList(chapterList);
    }

    @Override
    public int describeContents() {
        return 0;
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
            return new ArrayList<Chapter>();
        }
        return chapterList;
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
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.get("status").equals("success")) {

                UpdateRanobe(jsonObject.getJSONObject("response"),
                        Constans.JsonObjectFrom.RulateGetBookInfo);
            }
        } catch (JSONException e) {

        }

    }

    private void updateRanobeRfRanobe() {

        Document response = JsonRanobeRfApi.getInstance().GetBookInfo(getUrl());

        UpdateRanobe(response, Constans.JsonObjectFrom.RanobeRfGetBookInfo);

    }

    public void updateRanobe(Context mContext) {
        if (getRanobeSite().equals(StringResources.Rulate_Site)) {
            updateRulateRanobe(mContext);
        } else if (getRanobeSite().equals(StringResources.RanobeRf_Site)) {
            updateRanobeRfRanobe();
        }

    }

}
