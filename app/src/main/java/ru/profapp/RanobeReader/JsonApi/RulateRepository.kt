package ru.profapp.RanobeReader.JsonApi

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Helpers.MyLog
import ru.profapp.RanobeReader.JsonApi.IApiServices.IRulateApiService
import ru.profapp.RanobeReader.JsonApi.Rulate.ReadyGson
import ru.profapp.RanobeReader.JsonApi.Rulate.RulateBook
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.Models.RanobeImage
import ru.profapp.RanobeReader.MyApp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object RulateRepository {

    fun getBookInfo(ranobe: Ranobe, token: String = "", book_id: Int): Single<Ranobe> {
        return IRulateApiService.create().GetBookInfo(token, book_id)
                .map {
                    if (it.status == "success") {
                        it.response?.let { it1 -> ranobe.updateRulateRanobe(it1) }
                    }
                    return@map ranobe
                }
    }

    fun getReadyBooks(page: Int): Single<List<Ranobe>> {
        return IRulateApiService.create().GetReadyBooks(page)
                .map {
                    return@map getRanobeList(it)
                }
    }

    fun searchBooks(search: String): Single<List<Ranobe>> {
        return IRulateApiService.create().SearchBooks(search)
                .map {
                    return@map getRanobeList(it)
                }
    }


    fun login(login: String,password: String): Single<Array<String>>{
        return IRulateApiService.create().Login(login,password).map{
            if (it.status == "success") {
                return@map arrayOf("true", it.msg, it.response.token)
            } else arrayOf("false", it.msg)
        }
    }

    fun getChapterText(token: String, mCurrentChapter: Chapter): Single<Boolean> {
        return IRulateApiService.create().GetChapterText(token, mCurrentChapter.id, mCurrentChapter.ranobeId)
                .map {
                    if (it.status == "success") {
                        it.response?.let { it1 ->
                            mCurrentChapter.UpdateChapter(it1)
                            return@map true
                        }
                        return@map false
                    } else {
                        mCurrentChapter.text = it.msg
                        return@map false
                    }

                }
    }

    private fun getRanobeList(it: ReadyGson): List<Ranobe> {
        val or: MutableList<Ranobe> = arrayListOf()
        if (it.status == "success") {

            for (book in it.books) {
                val ranobe = Ranobe(Constants.RanobeSite.Rulate)
                ranobe.updateRulateRanobe(book)
                or.add(ranobe)
            }
        }
        return or
    }

    private infix fun Ranobe.updateRulateRanobe(book: RulateBook) {

        val mCalendar = Calendar.getInstance()
        val format = SimpleDateFormat("MM-dd HH:mm")
        id = if (id == -1) book.bookId ?: book.id ?: id else id

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
            MyLog.SendError(MyLog.LogType.WARN, Ranobe::class.java.toString(), "", e)
        }

        readyDate = readyDate ?: (if (book.lastActivity != null) Date(book.lastActivity!! * 1000) else readyDate)

        url = if (url.isBlank()) (Constants.RanobeSite.Rulate.url + "/book/" + id) else url

        chapterCount = chapterCount ?: book.chaptersTotal ?: chapterCount

        status = status ?: book.status ?: status
        rating = rating ?: book.rating ?: rating


        chapterList.clear()
        val size = book.chapters.size
        for ((i, chap) in book.chapters.withIndex()) {
            val chapter = Chapter(chap)
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
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe()

        }
    }

}