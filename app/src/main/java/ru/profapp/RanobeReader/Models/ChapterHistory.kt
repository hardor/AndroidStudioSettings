package ru.profapp.RanobeReader.Models

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
class ChapterHistory {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "ChapterUrl")
    var chapterUrl: String
    @ColumnInfo(name = "ChapterName")
    var chapterName: String
    @ColumnInfo(name = "RanobeName")
    var ranobeName: String
    @ColumnInfo(name = "RanobeUrl")
    var ranobeUrl: String
    @ColumnInfo(name = "Index")
    var index: Int = 0
    @ColumnInfo(name = "ReadDate")
    var readDate: Date = Date()
    @ColumnInfo(name = "Progress")
    var progress: Float = 0F

    constructor(chapterUrl: String, chapterName: String, ranobeName: String, ranobeUrl: String, index: Int, progress: Float) {
        this.chapterUrl = chapterUrl
        this.chapterName = chapterName
        this.ranobeName = ranobeName
        this.ranobeUrl = ranobeUrl
        this.index = index
        this.progress = progress
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChapterHistory

        if (chapterUrl != other.chapterUrl) return false
        if (chapterName != other.chapterName) return false
        if (ranobeName != other.ranobeName) return false
        if (ranobeUrl != other.ranobeUrl) return false
        if (index != other.index) return false
        if (readDate != other.readDate) return false
        if (progress != other.progress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chapterUrl.hashCode()
        result = 31 * result + chapterName.hashCode()
        result = 31 * result + ranobeName.hashCode()
        result = 31 * result + ranobeUrl.hashCode()
        result = 31 * result + index
        result = 31 * result + readDate.hashCode()
        result = 31 * result + progress.hashCode()
        return result
    }

}

