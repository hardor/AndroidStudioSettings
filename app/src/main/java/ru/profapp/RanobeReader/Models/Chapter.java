package ru.profapp.RanobeReader.Models;

import static android.arch.persistence.room.ForeignKey.CASCADE;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.DAO.DatabaseDao;

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
public class Chapter {

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
    private Boolean Readed;
    private String Text;

    public Chapter() {

    }


    @Ignore
    public Chapter(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {
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
            case RanobeRfSearch:
                Title = object.optString("title");
                Url = object.optString("link");
                break;
        }

    }

    @Ignore
    public void UpdateChapter(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom,
            Context mContext) {

        switch (enumFrom) {
            case RulateGetChapterText:
                fromRulateGetChapterText(object, mContext);
                break;
            case RanobeRfGetChapterText:
                fromRanobeRfGetChapterText(object, mContext);
                break;
            default:
//                throw new NullPointerException();
                break;
        }

    }

    private void fromRanobeRfGetChapterText(JSONObject object, Context mContext) {
        try {
            JSONObject part = object.getJSONObject("part");
            Title = part.getString("title");
            Text = part.getString("content");
            new Thread() {
                @Override
                public void run() {
                    DatabaseDao.getInstance(mContext).getTextDao().insert(
                            new TextChapter(getUrl(), Text));
                }

            }.start();
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void fromRulateGetChapterText(JSONObject object, Context mContext) {
        try {
            Title = object.getString("title");
            Text = object.getString("text");

            new Thread() {
                @Override
                public void run() {
                    DatabaseDao.getInstance(mContext).getTextDao().insert(
                            new TextChapter(getUrl(), Text));
                }

            }.start();

        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
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
        return New == null ? false : New;
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
        return Downloaded == null ? false : Downloaded;
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

    public Boolean getReaded() {
        return Readed == null ? false : Readed;
    }

    public void setReaded(Boolean readed) {
        Readed = readed;
    }
}