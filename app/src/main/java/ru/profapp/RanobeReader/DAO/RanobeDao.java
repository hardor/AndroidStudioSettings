package ru.profapp.RanobeReader.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("DELETE FROM ranobe  WHERE Url=:UrlToRanobe AND FavoritedInWeb != 1")
    void delete(String UrlToRanobe);

    @Query("DELETE FROM ranobe  WHERE Url=:UrlToRanobe AND FavoritedInWeb = 1")
    void deleteWeb(String UrlToRanobe);
    @Delete
    void delete(Ranobe ranobe);

    @Query("SELECT * FROM ranobe WHERE Url=:UrlToRanobe")
    Ranobe getRanobeByUrl(String UrlToRanobe);

    @Query("SELECT * FROM ranobe")
    List<Ranobe> getAllRanobe();

    @Query("SELECT * FROM ranobe WHERE Favorited = 1")
    List<Ranobe> GetFavoriteRanobes();

    @Query("SELECT * FROM ranobe WHERE Favorited = 1 AND RanobeSite = :ranobeSite")
    List<Ranobe> GetFavoriteBySite(String ranobeSite);

    @Query("SELECT * FROM ranobe WHERE Favorited = 1 AND Url=:UrlToRanobe LIMIT 1")
    Ranobe IsRanobeFavorite(String UrlToRanobe);

    @Query("DELETE FROM ranobe")
    void cleanTable();
}
