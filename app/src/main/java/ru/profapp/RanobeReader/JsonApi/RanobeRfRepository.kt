package ru.profapp.RanobeReader.JsonApi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Helpers.StringHelper
import ru.profapp.RanobeReader.JsonApi.IApiServices.IRanobeRfApiService
import ru.profapp.RanobeReader.JsonApi.Ranoberf.ResultBookInfo
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfBook
import ru.profapp.RanobeReader.JsonApi.Ranoberf.Sequence
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import java.util.*
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfChapter


object RanobeRfRepository {


    private var sequence: String = ""
    val gson = Gson()
    private val listType = object : TypeToken<List<Sequence>>() {}.type

    fun getBookInfo(ranobe: Ranobe): Single<Ranobe> {
        var ranobeName = ranobe.url.replace(Constants.RanobeSite.RanobeRf.url, "")
        ranobeName = ranobeName.substring(1, ranobeName.length - 1)
        return IRanobeRfApiService.create().GetBookInfo(ranobeName)
                .map {
                    if (it.status == 200) {
                        it.result?.let { it1 -> ranobe.updateRanobeRfRanobe(it1) }
                    }
                    return@map ranobe
                }
    }

    fun getReadyBooks(page: Int): Single<List<Ranobe>> {
        if (page == 1)
            sequence = ""
        return IRanobeRfApiService.create().GetReadyBooks(page, sequence)
                .map {


                    this.sequence = gson.toJson(it.result?.sequence, listType)
                    return@map getRanobeList(it.result?.books)
                }
    }



    fun login(email: String,password: String): Single<Array<String>>{
        return IRanobeRfApiService.create().Login(email,password).map{
            if (it.status == 200) {
                return@map arrayOf("true", it.message, it.result.token)
            } else arrayOf("false", it.message)
        }
    }


    fun searchBooks(search: String): Single<ArrayList<Ranobe>> {
        return IRanobeRfApiService.create().SearchBooks(search)
                .map {
                    val or: ArrayList<Ranobe> = ArrayList()

                    if (it.status == 200) {

                        for (book in it.result) {
                            val ranobe = Ranobe(Constants.RanobeSite.RanobeRf)
                            ranobe.id = book.id ?: ranobe.id
                            ranobe.url = Constants.RanobeSite.RanobeRf.url + book.link
                            ranobe.title = book.label ?: ranobe.title
                            ranobe.engTitle = book.value?.replace(book.label + " / ", "")
                            ranobe.image = book.image
                            or.add(ranobe)
                        }
                    }

                    return@map or
                }
    }

    private fun getRanobeList(it: List<RfBook>?): List<Ranobe> {
        val or: MutableList<Ranobe> = arrayListOf()
        for (value in it.orEmpty()) {
            val ranobe = Ranobe(Constants.RanobeSite.RanobeRf)
            ranobe.updateRanobeRfRanobe(value)
            or.add(ranobe)
        }
        return or
    }
    private infix fun Ranobe.updateRanobeRfRanobe(book: RfBook) {
        id = if (id == -1) book.id ?: id else id
        title = if (title.isBlank()) book.title ?: title else title
        url = if (url.isBlank()) Constants.RanobeSite.RanobeRf.url + book.url else url
        readyDate = readyDate ?: book.lastUpdatedBook?.times(1000)?.let { Date(it) }
        image = image ?: book.image?.desktop?.image
        rating = rating ?: "Likes: ${book.likes} Dislikes:${book.dislikes}"



        engTitle = book.fullTitle?.replace("$title / ", "")
//        url = book.alias
        description = book.description?.let { StringHelper.removeTags(it) }
        additionalInfo = book.info?.let { StringHelper.cleanAdditionalInfo(it) }
        rating = rating?:  "Likes: " + book.likes + ", Dislikes: " + book.dislikes



        chapterList.clear()
        for ((index, rChapter) in book.parts.withIndex()) {
            val chapter = Chapter()

            chapter.id = if (chapter.id == -1) rChapter.id ?: chapter.id else chapter.id
            chapter.title = if (chapter.title.isBlank()) "${rChapter.partNumber} ${rChapter.title}" else chapter.title
            chapter.url = if (chapter.url.isBlank()) rChapter.url ?: chapter.url else chapter.url
            if (!chapter.url.contains(Constants.RanobeSite.RanobeRf.url)) {
                chapter.url = Constants.RanobeSite.RanobeRf.url + chapter.url
            }

            chapter.canRead = ( !rChapter.payment || rChapter.sponsor)
            chapter.ranobeUrl = if (chapter.ranobeUrl.isBlank()) url else chapter.ranobeUrl
            chapter.ranobeName = title
            chapter.index = index


            //  chapter.url = rChapter.alias ?: chapter.url


            chapterList.add(chapter)
        }


    }

    private infix fun Ranobe.updateRanobeRfRanobe(result: ResultBookInfo) {
        result.book?.let { this.updateRanobeRfRanobe(it) }

       // genres = result.genres.map { genre -> genre.title }.toString()
        genres = result.genres.toString()

        chapterList.clear()

        val allChapters = result.donateParts.plus(result.parts)
        for ((i, ch) in allChapters.withIndex()) {
            val chapter = Chapter()
            chapter.updateRanobeRfChapter(ch)
            chapter.ranobeUrl = url
            chapter.ranobeName = title
            chapter.index = i
            chapterList.add(chapter)
        }
        if (chapterList.size > 0) {
            readyDate = readyDate ?: result.parts[0].publishedAt?.times(1000)?.let { Date(it) }
        }


    }

    private infix fun Chapter.updateRanobeRfChapter(rChapter: RfChapter) {

        id = if (id == -1) rChapter.id ?: id else id
        title = if (title.isBlank()) String.format("%s %s", rChapter.partNumber
                ?: "", rChapter.title)
        else
            title


        url = if (url.isBlank()) (rChapter.url ?: url) else url
        if (!url.contains(Constants.RanobeSite.RanobeRf.url)) {
            url = Constants.RanobeSite.RanobeRf.url + url
        }
        canRead = !rChapter.partDonate &&( !rChapter.payment || rChapter.sponsor)
    }

}