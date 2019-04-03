package ru.profapp.ranobe.dao

import androidx.room.*
import io.reactivex.Single
import ru.profapp.ranobe.models.Chapter

/**
 * Created by Ruslan on 09.02.2018.
 */

@Dao
interface ChapterDao {

    @Query("SELECT * FROM chapter")
    fun allChapters(): Single<List<Chapter>>

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
    fun getChaptersForRanobe(RanobeUrl: String): Single<List<Chapter>>

}
