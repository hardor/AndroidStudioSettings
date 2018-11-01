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
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.Network.DTO.RulateDTO.RulateComment
import ru.profapp.RanobeReader.Network.Repositories.RanobeHubRepository
import ru.profapp.RanobeReader.Network.Repositories.RanobeRfRepository
import ru.profapp.RanobeReader.Network.Repositories.RulateRepository
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
                    field
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
    @Ignore
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

    fun updateRanobe(mContext: Context): Single<Boolean> {

        val bookInfo: Single<Boolean>

        bookInfo = if (!wasUpdated) {
            if (ranobeSite == Rulate.url || url.contains(Rulate.url)) {
                val mPreferences = mContext.getSharedPreferences(Constants.Rulate_Login_Pref, 0)
                val token = mPreferences.getString(Constants.KEY_Token, "") ?: ""
                RulateRepository.getBookInfo(this, token, id)
            } else if (ranobeSite == RanobeRf.url || url.contains(RanobeRf.url)) {
                RanobeRfRepository.getBookInfo(this)
            } else if (ranobeSite == RanobeHub.url || url.contains(RanobeHub.url)) {
                RanobeHubRepository.getBookInfo(this)
            } else
                Single.just(false)

        } else {
            Single.just(true)
        }

        return bookInfo.map {
            if (!it) {
                val result = MyApp.database.ranobeDao().getRanobeWithChaptersByUrl(url).blockingGet()
                if (result != null) {
                    chapterList = result.chapterList
                }
            }
            return@map it
        }.doAfterSuccess {
            wasUpdated = it
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ranobe

        if (url != other.url) return false
        if (engTitle != other.engTitle) return false
        if (title != other.title) return false
        if (image != other.image) return false
        if (readyDate != other.readyDate) return false
        if (lang != other.lang) return false
        if (description != other.description) return false
        if (additionalInfo != other.additionalInfo) return false
        if (chapterCount != other.chapterCount) return false
        if (lastReadChapter != other.lastReadChapter) return false
        if (wasUpdated != other.wasUpdated) return false
        if (isFavorite != other.isFavorite) return false
        if (isFavoriteInWeb != other.isFavoriteInWeb) return false
        if (rating != other.rating) return false
        if (status != other.status) return false
        if (genres != other.genres) return false
        if (chapterList != other.chapterList) return false
        if (comments != other.comments) return false
        if (bookmarkIdRf != other.bookmarkIdRf) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + (engTitle?.hashCode() ?: 0)
        result = 31 * result + title.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (readyDate?.hashCode() ?: 0)
        result = 31 * result + (lang?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (additionalInfo?.hashCode() ?: 0)
        result = 31 * result + (chapterCount ?: 0)
        result = 31 * result + (lastReadChapter ?: 0)
        result = 31 * result + wasUpdated.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + isFavoriteInWeb.hashCode()
        result = 31 * result + (rating?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (genres?.hashCode() ?: 0)
        result = 31 * result + chapterList.hashCode()
        result = 31 * result + comments.hashCode()
        result = 31 * result + bookmarkIdRf
        return result
    }

}
