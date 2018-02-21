package ru.profapp.ranobereader.Models;

import static android.arch.persistence.room.ForeignKey.CASCADE;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ru.profapp.ranobereader.Common.Constans;
import ru.profapp.ranobereader.DAO.Database;

/**
 * Created by Ruslan on 09.02.2018.
 */

@Entity(tableName = "chapter",
        foreignKeys = @ForeignKey(
                entity = Ranobe.class,
                parentColumns = "Url",
                childColumns = "RanobeUrl",
                onDelete = CASCADE),
        indices = @Index(value = "RanobeUrl"))
public class Chapter implements Parcelable {

    public static final Creator<Chapter> CREATOR = new Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };
    @NonNull
    @PrimaryKey
    private String Url;
    private String RanobeUrl;
    private int Id;
    private String Title;
    private String Status;
    private Boolean CanRead;
    private Boolean New;
    private int Index;
    private Date Time = new Date();
    private int RanobeId;
    private Boolean Downloaded;
    private String Text;

    public Chapter() {

    }

    @Ignore
    public Chapter(JSONObject object, Constans.JsonObjectFrom enumFrom) {
        switch (enumFrom) {
            case RulateGetBookInfo:
                Id = object.optInt("id");
                Title = object.optString("title");
                Status = object.optString("status");
                CanRead = object.optBoolean("can_read");
                New = object.optBoolean("new");
                break;
            case RanobeRfGetReady:
                Title = object.optString("number") + ": " + object.optString("title");
                Url = object.optString("alias");
                break;
        }

    }

    protected Chapter(Parcel in) {
        Url = in.readString();
        RanobeUrl = in.readString();
        Id = in.readInt();
        Title = in.readString();
        Status = in.readString();
        byte tmpCanRead = in.readByte();
        CanRead = tmpCanRead == 0 ? null : tmpCanRead == 1;
        byte tmpNew = in.readByte();
        New = tmpNew == 0 ? null : tmpNew == 1;
        Index = in.readInt();
        RanobeId = in.readInt();
        byte tmpIsDownloaded = in.readByte();
        Downloaded = tmpIsDownloaded == 0 ? null : tmpIsDownloaded == 1;
        Text = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Url);
        dest.writeString(RanobeUrl);
        dest.writeInt(Id);
        dest.writeString(Title);
        dest.writeString(Status);
        dest.writeByte((byte) (CanRead == null ? 0 : CanRead ? 1 : 2));
        dest.writeByte((byte) (New == null ? 0 : New ? 1 : 2));
        dest.writeInt(Index);
        dest.writeInt(RanobeId);
        dest.writeByte((byte) (Downloaded == null ? 0 : Downloaded ? 1 : 2));
        dest.writeString(Text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    public void UpdateChapter(JSONObject object, Constans.JsonObjectFrom enumFrom) {

        switch (enumFrom) {
            case RulateGetChapterText:
                fromRulateGetChapterText(object);
                break;
            default:
//                throw new NullPointerException();
                break;
        }

    }

    private void fromRulateGetChapterText(JSONObject object) {
        try {
            Title = object.getString("title");
            Text = object.getString("text");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public String getUrl() {
        return Url;
    }

    public void setUrl(@NonNull String url) {
        Url = url;
    }

    public String getRanobeUrl() {
        return RanobeUrl;
    }

    public void setRanobeUrl(String ranobeUrl) {
        RanobeUrl = ranobeUrl;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Boolean getCanRead() {
        return CanRead == null ? true : CanRead;
    }

    public void setCanRead(Boolean canRead) {
        CanRead = canRead;
    }

    public Boolean getNew() {
        return New;
    }

    public void setNew(Boolean aNew) {
        New = aNew;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }

    public Date getTime() {
        return Time;
    }

    public void setTime(Date time) {
        Time = time;
    }

    public int getRanobeId() {
        return RanobeId;
    }

    public void setRanobeId(int ranobeId) {
        RanobeId = ranobeId;
    }

    public Boolean getDownloaded() {
        return Downloaded;
    }

    public void setDownloaded(Boolean downloaded) {
        Downloaded = downloaded;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

}