package ru.profapp.ranobereader.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ru.profapp.ranobereader.Models.Ranobe;

/**
 * Created by Ruslan on 09.02.2018.
 */

@Dao
public interface RanobeDao {

    @Insert
    void insertAll(Ranobe... ranobes);

    @Update
    void update(Ranobe... ranobes);

    @Delete
    void delete(Ranobe ranobe);


    @Query("SELECT * FROM ranobe WHERE UrlToRanobe=:UrlToRanobe")
    Ranobe getRanobeByUrl(String UrlToRanobe);

    @Query("SELECT * FROM ranobe")
    List<Ranobe> getAllRanobe();

    @Query("SELECT * FROM ranobe WHERE IsFavorited = 1")
    List<Ranobe> GetFavoriteRanobes();


}
