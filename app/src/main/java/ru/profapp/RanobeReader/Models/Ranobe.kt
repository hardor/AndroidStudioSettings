package ru.profapp.RanobeReader.Models

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.reactivex.Single
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Common.Constants.RanobeSite.*
import ru.profapp.RanobeReader.Common.StringResources
import ru.profapp.RanobeReader.JsonApi.RanobeHubRepository
import ru.profapp.RanobeReader.JsonApi.RanobeRfRepository
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateComment
import ru.profapp.RanobeReader.JsonApi.RulateRepository
import java.util.*

/**
 * Created by Ruslan on 09.02.2018.
 */
@Entity(tableName = "ranobe")
class Ranobe() {

    constructor(ranobeSiteEnum: Constants.RanobeSite) : this() {
        ranobeSite = ranobeSiteEnum.url
    }

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "Url")
    var url: String = ""
    @ColumnInfo(name = "Id")
    var id: Int? = null
        get() {
            if (field != null) {
                return field
            } else if (url.isNotBlank()) {
                return try {
                    when (ranobeSite) {
                        Rulate.url -> url.replace("${Rulate.url}/book/", "").toInt()
                        RanobeHub.url -> url.replace("${Constants.RanobeSite.RanobeHub.url}/ranobe/", "").toInt()
                        else -> field
                    }

                } catch (ignore: NumberFormatException) {
                    return field
                }
            }
            return field
        }
    @ColumnInfo(name = "EngTitle")
    var engTitle: String? = null
    @ColumnInfo(name = "Title")
    var title: String = ""
    @ColumnInfo(name = "Image")
    var image: String? = null
    @ColumnInfo(name = "ReadyDate")
    var readyDate: Date? = null
    @ColumnInfo(name = "Lang")
    var lang: String? = null
    @ColumnInfo(name = "Description")
    var description: String? = null
    @ColumnInfo(name = "AdditionalInfo")
    var additionalInfo: String? = null
    @ColumnInfo(name = "RanobeSite")
    var ranobeSite: String = ""
        get() {
            return when {
                field.isNotEmpty() -> field
                url.contains(Rulate.url) -> Rulate.url
                url.contains(RanobeRf.url) -> RanobeRf.url
                url.contains(RanobeHub.url) -> RanobeHub.url
                url.contains(Title.url) -> Title.url
                else -> None.url
            }
        }
    @ColumnInfo(name = "ChapterCount")
    var chapterCount: Int? = null

    @ColumnInfo(name = "LastReadChapter")
    var lastReadChapter: Int? = null
    @ColumnInfo(name = "WasUpdated")
    var wasUpdated: Boolean = false
    @ColumnInfo(name = "IsFavorite")
    var isFavorite: Boolean = false
    @ColumnInfo(name = "IsFavoriteInWeb")
    var isFavoriteInWeb: Boolean = false
    @ColumnInfo(name = "Rating")
    var rating: String? = null
    @ColumnInfo(name = "Status")
    var status: String? = null

    @Ignore
    var genres: String? = null

    @Ignore
    var chapterList: MutableList<Chapter> = ArrayList()

    @Ignore
    var comments: List<RulateComment> = ArrayList()

    @Ignore
    var bookmarkIdRf: Int = 0

    fun updateRanobe(mContext: Context): Single<Ranobe> {

        if (!wasUpdated) {
            if (ranobeSite == Rulate.url || url.contains(Rulate.url)) {
                val mPreferences = mContext.getSharedPreferences(
                        StringResources.Rulate_Login_Pref, 0)
                val token = mPreferences.getString(StringResources.KEY_Token, "") ?: ""
                return RulateRepository.getBookInfo(this, token, id)
            } else if (ranobeSite == RanobeRf.url || url.contains(RanobeRf.url)) {

                return RanobeRfRepository.getBookInfo(this)
            } else if (ranobeSite == RanobeHub.url || url.contains(RanobeHub.url)) {

                return RanobeHubRepository.getBookInfo(this)
            } else if (ranobeSite != Title.url) {
                throw NullPointerException()
            }
        }
        return Single.create { this }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ranobe

        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    constructor(ranobeUrl: String) : this()


}
