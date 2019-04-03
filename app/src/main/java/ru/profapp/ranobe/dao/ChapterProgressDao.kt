package ru.profapp.ranobe.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Maybe
import ru.profapp.ranobe.models.ChapterProgress

/**
 * Created by Ruslan on 21.02.2018.
 */
@Dao
interface ChapterProgressDao {
    @Query("SELECT * FROM chapterProgress where ranobeUrl=:ranobeUrl ORDER BY ReadDate DESC  LIMIT 1 ")
    fun getLastChapterByRanobeUrl(ranobeUrl: String): Maybe<ChapterProgress>

    @Query("SELECT * FROM chapterProgress ORDER BY ReadDate DESC LIMIT 1")
    fun getLastChapter(): Maybe<ChapterProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chapterProgress: ChapterProgress)

    @Query("DELETE FROM chapterProgress")
    fun cleanTable()

}

