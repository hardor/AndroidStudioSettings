package ru.profapp.RanobeReader.Models

import androidx.annotation.NonNull
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import ru.profapp.RanobeReader.Common.Constants.RanobeSite.RanobeRf
import ru.profapp.RanobeReader.Common.Constants.RanobeSite.Rulate
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
                try {
                    return Integer.parseInt(url.substring(url.lastIndexOf("/") + 1))
                } catch (ignore: NumberFormatException) {
                    return field
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
                try {
                    return Integer.parseInt(ranobeUrl.substring(ranobeUrl.lastIndexOf("/") + 1))
                } catch (ignore: NumberFormatException) {
                    return field
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
    }

    constructor(chapterHistory: ChapterHistory) : this() {
        ranobeName = chapterHistory.ranobeName
        title = chapterHistory.chapterName
        url = chapterHistory.chapterUrl
        ranobeUrl = chapterHistory.ranobeUrl
        index = chapterHistory.index
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chapter

        if (url != other.url) return false
        if (title != other.title) return false
        if (status != other.status) return false
        if (canRead != other.canRead) return false
        if (isNew != other.isNew) return false
        if (index != other.index) return false
        if (time != other.time) return false
        if (downloaded != other.downloaded) return false
        if (isRead != other.isRead) return false
        if (text != other.text) return false
        if (ranobeName != other.ranobeName) return false
        if (isChecked != other.isChecked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + canRead.hashCode()
        result = 31 * result + isNew.hashCode()
        result = 31 * result + index
        result = 31 * result + (time?.hashCode() ?: 0)
        result = 31 * result + downloaded.hashCode()
        result = 31 * result + isRead.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + ranobeName.hashCode()
        result = 31 * result + isChecked.hashCode()
        return result
    }

}