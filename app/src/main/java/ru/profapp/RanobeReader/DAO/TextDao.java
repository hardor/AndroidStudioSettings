package ru.profapp.RanobeReader.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ru.profapp.RanobeReader.Models.TextChapter;

/**
 * Created by Ruslan on 21.02.2018.
 */
@Dao
public interface TextDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(TextChapter text);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
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

    @Query("SELECT * FROM textChapter order by RanobeName Asc, `Index` ASC")
    List<TextChapter> getAllText();
}
