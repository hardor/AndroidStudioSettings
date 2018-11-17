package ru.profapp.RanobeReader.Models

import androidx.annotation.NonNull
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import ru.profapp.RanobeReader.Common.Constants.RanobeSite.*
import java.util.*

/**
 * Created by Ruslan on 09.02.2018.
 */

@Entity(tableName = "chapter",
        foreignKeys = [ForeignKey(entity = Ranobe::class, parentColumns = arrayOf("Url"),
                childColumns = arrayOf("RanobeUrl"), onDelete = CASCADE)],
        indices = [Index(value = ["RanobeUrl"])])
class Chapter() {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "Url")
    var url: String = ""
    @ColumnInfo(name = "RanobeUrl")
    var ranobeUrl: String = ""
        get() {

            if (!field.isEmpty()) return field
            try {
                if (url.contains(RanobeRf.url)) {
                    val a = url.lastIndexOf("/glava-")
                    val b = url.lastIndexOf("/noindex-")
                    if (Math.max(a, b) != -1) {
                        return url.substring(0, Math.max(a, b))
                    }
                } else if (url.contains(Rulate.url)) {
                    return url.substring(0, url.lastIndexOf("/"))
                }
            } catch (ignore: Exception) {
                ignore.printStackTrace()
            }


            return field
        }
    @ColumnInfo(name = "Id")
    var id: Int? = null
        get() {
            if (field == null) {
                val value: String =
                        if (url.contains(RanobeHub.url)) {
                            val arr = url.replace(ranobeUrl, "").split("/").takeLast(2)
                            (arr[1].toInt() + (arr[0].toInt() * 1000)).toString()
                        } else {
                            url.substring(url.lastIndexOf("/") + 1)
                        }

                return try {
                    Integer.parseInt(value)
                } catch (ignore: NumberFormatException) {
                    field
                }
            }
            return field
        }
    @ColumnInfo(name = "Title")
    var title: String = ""
    @ColumnInfo(name = "Status")
    var status: String? = null
    @ColumnInfo(name = "CanRead")
    var canRead: Boolean = true
    @ColumnInfo(name = "New")
    var isNew: Boolean = false
    @ColumnInfo(name = "Index")
    var index: Int = 0
    @ColumnInfo(name = "Time")
    var time: Date? = null
    @ColumnInfo(name = "RanobeId")
    var ranobeId: Int? = null
        get() {
            if (field == null && ranobeUrl.isNotBlank()) {
                return try {
                    Integer.parseInt(ranobeUrl.substring(ranobeUrl.lastIndexOf("/") + 1))
                } catch (ignore: NumberFormatException) {
                    field
                }

            }
            return field
        }
    @ColumnInfo(name = "Downloaded")
    var downloaded: Boolean = false
    @ColumnInfo(name = "IsRead")
    var isRead: Boolean = false
    @Ignore
    @ColumnInfo(name = "Text")
    var text: String? = null

    @ColumnInfo(name = "RanobeName")
    var ranobeName: String = ""
    @Ignore
    var isChecked: Boolean = false

    constructor(textChapter: TextChapter) : this() {
        ranobeName = textChapter.ranobeName
        title = textChapter.chapterName
        text = textChapter.text
        url = textChapter.chapterUrl
        ranobeUrl = textChapter.ranobeUrl
    }

    constructor(chapterHistory: ChapterHistory) : this() {
        ranobeName = chapterHistory.ranobeName
        title = chapterHistory.chapterName
        url = chapterHistory.chapterUrl
        ranobeUrl = chapterHistory.ranobeUrl
        index = chapterHistory.index
    }
}