package ru.profapp.ranobe.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

/**
 * Created by Ruslan on 21.02.2018.
 */
@Entity(tableName = "chapterProgress", indices = [Index(value = ["ChapterUrl"])])
class ChapterProgress(@PrimaryKey @NonNull @ColumnInfo(name = "ChapterUrl") var chapterUrl: String, @ColumnInfo(
    name = "RanobeUrl") var ranobeUrl: String, @ColumnInfo(name = "Progress") var progress: Float) {

    @ColumnInfo(name = "ReadDate")
    var readDate: Date = Date()

}

