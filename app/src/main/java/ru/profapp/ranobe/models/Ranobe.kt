package ru.profapp.ranobe.models

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.reactivex.Single
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.common.Constants.RanobeSite.*
import ru.profapp.ranobe.network.dto.rulateDTO.RulateComment
import ru.profapp.ranobe.network.repositories.RanobeHubRepository
import ru.profapp.ranobe.network.repositories.RanobeRfRepository
import ru.profapp.ranobe.network.repositories.RulateRepository
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

    @Ignore
    var newChapters: Int = 0

    //    @Ignore
    //    var hidePaymentChapters: Boolean = MyApp.hidePaymentChapter

    fun updateRanobe(mContext: Context): Single<Boolean> {

        val bookInfo: Single<Boolean>

        bookInfo = if (!wasUpdated) {
            if (ranobeSite == Rulate.url || url.contains(Rulate.url)) {
                val token = MyApp.preferencesManager.rulateToken
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

}
