package ru.profapp.RanobeReader.Models

import androidx.annotation.NonNull
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import org.json.JSONObject
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Common.Constants.RanobeSite.RanobeRf
import ru.profapp.RanobeReader.Common.Constants.RanobeSite.Rulate
import ru.profapp.RanobeReader.JsonApi.RanobeHub.tChapter
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfText
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateChapter
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateText
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


    constructor(`object`: JSONObject, enumFrom: Constants.JsonObjectFrom) : this() {
        when (enumFrom) {
            Constants.JsonObjectFrom.RanobeRfGetReady -> {
                title = `object`.optString("number") + ": " + `object`.optString("title")
                url = `object`.optString("alias")
            }
            Constants.JsonObjectFrom.RanobeRfSearch -> {
                title = `object`.optString("title")
                url = `object`.optString("link")
            }
        }

    }







    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chapter

        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    constructor(chapterUrl: String) : this()

}