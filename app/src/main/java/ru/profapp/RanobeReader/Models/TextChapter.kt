package ru.profapp.RanobeReader.Models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by Ruslan on 21.02.2018.
 */
@Entity(tableName = "textChapter", indices = [Index(value = ["ChapterUrl"])])
class TextChapter {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "ChapterUrl")
    var chapterUrl: String
    @ColumnInfo(name = "ChapterName")
    var chapterName: String
    @ColumnInfo(name = "RanobeName")
    var ranobeName: String
    @ColumnInfo(name = "RanobeUrl")
    var ranobeUrl: String = ""
    @ColumnInfo(name = "Text")
    var text: String
    @ColumnInfo(name = "Index")
    var index: Int = 0

    constructor(@NonNull chapterUrl: String, text: String, chapterName: String, ranobeName: String, ranobeUrl: String, index: Int) {
        this.chapterUrl = chapterUrl
        this.text = text
        this.chapterName = chapterName
        this.ranobeName = ranobeName
        this.ranobeUrl = ranobeUrl
        this.index = index
    }

    constructor(chapter: Chapter) {
        this.chapterUrl = chapter.url
        this.text = chapter.text.toString()
        this.chapterName = chapter.title
        this.ranobeName = chapter.ranobeName
        this.index = chapter.index
        this.ranobeUrl = chapter.ranobeUrl
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextChapter

        if (chapterUrl != other.chapterUrl) return false
        if (chapterName != other.chapterName) return false
        if (ranobeName != other.ranobeName) return false
        if (ranobeUrl != other.ranobeUrl) return false
        if (text != other.text) return false
        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chapterUrl.hashCode()
        result = 31 * result + chapterName.hashCode()
        result = 31 * result + ranobeName.hashCode()
        result = 31 * result + ranobeUrl.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + index
        return result
    }

}
