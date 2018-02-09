package ru.profapp.ranobereader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Ruslan on 09.02.2018.
 */

@Entity(foreignKeys = @ForeignKey(
        entity = Ranobe.class,
        parentColumns = "UrlToRanobe",
        childColumns = "RanobeUrl"))
public class Chapter {
    @PrimaryKey
    @NonNull
    public String CharpterName;
    @NonNull
    public String RanobeUrl;

    public String CharpterUrl;

    public int CharpterIndex;

    public Date CharpterTime;

    public int CharpterId;

    public int BookId;

    public Boolean CanRead = true;

    public Boolean IsNew;

    public String Status;

}