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
@Entity(tableName = "chapterHistory", indices = [Index(value = ["ChapterUrl"])])
class ChapterHistory(@PrimaryKey
                     @NonNull
                     @ColumnInfo(name = "ChapterUrl") var chapterUrl: String, @ColumnInfo(name = "ChapterName") var chapterName: String, @ColumnInfo(name = "RanobeName") var ranobeName: String, @ColumnInfo(name = "RanobeUrl") var ranobeUrl: String, @ColumnInfo(name = "Index") var index: Int) {

    @ColumnInfo(name = "ReadDate")
    var readDate: Date = Date()

}

