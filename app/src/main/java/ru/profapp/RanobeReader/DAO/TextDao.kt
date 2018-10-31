package ru.profapp.RanobeReader.DAO

import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single
import ru.profapp.RanobeReader.Models.TextChapter

/**
 * Created by Ruslan on 21.02.2018.
 */
@Dao
interface TextDao {

    @Query("SELECT * FROM textChapter order by RanobeName Asc")
    fun allText(): Single<List<TextChapter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(text: TextChapter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg texts: TextChapter)

    @Update
    fun updateAll(vararg texts: TextChapter)

    @Update
    fun update(text: TextChapter)

    @Delete
    fun delete(text: TextChapter)

    @Query("Delete FROM textChapter where ChapterUrl = :ChapterUrl")
    fun delete(ChapterUrl: String)

    @Query("SELECT * FROM textChapter WHERE ChapterUrl = :ChapterUrl")
    fun getTextByChapterUrl(ChapterUrl: String): Maybe<TextChapter>

    @Query("SELECT * FROM textChapter WHERE RanobeUrl = :RanobeUrl")
    fun getTextByRanobeUrl(RanobeUrl: String): Maybe<List<TextChapter>>

    @Query("DELETE FROM textChapter")
    fun cleanTable()
}
