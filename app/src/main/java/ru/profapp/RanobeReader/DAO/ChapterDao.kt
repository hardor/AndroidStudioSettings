package ru.profapp.RanobeReader.DAO

import androidx.room.*
import ru.profapp.RanobeReader.Models.Chapter

/**
 * Created by Ruslan on 09.02.2018.
 */

@Dao
interface ChapterDao {


    @Query("SELECT * FROM chapter")
    fun allChapters(): List<Chapter>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chapter: Chapter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg chapters: Chapter)

    @Update
    fun updateAll(vararg chapters: Chapter)

    @Update
    fun update(chapter: Chapter)

    @Delete
    fun delete(chapter: Chapter)

    @Query("SELECT * FROM chapter WHERE Url = :ChapterUrl")
    fun getByChapterUrl(ChapterUrl: String): Chapter

    @Query("SELECT * FROM chapter WHERE RanobeUrl IS :RanobeUrl order by chapter.`Index` Asc")
    fun getChaptersForRanobe(RanobeUrl: String): List<Chapter>

}
