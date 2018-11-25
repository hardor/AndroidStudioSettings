package ru.profapp.ranobe.DAO

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.profapp.ranobe.Models.*

@androidx.room.Database(entities = [Ranobe::class, Chapter::class, TextChapter::class, RanobeImage::class, ChapterHistory::class, RanobeHistory::class], version = 3)
@TypeConverters(DateConverter::class)
abstract class DatabaseDao : RoomDatabase() {

    abstract fun ranobeDao(): RanobeDao

    abstract fun chapterDao(): ChapterDao

    abstract fun textDao(): TextDao

    abstract fun ranobeImageDao(): RanobeImageDao

    abstract fun ranobeHistoryDao(): RanobeHistoryDao

}

