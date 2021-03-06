package ru.profapp.ranobe.network.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.helpers.logError
import ru.profapp.ranobe.helpers.removeTags
import ru.profapp.ranobe.models.Chapter
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.models.RanobeImage
import ru.profapp.ranobe.network.dto.ranoberfDTO.ResultBookInfo
import ru.profapp.ranobe.network.dto.ranoberfDTO.RfBook
import ru.profapp.ranobe.network.dto.ranoberfDTO.RfChapter
import ru.profapp.ranobe.network.dto.ranoberfDTO.Sequence
import ru.profapp.ranobe.network.endpoints.IRanobeRfApiService
import ru.profapp.ranobe.network.interceptors.RanobeRfCookiesInterceptor
import java.util.*

object RanobeRfRepository : BaseRepository() {

    var token: String? = null
    var paymentStatus: Boolean? = null
    private var sequence: String = ""
    val gson = Gson()
    private val listType = object : TypeToken<List<Sequence>>() {}.type

    private fun getUserStatus() {
        paymentStatus = instance.GetUserStatus().map {
            if (it.status == 200) {
                if (it.result?.paymentStatus.isNullOrEmpty()) {
                    return@map false
                } else {
                    return@map it.result!!.paymentContinue ?: 0 > (Date().time / 1000)
                }
            }

            return@map false
        }.onErrorReturn {
            logError("getUserStatus", "getUserStatus error", it)
            return@onErrorReturn false
        }.subscribeOn(Schedulers.io()).blockingGet()
    }

    fun getBookInfo(ranobe: Ranobe): Single<Boolean> {
        if (paymentStatus == null) {
            getUserStatus()
        }
        var ranobeName = ranobe.url.replace(Constants.RanobeSite.RanobeRf.url, "")
        ranobeName = ranobeName.replace("/", "")
        return instance.GetBookInfo(ranobeName).map {
            if (it.status == 200) {
                it.result?.let { it1 -> ranobe.updateRanobe(it1) }
                return@map true
            }

            return@map false
        }.onErrorReturn { false }
    }

    fun getReadyBooks(page: Int): Single<List<Ranobe>> {
        if (paymentStatus == null) {
            getUserStatus()
        }
        if (page == 1) sequence = ""
        return instance.GetReadyBooks(page, sequence).map {
            sequence = gson.toJson(it.result?.sequence, listType)
            return@map getRanobeList(it.result?.books)
        }
    }

    fun login(email: String, password: String): Single<Array<String>> {
        return instance.Login(email, password).map {
            if (it.status == 200) {
                token = it.result.token
                return@map arrayOf("true", it.message, it.result.token)
            } else arrayOf("false", it.message, "")
        }
    }

    fun searchBooks(search: String): Single<List<Ranobe>> {
        return instance.SearchBooks(search).map {
            val or: MutableList<Ranobe> = mutableListOf()

            if (it.status == 200) {

                for (book in it.result) {
                    val ranobe = Ranobe(Constants.RanobeSite.RanobeRf)
                    ranobe.id = book.id ?: ranobe.id
                    ranobe.url = Constants.RanobeSite.RanobeRf.url + book.link
                    ranobe.title = book.label ?: ranobe.title
                    ranobe.engTitle = book.value?.replace(book.label + " / ", "")
                    ranobe.image = book.image
                    if (!book.image.isNullOrBlank()) {
                        Completable.fromAction {
                            MyApp.database.ranobeImageDao()
                                .insert(RanobeImage(ranobe.url, book.image))
                        }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                            logError("", "", error, false)
                        })

                    }
                    or.add(ranobe)
                }
            }

