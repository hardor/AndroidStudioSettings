package ru.profapp.RanobeReader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;

/**
 * Created by Ruslan on 21.02.2018.
 */
@Entity(tableName = "notify")
public class Notify {

    @PrimaryKey(autoGenerate = true)
    private int notify_id;
    private String Title;
    private String Message;
    private String ImageUrl;
    private Date date;

    public Notify() {
    }

    @Ignore
    public Notify(RemoteMessage remoteMessage) {
        setTitle(remoteMessage.getData().get("title"));
        setMessage(remoteMessage.getData().get("message"));
        setImageUrl(remoteMessage.getData().get("image-url"));
        setDate(new Date());
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNotify_id() {
        return notify_id;
    }

    public void setNotify_id(int notify_id) {
        this.notify_id = notify_id;
    }
}
