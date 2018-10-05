package ru.profapp.RanobeReaderTest.JsonApi

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReaderTest.Common.Constants
import ru.profapp.RanobeReaderTest.Helpers.LogHelper
import ru.profapp.RanobeReaderTest.JsonApi.IApiServices.IRulateApiService
import ru.profapp.RanobeReaderTest.JsonApi.Rulate.ReadyGson
import ru.profapp.RanobeReaderTest.JsonApi.Rulate.RulateBook
import ru.profapp.RanobeReaderTest.JsonApi.Rulate.RulateChapter
import ru.profapp.RanobeReaderTest.JsonApi.Rulate.RulateText
import ru.profapp.RanobeReaderTest.Models.Chapter
import ru.profapp.RanobeReaderTest.Models.Ranobe
import ru.profapp.RanobeReaderTest.Models.RanobeImage
import ru.profapp.RanobeReaderTest.MyApp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object RulateRepository {

    fun getBookInfo(ranobe: Ranobe, token: String = "", book_id: Int?): Single<Ranobe> {
        return IRulateApiService.create().GetBookInfo(token, book_id).map {
            if (it.status == "success") {
                it.response?.let { it1 -> ranobe.updateRanobe(it1) }
            }
            return@map ranobe
        }
    }

    fun getReadyBooks(page: Int): Single<List<Ranobe>> {
        return IRulateApiService.create().GetReadyBooks(page).map {
            return@map getRanobeList(it)
        }
    }

    fun getFavoriteBooks(token: String?): Single<List<Ranobe>> {
        if (token.isNullOrBlank()) return Single.just(listOf())

        return IRulateApiService.create().GetFavoriteBooks(token!!).map {
            val or: MutableList<Ranobe> = mutableListOf()


            if (it.status == "success") {
                for (response in it.response) {

                    val ranobe = Ranobe(Constants.RanobeSite.Rulate)
                    ranobe.isFavoriteInWeb = true
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
        return IRulateApiService.create().SearchBooks(search).map {
            return@map getRanobeList(it)
        }
    }

    fun login(login: String, password: String): Single<Array<String>> {
        return IRulateApiService.create().Login(login, password).map {
            if (it.status == "success") {
                return@map arrayOf("true", it.msg, it.response.token)
            } else arrayOf("false", it.msg)
        }
    }

    fun getChapterText(token: String, mCurrentChapter: Chapter): Single<Boolean> {
        return IRulateApiService.create().GetChapterText(token, mCurrentChapter.id, mCurrentChapter.ranobeId).map {
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

    fun addBookmark(token: String, book_id: Int): Single<Pair<Boolean, String>> {
        return IRulateApiService.create().AddBookmark(token, book_id).map {

            if (it.status == "success") return@map Pair(true, it.msg.toString())
            else return@map Pair(false, it.msg.toString())
        }
    }

    fun removeBookmark(token: String, book_id: Int): Single<Pair<Boolean, String>> {
        return IRulateApiService.create().RemoveBookmark(token, book_id).map {

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
                or.add(ranobe)
            }
        }
        return or
    }

    private infix fun Ranobe.updateRanobe(book: RulateBook) {

        val mCalendar = Calendar.getInstance()
        val format = SimpleDateFormat("MM-dd HH:mm")
        id = if (id == null) book.bookId ?: book.id ?: id else id

        engTitle = engTitle ?: book.sTitle

        title = if (title.isBlank()) book.tTitle ?: title else title
        image = image ?: book.img?.replace("-5050", "")

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
            LogHelper.logError(LogHelper.LogType.WARN, Ranobe::class.java.toString(), "", e)
        }

        readyDate = readyDate ?: (if (book.lastActivity != null) Date(book.lastActivity!! * 1000) else readyDate)

        url = if (url.isBlank()) (Constants.RanobeSite.Rulate.url + "/book/" + id) else url

        chapterCount = chapterCount ?: book.chaptersTotal ?: chapterCount

        status = status ?: book.status ?: status
        rating = rating ?: book.rating ?: rating


        chapterList.clear()
        val size = book.chapters.size
        for ((i, chap) in book.chapters.withIndex()) {
            val chapter = Chapter()
            chapter.updateChapter(chap)
            chapter.ranobeId = id
            chapter.ranobeUrl = url
            chapter.url = url + "/" + chapter.id
            chapter.ranobeName = title
            chapter.index = size - 1 - i
            chapterList.add(chapter)
        }
        chapterList.reverse()


        comments = book.comments.asReversed()
        description = null
        if (status != null) {
            description = (description ?: "") + ("Статус: $status")
        }
        if (lang != null) {
            description = (description ?: "") + ("\nПеревод: $lang")
        }
        if (chapterCount != null) {
            description = (description ?: "") + ("\nКоличество глав: $chapterCount")
        }


        if (!image.isNullOrBlank()) {
            Completable.fromAction {
                MyApp.database?.ranobeImageDao()?.insert(RanobeImage(url, image!!))
            }?.subscribeOn(Schedulers.io())?.subscribe()

        }
    }

    private infix fun Chapter.updateChapter(rChapter: RulateChapter) {

        id = rChapter.id!!
        title = rChapter.title.toString()
        status = rChapter.status
        canRead = rChapter.canRead!!
        isNew = rChapter.new!!
    }

    private infix fun Chapter.updateChapter(response: RulateText) {

        title = response.title.toString()
        text = response.text

    }
}