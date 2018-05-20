package ru.profapp.RanobeReader.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ru.profapp.RanobeReader.Models.Chapter;

/**
 * Created by Ruslan on 09.02.2018.
 */

@Dao
public interface ChapterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Chapter chapter);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Chapter... chapters);

    @Update
    void updateAll(Chapter... chapters);
    @Update
    void update(Chapter chapter);

    @Delete
    void delete(Chapter chapter);

    @Query("SELECT * FROM chapter WHERE Url = :ChapterUrl")
    Chapter getByChapterUrl(String ChapterUrl);


    @Query("SELECT * FROM chapter")
    List<Chapter> getAllChapters();

    @Query("SELECT * FROM chapter WHERE RanobeUrl IS :RanobeUrl")
    List<Chapter> getChaptersForRanobe(String RanobeUrl);

}
