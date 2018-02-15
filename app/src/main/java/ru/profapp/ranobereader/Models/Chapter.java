package ru.profapp.ranobereader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ru.profapp.ranobereader.Common.Constans;

/**
 * Created by Ruslan on 09.02.2018.
 */

@Entity(foreignKeys = @ForeignKey(
        entity = Ranobe.class,
        parentColumns = "RanobeUrl",
        childColumns = "RanobeUrl"))
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
    public String RanobeUrl;
    public int Id;
    public String Title;
    public String Status;
    public Boolean CanRead;
    public Boolean New;
    public String Url;
    public int Index;
    public Date Time;
    public String Text;
    public int RanobeId;
    public Boolean IsDownloaded;


    public Chapter() {

    }


    @Ignore
    public Chapter(JSONObject object) {


        Id = object.optInt("id");
        Title = object.optString("title");
        Status = object.optString("status");
        CanRead = object.optBoolean("can_read");
        New = object.optBoolean("new");


    }

    protected Chapter(Parcel in) {
        RanobeUrl = in.readString();
        Id = in.readInt();
        Title = in.readString();
        Status = in.readString();
        byte tmpCanRead = in.readByte();
        CanRead = tmpCanRead == 0 ? null : tmpCanRead == 1;
        byte tmpNew = in.readByte();
        New = tmpNew == 0 ? null : tmpNew == 1;
        Url = in.readString();
        Index = in.readInt();
        Text = in.readString();
        RanobeId = in.readInt();
        byte tmpIsDownloaded = in.readByte();
        IsDownloaded = tmpIsDownloaded == 0 ? null : tmpIsDownloaded == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(RanobeUrl);
        dest.writeInt(Id);
        dest.writeString(Title);
        dest.writeString(Status);
        dest.writeByte((byte) (CanRead == null ? 0 : CanRead ? 1 : 2));
        dest.writeByte((byte) (New == null ? 0 : New ? 1 : 2));
        dest.writeString(Url);
        dest.writeInt(Index);
        dest.writeString(Text);
        dest.writeInt(RanobeId);
        dest.writeByte((byte) (IsDownloaded == null ? 0 : IsDownloaded ? 1 : 2));
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
}