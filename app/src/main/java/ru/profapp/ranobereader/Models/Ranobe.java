package ru.profapp.ranobereader.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Ruslan on 09.02.2018.
 */
@Entity
public class Ranobe {
    @PrimaryKey
    @NonNull
    public String UrlToRanobe;

    public String RanobeName;
    public String RanobeDescripton;
    public String LogoImageUrl;

    public String RanobeSite;
    public Date UpdateTime;
    public Boolean IsFavorited;
    public Boolean IsFavoritedInWeb;
    public int LastReadedCharpter;
    public int CharpterCount;
    public int BookId;
    public Boolean WasUpdated;

}
