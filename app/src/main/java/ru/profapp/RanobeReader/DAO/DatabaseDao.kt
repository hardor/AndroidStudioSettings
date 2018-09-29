package ru.profapp.RanobeReader.DAO

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.profapp.RanobeReader.Models.*

@androidx.room.Database(entities = [Ranobe::class, Chapter::class, TextChapter::class, RanobeImage::class, ChapterHistory::class], version = 3)
@TypeConverters(DateConverter::class)
abstract class DatabaseDao : RoomDatabase() {

    abstract fun ranobeDao(): RanobeDao

    abstract fun chapterDao(): ChapterDao

    abstract fun textDao(): TextDao

    abstract fun ranobeImageDao(): RanobeImageDao

    abstract fun chapterHistoryDao(): ChapterHistoryDao

}

