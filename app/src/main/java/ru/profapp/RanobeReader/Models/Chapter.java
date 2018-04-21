package ru.profapp.RanobeReader.Models;

import static android.arch.persistence.room.ForeignKey.CASCADE;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.Date;

import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfChapter;
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfText;
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateChapter;
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateText;

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
    private Date Time = null;
    private int RanobeId;
    private Boolean Downloaded;
    private Boolean Readed;
    private String Text;
    private String RanobeName;

    public Chapter() {

    }

    public Chapter(JSONObject object, RanobeConstans.JsonObjectFrom enumFrom) {
        switch (enumFrom) {
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

    public Chapter(RulateChapter rChapter) {
        Id = rChapter.getId();
        Title = rChapter.getTitle();
        Status = rChapter.getStatus();
        CanRead = rChapter.getCanRead();
        New = rChapter.getNew();
    }

    public Chapter(RfChapter rChapter) {

        Id = Id == 0 ? (rChapter.getId() != null ? rChapter.getId() : Id) : Id;
        Title = empty(Title) ? (String.format("%s %s",
                rChapter.getNumber() != null ? rChapter.getNumber() : "",
                rChapter.getTitle())) : Title;
        Url = empty(Url) ? (rChapter.getAlias() != null ? rChapter.getAlias() : Url) : Url;
        Url = empty(Url) ? (rChapter.getUrl() != null ? rChapter.getUrl() : Url) : Url;
    }

    public static boolean empty(final String s) {
        // Null-safe, short-circuit evaluation.
        return s == null || s.trim().isEmpty();
    }

    @NonNull
    public String getUrl() {
        return Url;
    }

    public void setUrl(@NonNull String url) {
        Url = url;
    }

    public String getRanobeUrl() {
        if (RanobeUrl == null && getUrl() != null) {
            if (getUrl().contains(StringResources.RanobeRf_Site)) {
                RanobeUrl = getUrl().substring(0, getUrl().lastIndexOf("/glava-"));
            } else if (getUrl().contains(StringResources.Rulate_Site)) {
                RanobeUrl = getUrl().substring(0, getUrl().lastIndexOf("/"));
            }
        }
        return RanobeUrl;
    }

    public void setRanobeUrl(String ranobeUrl) {
        RanobeUrl = ranobeUrl;
    }

    public int getId() {
        if (Id == 0 && getUrl() != null) {
            Id = Integer.parseInt(
                    getUrl().substring(getUrl().lastIndexOf("/") + 1));
        }
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
        if (RanobeId == 0 && getRanobeUrl() != null) {
            try {
                RanobeId = Integer.parseInt(
                        getRanobeUrl().substring(getRanobeUrl().lastIndexOf("/") + 1));
            } catch (NumberFormatException ignore) {
            }

        }
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

    public void UpdateChapter(RulateText response, Context context, boolean isButton) {

        Title = response.getTitle();
        //Todo:
        //Text = StringHelper.getInstance().removeTags(response.getText());
        Text = response.getText();
        if (RanobeKeeper.getInstance().getAutoSaveText() || isButton) {
            new Thread() {
                @Override
                public void run() {
                    DatabaseDao.getInstance(context).getTextDao().insert(
                            new TextChapter(getUrl(), Text, getTitle(), getRanobeName(),
                                    getIndex()));
                }

            }.start();
        }
    }

    public void UpdateChapter(RfText response, Context context, boolean isButton) {

        if (response.getStatus() == 200) {
            Title = response.getPart().getTitle();
            //Todo:
            // Text =  StringHelper.getInstance().removeTags(response.getPart().getContent());
            Text = response.getPart().getContent();
            Url = response.getPart().getUrl();
            if (RanobeKeeper.getInstance().getAutoSaveText() || isButton) {
                new Thread() {
                    @Override
                    public void run() {
                        DatabaseDao.getInstance(context).getTextDao().insert(
                                new TextChapter(getUrl(), Text, getTitle(), getRanobeName(),
                                        getIndex()));
                    }

                }.start();
            }
        }
    }

    public String getRanobeName() {
        return RanobeName;
    }

    public void setRanobeName(String ranobeName) {
        RanobeName = ranobeName;
    }
}