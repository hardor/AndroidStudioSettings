package ru.profapp.RanobeReader.JsonApi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import ru.profapp.RanobeReader.Common.RanobeConstants
import ru.profapp.RanobeReader.JsonApi.IApiServices.IRanobeRfApiService
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfBook
import ru.profapp.RanobeReader.JsonApi.Ranoberf.Sequence
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import java.util.*

object RanobeRfRepository {


    private var sequence: String = ""
    val gson = Gson()
    private val listType = object : TypeToken<List<Sequence>>() {}.type

    fun getReadyBooks(page: Int): Observable<ArrayList<Ranobe>> {
        if (page == 1)
            sequence = ""
        return IRanobeRfApiService.create().GetReadyBooks(page, sequence)
                .map {


                    this.sequence = gson.toJson(it.result?.sequence, listType)
                    return@map getRanobeList(it.result?.books)
                }
    }

    private fun getRanobeList(it: List<RfBook>?): ArrayList<Ranobe> {
        val or: ArrayList<Ranobe> = ArrayList()
        for (value in it.orEmpty()) {
            val ranobe = Ranobe(RanobeConstants.RanobeSite.RanobeRf)
            ranobe.UpdateRanobeRfRanobe(value)
            or.add(ranobe)
        }
        return or
    }

    fun getBookInfo(ranobe: Ranobe): Observable<Ranobe> {
        return Observable.create { ranobe }
    }

    fun searchBooks(search: String): Observable<ArrayList<Ranobe>> {
        return IRanobeRfApiService.create().SearchBooks(search)
                .map {
                    val or: ArrayList<Ranobe> = ArrayList()

                    if (it.status == 200) {

                        for (book in it.result) {
                            val ranobe = Ranobe(RanobeConstants.RanobeSite.RanobeRf)
                            ranobe.id = book.id ?: ranobe.id
                            ranobe.url = RanobeConstants.RanobeSite.RanobeRf.url + book.link
                            ranobe.title = book.label?:ranobe.title
                            ranobe.engTitle = book.value?.replace(book.label + " / ", "")
                            ranobe.image = book.image
                            or.add(ranobe)
                        }
                    }

                    return@map or
                }
    }


    private infix fun Ranobe.UpdateRanobeRfRanobe(book: RfBook) {
        id = if (id == -1) book.id ?: id else id
        title = if (title.isBlank())  book.title?:title else title
        url = if (url.isBlank()) RanobeConstants.RanobeSite.RanobeRf.url + book.url else url
        readyDate = readyDate ?: book.lastUpdatedBook?.times(1000)?.let { Date(it) }
        image = image ?: book.image?.desktop?.image
        rating = rating ?: "Likes: ${book.likes} Dislikes:${book.dislikes}"

//        title = book.name
//        engTitle = book.fullTitle?.replace(title + " / ", "")
//        url = book.alias
//        description = StringHelper.removeTags(book.description)
//        additionalInfo = StringHelper.cleanAdditionalInfo(book.info)
//        image = image ?: RanobeConstants.RanobeSite.RanobeRf.url + book.images[0]

//        genres = result.genres!!.toString()
//        if (chapterList.any())
//            readyDate = Date(result.parts!![0].publishedAt!! * 1000)
//
        chapterList.clear()
        for ((index, rChapter) in book.parts.withIndex()) {
            val chapter = Chapter()

            chapter.id = if (chapter.id == -1) rChapter.id ?: chapter.id else chapter.id
            chapter.title =  if (chapter.title.isBlank()) "${rChapter.partNumber} ${rChapter.title}" else chapter.title
            chapter.url = if (chapter.url.isBlank()) rChapter.url ?: chapter.url else chapter.url
            if (!chapter.url.contains(RanobeConstants.RanobeSite.RanobeRf.url)) {
                chapter.url = RanobeConstants.RanobeSite.RanobeRf.url + chapter.url
            }

            chapter.canRead = (!rChapter.payment!!)
            chapter.ranobeUrl = if (chapter.ranobeUrl.isBlank()) url else chapter.ranobeUrl
            chapter.ranobeName = title
            chapter.index = index


            //  chapter.url = rChapter.alias ?: chapter.url


            chapterList.add(chapter)
        }


    }

}