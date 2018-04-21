package ru.profapp.RanobeReader.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ru.profapp.RanobeReader.Models.Ranobe;
import ru.profapp.RanobeReader.Models.TextChapter;

/**
 * Created by Ruslan on 21.02.2018.
 */
@Dao
public interface TextDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TextChapter text);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(TextChapter... texts);

    @Update
    void updateAll(TextChapter... texts);

    @Update
    void update(TextChapter text);

    @Delete
    void delete(TextChapter text);

    @Query("Delete FROM textChapter where ChapterUrl = :ChapterUrl")
    void delete(String ChapterUrl);


    @Query("SELECT * FROM textChapter WHERE ChapterUrl = :ChapterUrl")
    TextChapter getTextByChapterUrl(String ChapterUrl);

    @Query("DELETE FROM textChapter")
    void cleanTable();

    @Query("SELECT * FROM textChapter order by RanobeName, `Index`")
    List<TextChapter> getAllText();
}
