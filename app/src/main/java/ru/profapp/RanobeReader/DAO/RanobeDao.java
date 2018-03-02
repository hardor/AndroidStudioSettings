package ru.profapp.RanobeReader.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ru.profapp.RanobeReader.Models.Ranobe;

/**
 * Created by Ruslan on 09.02.2018.
 */

@Dao
public interface RanobeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ranobe ranobe);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Ranobe... ranobes);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAll(Ranobe... ranobes);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Ranobe ranobe);

    @Delete
    void delete(Ranobe ranobe);

    @Query("SELECT * FROM ranobe WHERE Url=:UrlToRanobe")
    Ranobe getRanobeByUrl(String UrlToRanobe);

    @Query("SELECT * FROM ranobe")
    List<Ranobe> getAllRanobe();

    @Query("SELECT * FROM ranobe WHERE Favorited = 1")
    List<Ranobe> GetFavoriteRanobes();

    @Query("SELECT * FROM ranobe WHERE Favorited = 1 AND Url=:UrlToRanobe LIMIT 1")
    Ranobe IsRanobeFavorite(String UrlToRanobe);

}
