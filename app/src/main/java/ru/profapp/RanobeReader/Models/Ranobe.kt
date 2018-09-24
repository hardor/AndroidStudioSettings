package ru.profapp.RanobeReader.Models

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Common.Constants.RanobeSite.*
import ru.profapp.RanobeReader.Common.StringResources
import ru.profapp.RanobeReader.Fragments.RepositoryProvider
import ru.profapp.RanobeReader.Helpers.MyLog
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateComment
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
    @ColumnInfo(name = "id")
    var id: Int = -1
        get() {
            if (field != -1) {
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
    var title: String=""
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

    @ColumnInfo(name = "LastReadCharpter")
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
    var rulateComments: List<RulateComment> = ArrayList()

    @Ignore
    var bookmarkIdRf: Int = 0



    fun UpdateRanobe(`object`: JSONObject, enumFrom: Constants.JsonObjectFrom) {

        when (enumFrom) {

            Constants.JsonObjectFrom.RanobeRfGetReady -> fromRanobeRfGetReady(`object`, enumFrom)
            Constants.JsonObjectFrom.RulateFavorite -> fromRulateFavorite(`object`, enumFrom)
            else -> {
            }
        }//  throw isNew NullPointerException();
    }

    private fun fromRulateFavorite(`object`: JSONObject, enumFrom: Constants.JsonObjectFrom) {
        ranobeSite = Rulate.url
        isFavoriteInWeb = true
        try {
            engTitle = `object`.getString("s_title")
            title = `object`.getString("t_title")
            lang = `object`.optString("lang")
            chapterCount = `object`.optInt("n_chapters")
            id = `object`.getInt("book_id")
            url = Rulate.url + "/book/" + id
        } catch (e: JSONException) {
            MyLog.SendError(MyLog.LogType.WARN, Ranobe::class.java.toString(), "", e)

        }

    }

    private fun fromRanobeRfGetReady(`object`: JSONObject, enumFrom: Constants.JsonObjectFrom) {
        ranobeSite = RanobeRf.url
        try {

            title = `object`.getString("name")
            readyDate = java.util.Date(`object`.getLong("last_updated_book") * 1000)

            url = `object`.getString("alias")
            if (`object`.has("images")) {
                val jsonArray = `object`.optJSONArray("images")
                image = RanobeRf.url + jsonArray.getString(0)
            }

            if (`object`.has("parts")) {
                val jsonArray = `object`.optJSONArray("parts")
                for (i in 0 until jsonArray.length()) {

                    val value = jsonArray.optJSONObject(i)
                    val chapter = Chapter(value, enumFrom)
                    chapter.ranobeUrl = url
                    chapter.ranobeName = title
                    chapter.index = i
                    chapterList.add(chapter)

                }
            }

        } catch (e: JSONException) {
            MyLog.SendError(MyLog.LogType.WARN, Ranobe::class.java.toString(), "", e)

        }

    }



    private fun updateRanobeHubRanobe() {

        val repository = RepositoryProvider.provideRanobeHubRepository()
        repository.getChapters(id).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

                .subscribe { result ->
                    for ((ind, res) in result.withIndex()) {
                        val chapter = Chapter(res)
                        chapter.ranobeUrl = url
                        chapter.ranobeName = title
                        chapter.index = ind
                        chapterList.add(chapter)
                    }

                }

    }

    fun updateRanobe(mContext: Context): Single<Ranobe> {

        if (!wasUpdated) {
            if (ranobeSite == Rulate.url || url.contains(Rulate.url)) {
                val mPreferences = mContext.getSharedPreferences(
                        StringResources.Rulate_Login_Pref, 0)
                val token = mPreferences.getString(StringResources.KEY_Token, "")?:""
                return RepositoryProvider.provideRulateRepository().getBookInfo(this, token, id)

            } else if (ranobeSite == RanobeRf.url || url.contains(                      RanobeRf.url)) {
                // updateRanobeRfRanobe()
                return RepositoryProvider.provideRanobeRfRepository().getBookInfo(this)
            } else if (ranobeSite == RanobeHub.url || url.contains(
                            RanobeHub.url)) {
                //updateRanobeHubRanobe()
                return RepositoryProvider.provideRanobeHubRepository().getBookInfo(this)

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


}
