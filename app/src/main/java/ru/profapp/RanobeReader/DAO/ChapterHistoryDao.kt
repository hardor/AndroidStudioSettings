package ru.profapp.RanobeReader.DAO

import androidx.room.*
import io.reactivex.Single
import ru.profapp.RanobeReader.Models.ChapterHistory


/**
 * Created by Ruslan on 21.02.2018.
 */
@Dao
interface ChapterHistoryDao {

    @Query("SELECT * FROM chapterHistory WHERE ChapterUrl=:UrlToChapter")
    fun getChapterHistoryByUrl(UrlToChapter: String): Single<ChapterHistory>

    @Query("SELECT * FROM chapterHistory order by ReadDate desc")
    fun allChapters(): Single<List<ChapterHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chapter: ChapterHistory)

    @Delete
    fun delete(chapter: ChapterHistory)


    @Query("DELETE FROM chapterHistory WHERE ChapterUrl NOT IN (SELECT ChapterUrl FROM chapterHistory ORDER BY ReadDate DESC LIMIT 99)")
    fun deleteLast()

    @Transaction
    fun insertAndDeleteLast(chapter: ChapterHistory) {
        deleteLast()
        insert(chapter)
    }

    @Query("DELETE FROM chapterHistory")
    fun cleanTable()
}