            return@map or.toList()
        }.onErrorReturn {
            logError("searchBooks", "raboberf: $search", it)
            listOf<Ranobe>()
        }
    }

    fun getFavoriteBooks(token: String?): Single<List<Ranobe>> {

        if (token.isNullOrBlank()) return Single.just(mutableListOf())
        return instance.GetFavoriteBooks("Bearer $token").map {
            val or: MutableList<Ranobe> = mutableListOf()

            if (it.status == 200) {
                for (result in it.result) {
                    val ranobe = Ranobe(Constants.RanobeSite.RanobeRf)
                    ranobe.url = result.bookAlias
                    ranobe.title = result.bookTitle
                    ranobe.bookmarkIdRf = result.bookmarkId
                    ranobe.image = result.bookImage
                    ranobe.chapterCount = result.allPartsCount
                    ranobe.isFavoriteInWeb = true
                    ranobe.isFavorite = true
                    or.add(ranobe)
                }
            }
            return@map or
        }
    }

    fun getChapterText(mCurrentChapter: Chapter): Single<Boolean> {
        val parts = mCurrentChapter.url.split("/")

        return instance.GetChapterText(parts[3], parts[4]).map {

            if (it.status == 200) {
                val response = it.result
                if (response?.status == 200) {
                    mCurrentChapter.title = response.part?.title.toString()
                    mCurrentChapter.text = response.part?.content
                    mCurrentChapter.url = response.part?.url.toString()
                }

                if (it.result?.part?.payment == true && mCurrentChapter.text.isNullOrBlank()) {
                    mCurrentChapter.text = "Даннная страница находится на платной подписке"
                    return@map false
                }
                return@map true
            } else {
                mCurrentChapter.text = it.message
                return@map false
            }

        }
    }

    fun addBookmark(token: String?, book_id: Int?, part_id: Int?): Single<Pair<Boolean, String>> {
        if (token.isNullOrBlank()) return Single.just(Pair(false, "No token found"))
        return instance.AddBookmark("Bearer $token", book_id, part_id).map {

            if (it.status == 200) return@map Pair(true, it.message)
            else return@map Pair(false, it.message)
        }
    }

    fun removeBookmark(token: String?, bookmark_id: Int): Single<Pair<Boolean, String>> {
        if (token.isNullOrBlank()) return Single.just(Pair(false, "No token found"))

        return instance.RemoveBookmark("Bearer $token", bookmark_id).map {

            if (it.status == 200) return@map Pair(true, it.message)
            else return@map Pair(false, it.message)
        }
    }

    private fun getRanobeList(it: List<RfBook>?): List<Ranobe> {
        val or: MutableList<Ranobe> = mutableListOf()
        for (value in it.orEmpty()) {
            val ranobe = Ranobe(Constants.RanobeSite.RanobeRf)
            ranobe.updateRanobe(value)
            or.add(ranobe)
        }
        return or
    }

    private infix fun Ranobe.updateRanobe(book: RfBook) {
        id = if (id == null) book.id ?: id else id
        title = if (title.isBlank()) book.title ?: title else title
        url = if (url.isBlank()) Constants.RanobeSite.RanobeRf.url + book.url else url
        readyDate = readyDate ?: book.lastUpdatedBook?.times(1000)?.let { Date(it) }
        image = book.image?.desktop?.image ?: book.image?.mobile?.image ?:image

        if (!image.isNullOrBlank()) {
            Completable.fromAction {
                MyApp.database.ranobeImageDao().insert(RanobeImage(url, image!!))
            }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                logError("", "", error, false)
            })

        }

        rating = rating ?: "Likes: ${book.likes}, dislikes:${book.dislikes}"



        engTitle = book.fullTitle?.replace("$title / ", "")
        //      url = book.alias
        description = book.description?.removeTags()
        additionalInfo = book.info?.removeTags()

        chapterList.clear()
        for ((index, rChapter) in book.parts.withIndex()) {
            val chapter = Chapter()

            chapter.id = if (chapter.id == null) rChapter.id ?: chapter.id else chapter.id
            chapter.title = if (chapter.title.isBlank()) "${rChapter.partNumber} ${rChapter.title}" else chapter.title
            chapter.url = if (chapter.url.isBlank()) rChapter.url ?: chapter.url else chapter.url
            if (!chapter.url.contains(Constants.RanobeSite.RanobeRf.url)) {
                chapter.url = Constants.RanobeSite.RanobeRf.url + chapter.url
            }

            chapter.canRead = (!rChapter.partDonate && !rChapter.payment) || (rChapter.partDonate && rChapter.userDonate) || (rChapter.payment && paymentStatus == true)

            chapter.ranobeUrl = if (chapter.ranobeUrl.isBlank()) url else chapter.ranobeUrl
            chapter.ranobeName = title
            chapter.index = book.parts.size - index - 1

            //  chapter.url = rChapter.alias ?: chapter.url

            chapterList.add(chapter)
        }

    }

    private infix fun Ranobe.updateRanobe(result: ResultBookInfo) {
        result.book?.let { this.updateRanobe(it) }

        // genres = result.genres.map { genre -> genre.title }.toString()
        genres = result.genres.asSequence().map { it.title }.joinToString()

        chapterList.clear()

        val allChapters = result.donateParts.plus(result.parts)
        for ((i, ch) in allChapters.withIndex()) {
            val chapter = Chapter()
            chapter.updateChapter(ch)
            chapter.ranobeUrl = url
            chapter.ranobeName = title
            chapter.index = allChapters.size - i - 1
            chapterList.add(chapter)
        }
        if (chapterList.size > 0) {
            readyDate = readyDate ?: result.parts[0].publishedAt?.times(1000)?.let { Date(it) }
        }

    }

    private infix fun Chapter.updateChapter(rChapter: RfChapter) {

        id = if (id == null) rChapter.id ?: id else id
        title = if (title.isBlank()) String.format("%s %s",
            rChapter.partNumber ?: "",
            rChapter.title)
        else title


        url = if (url.isBlank()) (rChapter.url ?: url) else url
        if (!url.contains(Constants.RanobeSite.RanobeRf.url)) {
            url = Constants.RanobeSite.RanobeRf.url + url
        }
        canRead = (!rChapter.partDonate && !rChapter.payment) || (rChapter.partDonate && rChapter.userDonate) || (rChapter.payment && paymentStatus == true)
    }

    private var instance: IRanobeRfApiService = create()

    fun create(): IRanobeRfApiService {
        val httpClient = baseClient.addInterceptor(RanobeRfCookiesInterceptor(this))
        val retrofit = Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create()).client(httpClient.build())
            .baseUrl("https://xn--80ac9aeh6f.xn--p1ai").build()
        return retrofit.create(IRanobeRfApiService::class.java)
    }

}