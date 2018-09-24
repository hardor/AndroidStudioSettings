package ru.profapp.RanobeReader.DAO

import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.profapp.RanobeReader.Models.ChapterHistory
import ru.profapp.RanobeReader.Models.TextChapter
import androidx.room.Transaction


/**
 * Created by Ruslan on 21.02.2018.
 */
@Dao
interface ChapterHistoryDao {

    @Query("SELECT * FROM chapterHistory order by ReadDate  Asc")
    fun allChapters(): Single<List<ChapterHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chapter: ChapterHistory)

    @Delete
    fun delete(chapter: ChapterHistory)


    @Query("DELETE FROM chapterHistory WHERE ChapterUrl IN (SELECT ChapterUrl FROM chapterHistory ORDER BY ReadDate ASC LIMIT 100)")
    fun deleteLast()

    @Transaction
    fun insertAndDeleteLast(chapter: ChapterHistory) {
        insert(chapter)
        deleteLast()
    }

    @Query("DELETE FROM chapterHistory")
    fun cleanTable()
}
