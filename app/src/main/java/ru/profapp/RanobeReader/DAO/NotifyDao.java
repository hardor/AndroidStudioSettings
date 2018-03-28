package ru.profapp.RanobeReader.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ru.profapp.RanobeReader.Models.Notify;

/**
 * Created by Ruslan on 21.02.2018.
 */
@Dao
public interface NotifyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notify notify);

    @Update
    void update(Notify notify);

    @Delete
    void delete(Notify notify);

    @Query("SELECT * FROM notify")
    List<Notify> getAllNotify();

    @Query("DELETE FROM notify")
    void cleanTable();
}
