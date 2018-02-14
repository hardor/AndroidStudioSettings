package ru.profapp.ranobereader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.profapp.ranobereader.Common.Constans;
import ru.profapp.ranobereader.Common.StringResources;

/**
 * Created by Ruslan on 09.02.2018.
 */
@Entity
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
    private String RanobeUrl;
    private int Id;
    private String EngTitle;
    private String Title;
    private String Image;
    private Date ReadyDate;
    private String Lang;
    private String Description;
    private String RanobeSite;
    private int CharpterCount;
    private int LastReadedCharpter;
    private Boolean WasUpdated;
    private Boolean Favorited;
    private Boolean FavoritedInWeb;
    private String Rating;
    private String Status;
    @Ignore
    private List<Chapter> chapterList;
    @Ignore
    private DateFormat format = new SimpleDateFormat("MM-dd HH-mm");


    public Ranobe() {
    }

    protected Ranobe(Parcel in) {
        RanobeUrl = in.readString();
        Id = in.readInt();
        EngTitle = in.readString();
        Title = in.readString();
        Image = in.readString();
        Lang = in.readString();
        Description = in.readString();
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

    @Ignore
    public void UpdateRanobe(JSONObject object, Constans.JsonObjectFrom enumFrom) {

        switch (enumFrom) {
            case RulateGetReady:
                fromRulateGetReady(object);
                break;
            case RulateGetBookInfo:
                fromRulateGetBookInfo(object);
                break;
            default:
//                throw new NullPointerException();
                break;
        }
        RanobeSite = StringResources.Rulate_Site;
        RanobeUrl = StringResources.Rulate_Site + "/book/" + Id;
        ;

    }

    private void fromRulateGetBookInfo(JSONObject object) {
        try {
            Id = object.optInt("id");
            EngTitle = object.getString("s_title");
            Title = object.getString("t_title");
            CharpterCount = object.optInt("n_chapters");
            Lang = object.getString("lang");

            //last_activity
            chapterList = new ArrayList<>();
            Status = object.getString("status");
            Rating = object.getString("rating");
            Image = object.getString("img");
            if (object.has("chapters")) {
                JSONArray jsonArray = object.optJSONArray("chapters");
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject value = jsonArray.optJSONObject(i);
                    Chapter chapter = new Chapter(value);
                    chapter.RanobeId = Id;
                    chapter.RanobeUrl = RanobeUrl;
                    chapterList.add(chapter);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fromRulateGetReady(JSONObject object) {
        try {
            Id = object.getInt("book_id");
            EngTitle = object.getString("s_title");
            Title = object.getString("t_title");
            Image = object.getString("img").replace("-5050", "");

            Lang = object.getString("lang");
            Rating = object.getString("rating");
            ReadyDate = format.parse(object.getString("ready_date"));
            chapterList = new ArrayList<>();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
            ReadyDate = new Date();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(RanobeUrl);
        dest.writeInt(Id);
        dest.writeString(EngTitle);
        dest.writeString(Title);
        dest.writeString(Image);
        dest.writeString(Lang);
        dest.writeString(Description);
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

    public String getRanobeUrl() {
        return RanobeUrl;
    }


    public void setRanobeUrl(@NonNull String ranobeUrl) {
        RanobeUrl = ranobeUrl;
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
        return WasUpdated;
    }

    public void setWasUpdated(Boolean wasUpdated) {
        WasUpdated = wasUpdated;
    }

    public Boolean getFavorited() {
        return Favorited;
    }

    public void setFavorited(Boolean favorited) {
        Favorited = favorited;
    }

    public Boolean getFavoritedInWeb() {
        return FavoritedInWeb;
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


}
