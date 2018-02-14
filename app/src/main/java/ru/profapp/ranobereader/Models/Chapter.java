package ru.profapp.ranobereader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Ruslan on 09.02.2018.
 */

@Entity(foreignKeys = @ForeignKey(
        entity = Ranobe.class,
        parentColumns = "RanobeUrl",
        childColumns = "RanobeUrl"))
public class Chapter implements Parcelable {
    @Ignore
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


    public String CharterUrl;
    public int ChapterIndex;
    public Date ChapterTime;
    public String ChapterString;
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

    @Ignore
    protected Chapter(Parcel in) {
        Id = in.readInt();
        Title = in.readString();
        Status = in.readString();


        RanobeUrl = in.readString();
        CharterUrl = in.readString();
        ChapterIndex = in.readInt();

        ChapterString = in.readString();
        RanobeId = in.readInt();
        byte tmpCanRead = in.readByte();
        CanRead = tmpCanRead == 0 ? null : tmpCanRead == 1;
        byte tmpIsNew = in.readByte();
        New = tmpIsNew == 0 ? null : tmpIsNew == 1;
        byte tmpIsDownloaded = in.readByte();
        IsDownloaded = tmpIsDownloaded == 0 ? null : tmpIsDownloaded == 1;

    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Title);
        parcel.writeString(RanobeUrl);
        parcel.writeString(CharterUrl);
        parcel.writeInt(ChapterIndex);
        parcel.writeInt(Id);
        parcel.writeString(ChapterString);
        parcel.writeInt(RanobeId);
        parcel.writeByte((byte) (CanRead == null ? 0 : CanRead ? 1 : 2));
        parcel.writeByte((byte) (New == null ? 0 : New ? 1 : 2));
        parcel.writeByte((byte) (IsDownloaded == null ? 0 : IsDownloaded ? 1 : 2));
        parcel.writeString(Status);
    }
}