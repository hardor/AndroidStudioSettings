package ru.profapp.RanobeReader.DAO

import androidx.room.*
import io.reactivex.Single
import ru.profapp.RanobeReader.Models.ChapterHistory
import ru.profapp.RanobeReader.Models.RanobeHistory

/**
 * Created by Ruslan on 21.02.2018.
 */
@Dao
interface RanobeHistoryDao {
    @Query("SELECT * FROM ranobeHistory order by ReadDate desc")
    fun allRanobes(): Single<List<RanobeHistory>>

    @Query("SELECT * FROM chapterHistory order by ReadDate desc")
    fun allChapters(): Single<List<ChapterHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ranobe: RanobeHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chapter: ChapterHistory)

    @Query("DELETE FROM ranobeHistory WHERE RanobeUrl NOT IN (SELECT RanobeUrl FROM ranobeHistory ORDER BY ReadDate DESC LIMIT 99)")
    fun deleteLastChapter()

    @Query("DELETE FROM chapterHistory WHERE ChapterUrl NOT IN (SELECT ChapterUrl FROM chapterHistory ORDER BY ReadDate DESC LIMIT 1000)")
    fun deleteLastRanobe()

    @Transaction
    fun insertNewRanobe(ranobe: RanobeHistory) {
        insert(ranobe)
        deleteLastRanobe()
    }

    @Transaction
    fun insertNewChapter(chapter: ChapterHistory) {
        insert(chapter)
        deleteLastChapter()
    }

    @Query("DELETE FROM ranobeHistory")
    fun cleanRanobes()

    @Query("DELETE FROM chapterHistory")
    fun cleanChapters()

    @Transaction
    fun cleanHistory(remove:Boolean=true) {
        cleanRanobes()
        cleanChapters()
    }

}

