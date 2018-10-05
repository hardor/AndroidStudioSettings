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
    @ColumnInfo(name = "Text")
    var text: String
    @ColumnInfo(name = "Index")
    var index: Int = 0

    constructor(@NonNull chapterUrl: String, text: String, chapterName: String, ranobeName: String, index: Int) {
        this.chapterUrl = chapterUrl
        this.text = text
        this.chapterName = chapterName
        this.ranobeName = ranobeName
        this.index = index
    }

    constructor(chapter: Chapter) {
        this.chapterUrl = chapter.url
        this.text = chapter.text.toString()
        this.chapterName = chapter.title
        this.ranobeName = chapter.ranobeName
        this.index = chapter.index
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextChapter

        if (chapterUrl != other.chapterUrl) return false

        return true
    }

    override fun hashCode(): Int {
        return chapterUrl.hashCode()
    }

}
