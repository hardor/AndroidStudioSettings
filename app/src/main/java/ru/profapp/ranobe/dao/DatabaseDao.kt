package ru.profapp.ranobe.dao

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.profapp.ranobe.models.*

@androidx.room.Database(entities = [Ranobe::class, Chapter::class, TextChapter::class, RanobeImage::class, ChapterHistory::class, RanobeHistory::class, ChapterProgress::class],
    version = 5)
@TypeConverters(DateConverter::class)
abstract class DatabaseDao : RoomDatabase() {

    abstract fun ranobeDao(): RanobeDao

    abstract fun chapterDao(): ChapterDao

    abstract fun textDao(): TextDao

    abstract fun ranobeImageDao(): RanobeImageDao

    abstract fun ranobeHistoryDao(): RanobeHistoryDao

    abstract fun chapterProgressDao(): ChapterProgressDao

}

