package ru.profapp.ranobe.network.repositories

import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.helpers.logError
import ru.profapp.ranobe.helpers.logWarn
import ru.profapp.ranobe.models.Chapter
import ru.profapp.ranobe.models.Comment
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.models.RanobeImage
import ru.profapp.ranobe.network.customDeserializer.RulateBookDeserializer
import ru.profapp.ranobe.network.dto.rulateDTO.*
import ru.profapp.ranobe.network.endpoints.IRulateApiService
import ru.profapp.ranobe.network.interceptors.ApiKeyInterceptor
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object RulateRepository : BaseRepository() {

    fun getBookInfo(ranobe: Ranobe, token: String = "", book_id: Int?): Single<Boolean> {
        return instance.GetBookInfo(token, book_id).map {
            if (it.status == "success") {
                it.response?.let { it1 ->
                    ranobe.updateRanobe(it1)
                    return@map true
                }
            }
            return@map false
        }.onErrorReturn { false }
    }

    fun getReadyBooks(page: Int): Single<List<Ranobe>> {
        return instance.GetReadyBooks(page).map {

            return@map getRanobeList(it)
        }
    }

    fun getFavoriteBooks(token: String?): Single<List<Ranobe>> {
        if (token.isNullOrBlank()) return Single.just(listOf())

        return instance.GetFavoriteBooks(token).map {
            val or: MutableList<Ranobe> = mutableListOf()


            if (it.status == "success") {
                for (response in it.favResponse) {

                    val ranobe = Ranobe(Constants.RanobeSite.Rulate)
                    ranobe.isFavoriteInWeb = true
                    ranobe.isFavorite = true
                    ranobe.engTitle = response.sTitle
                    ranobe.title = response.tTitle
                    ranobe.lang = response.lang
                    ranobe.chapterCount = response.nChapters
                    ranobe.id = response.bookID
                    ranobe.url = Constants.RanobeSite.Rulate.url + "/book/" + response.bookID
                    or.add(ranobe)
                }
            }
            return@map or
        }
    }

    fun searchBooks(search: String): Single<List<Ranobe>> {
        return instance.SearchBooks(search).map {
            return@map getRanobeList(it)
        }.onErrorReturn {
            logError("searchBooks", "rulate: $search", it)
            listOf()
        }
    }

    fun login(login: String, password: String): Single<Array<String>> {
        return instance.Login(login, password).map {
            if (it.status == "success") {
                return@map arrayOf("true", it.msg, it.response.token)
            } else arrayOf("false", it.msg, "")
        }
    }

    fun getChapterText(token: String, mCurrentChapter: Chapter): Single<Boolean> {
        return instance.GetChapterText(token, mCurrentChapter.id, mCurrentChapter.ranobeId).map {
            if (it.status == "success") {
                it.response?.let { it1 ->
                    mCurrentChapter.updateChapter(it1)
                    return@map true
                }
                return@map false
            } else {
                mCurrentChapter.text = it.msg
                return@map false
            }
        }
    }

    fun addBookmark(token: String, book_id: Int?): Single<Pair<Boolean, String>> {
        return instance.AddBookmark(token, book_id).map {

            if (it.status == "success") return@map Pair(true, it.msg.toString())
            else return@map Pair(false, it.msg.toString())
        }
    }

    fun removeBookmark(token: String, book_id: Int?): Single<Pair<Boolean, String>> {
        return instance.RemoveBookmark(token, book_id).map {

            if (it.status == "success") return@map Pair(true, it.msg.toString())
            else return@map Pair(false, it.msg.toString())
        }
    }

    private fun getRanobeList(it: ReadyGson): List<Ranobe> {
        val or: MutableList<Ranobe> = mutableListOf()
        if (it.status == "success") {
            for (book in it.books) {
                val ranobe = Ranobe(Constants.RanobeSite.Rulate)
                ranobe.updateRanobe(book)

                val existRanobe = or.firstOrNull { b -> b.url == ranobe.url }
                if (existRanobe != null) {
                    existRanobe.chapterList.addAll(ranobe.chapterList)
                } else {
                    or.add(ranobe)
                }

            }
        }
        or.forEach { r -> r.chapterList.sortByDescending { ch -> ch.url } }
        return or
    }

    private fun getRanobeList(it: SearchGson): List<Ranobe> {
        val or: MutableList<Ranobe> = mutableListOf()
        if (it.status == "success") {
            for (book in it.books) {
                val ranobe = Ranobe(Constants.RanobeSite.Rulate)
                ranobe.updateRanobe(book)
                or.add(ranobe)
            }
        }
        return or
    }

    private infix fun Ranobe.updateRanobe(book: RulateReadyBook) {

        val mCalendar = Calendar.getInstance()
        val format = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        id = if (id == null) book.bookId ?: book.id ?: id else id

        engTitle = engTitle ?: book.sTitle

        title = if (title.isBlank()) book.tTitle ?: title else title
        image = image ?: book.img?.replace("-5050", "")

        if (!image.isNullOrBlank()) {
            Completable.fromAction {
                MyApp.database.ranobeImageDao().insert(RanobeImage(url, image!!))
            }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                logError("", "", error, false)
            })

        }

        lang = lang ?: book.lang
        try {

            if (readyDate == null) {
                if (book.readyDate != null) {
                    mCalendar.time = format.parse(book.readyDate)
                    mCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
                    mCalendar.timeZone = TimeZone.getTimeZone("Europe/Moscow")
                    readyDate = mCalendar.time
                }
            }

        } catch (e: ParseException) {
            logWarn("updateRanobe", "", e, false)
        }

        url = if (url.isBlank()) (Constants.RanobeSite.Rulate.url + "/book/" + id) else url

        chapterList.clear()
        val chapter = Chapter()
        chapter.ranobeId = id
        chapter.ranobeUrl = url
        chapter.url = url + "/" + book.id
        chapter.ranobeName = title
        chapter.title = book.title ?: ""
        chapterList.add(chapter)

    }

    private infix fun Ranobe.updateRanobe(book: RulateBook) {

//        val mCalendar = Calendar.getInstance()
//        val decFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        id = id ?: book.id

        engTitle = engTitle ?: book.sTitle
        title = if (title.isBlank()) book.tTitle ?: title else title
        image = book.img ?: image

        if (!image.isNullOrBlank()) {
            Completable.fromAction {
                MyApp.database.ranobeImageDao().insert(RanobeImage(url, image!!))
            }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                logError("", "", error, false)
            })

        }

        lang = lang ?: book.lang

        readyDate = readyDate
            ?: (if (book.lastActivity != null) Date(book.lastActivity * 1000) else readyDate)

        url = if (url.isBlank()) (Constants.RanobeSite.Rulate.url + "/book/" + id) else url

        chapterCount = chapterCount ?: book.chaptersTotal

        rating = rating ?: book.rating

        chapterList.clear()
        val size = book.chapters.size
        for ((i, chap) in book.chapters.withIndex()) {
            val chapter = Chapter()
            chapter.updateChapter(chap)
            chapter.ranobeId = id
            chapter.ranobeUrl = url
            chapter.url = url + "/" + chapter.id
            chapter.ranobeName = title
            chapter.index = i
            chapterList.add(chapter)
        }
        chapterList.reverse()


        comments = book.comments.asReversed().map {rulateComment ->
            Comment(rulateComment)
        }
        description = null

        if (lang != null) {
            description = (description ?: "") + ("\nПеревод: $lang")
        }
        if (chapterCount != null) {
            description = (description ?: "") + ("\nКоличество глав: $chapterCount")
        }
    }

    private infix fun Ranobe.updateRanobe(book: RulateSearchBook) {
        id = id ?: book.id
        engTitle = engTitle ?: book.sTitle

        title = if (title.isBlank()) book.tTitle ?: title else title

        lang = lang ?: book.lang

        readyDate = readyDate
            ?: (if (book.lastActivity != null) Date(book.lastActivity * 1000) else readyDate)

        url = if (url.isBlank()) (Constants.RanobeSite.Rulate.url + "/book/" + id) else url

        chapterCount = chapterCount ?: book.nChapters ?: chapterCount


        rating = rating ?: book.rating ?: rating

        description = null

        if (lang != null) {
            description = (description ?: "") + ("\nПеревод: $lang")
        }
        if (chapterCount != null) {
            description = (description ?: "") + ("\nКоличество глав: $chapterCount")
        }
        if (book.ready != null && book.ready != "&mdash;") {
            description = (description ?: "") + ("\nГотовность: ${book.ready}")
        }
    }

    private infix fun Chapter.updateChapter(rChapter: RulateChapter) {

        id = rChapter.id
        title = rChapter.title.toString()
        status = rChapter.status
        canRead = rChapter.canRead ?: false
        isNew = rChapter.new ?: false
    }

    private infix fun Chapter.updateChapter(response: RulateText) {
        title = response.title.toString()
        text = response.text?.replace("style=\"[^\"]*\"".toRegex(), "")

    }

    val gson = GsonBuilder().registerTypeAdapter(RulateBook::class.java,
        RulateBookDeserializer()).create()!!
    private var instance: IRulateApiService = create()

    fun create(): IRulateApiService {
        val httpClient = baseClient.addInterceptor(ApiKeyInterceptor())
        val retrofit = Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson)).client(httpClient.build())
            .baseUrl("https://tl.rulate.ru").build()

        return retrofit.create(IRulateApiService::class.java)
    }
}