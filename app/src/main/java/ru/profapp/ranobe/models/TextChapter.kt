package ru.profapp.ranobe.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by Ruslan on 21.02.2018.
 */
@Entity(tableName = "textChapter", indices = [Index(value = ["ChapterUrl"])])
class TextChapter() {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "ChapterUrl")
    var chapterUrl: String = ""
    @ColumnInfo(name = "ChapterName")
    var chapterName: String = ""
    @ColumnInfo(name = "RanobeName")
    var ranobeName: String = ""
    @ColumnInfo(name = "RanobeUrl")
    var ranobeUrl: String = ""
    @ColumnInfo(name = "Text")
    var text: String = ""
    @ColumnInfo(name = "ChapterIndex")
    var chapterIndex: Int = 0

    constructor(chapter: Chapter) : this() {
        this.chapterUrl = chapter.url
        this.text = chapter.text ?: ""
        this.chapterName = chapter.title.replace("^\\s+", "")
        this.ranobeName = chapter.ranobeName
        this.ranobeUrl = chapter.ranobeUrl
        this.chapterIndex = chapter.index
    }

}
