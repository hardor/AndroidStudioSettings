package ru.profapp.RanobeReader.Models

import androidx.annotation.NonNull
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import org.json.JSONObject
import ru.profapp.RanobeReader.Common.RanobeConstants
import ru.profapp.RanobeReader.Common.RanobeConstants.RanobeSite.RanobeRf
import ru.profapp.RanobeReader.Common.RanobeConstants.RanobeSite.Rulate
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
    @ColumnInfo(name = "Title")
    var title: String=""
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
    var ranobeId: Int = -1
        get() {
            if (field == -1 && ranobeUrl.isNotBlank()) {
                try {
                    return Integer.parseInt(ranobeUrl.substring(ranobeUrl.lastIndexOf("/") + 1))
                } catch (ignore: NumberFormatException) {
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
    var ranobeName: String=""
    @Ignore
    var isChecked: Boolean = false
    @ColumnInfo(name = "id")
    var id: Int = -1
        get() {
            if (field == -1) {
                try {
                    return Integer.parseInt(url.substring(url.lastIndexOf("/") + 1))
                } catch (ignore: NumberFormatException) {
                }
            }
            return field
        }


    constructor(`object`: JSONObject, enumFrom: RanobeConstants.JsonObjectFrom) : this() {
        when (enumFrom) {
            RanobeConstants.JsonObjectFrom.RanobeRfGetReady -> {
                title = `object`.optString("number") + ": " + `object`.optString("title")
                url = `object`.optString("alias")
            }
            RanobeConstants.JsonObjectFrom.RanobeRfSearch -> {
                title = `object`.optString("title")
                url = `object`.optString("link")
            }
        }

    }

    constructor(rChapter: RulateChapter) : this() {
        id = rChapter.id!!
        title = rChapter.title.toString()
        status = rChapter.status
        canRead = rChapter.canRead!!
        isNew = rChapter.new!!
    }


    constructor(rChapter: tChapter) : this() {
        id = rChapter.id
        title = rChapter.name
        url = "$ranobeUrl/ranobe/${rChapter.idRanobe}/${rChapter.numVolume}/${rChapter.num}"
        ranobeId = ranobeId
    }


    fun UpdateChapter(response: RulateText) {

        title = response.title.toString()
        text = response.text

    }

    fun UpdateChapter(response: RfText) {

        if (response.status == 200) {
            title = response.part!!.title.toString()
            text = response.part.content
            url = response.part.url!!
        }
    }


}