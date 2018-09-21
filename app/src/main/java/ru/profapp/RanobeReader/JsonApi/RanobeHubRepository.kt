package ru.profapp.RanobeReader.JsonApi

import io.reactivex.Observable
import org.jsoup.Jsoup
import ru.profapp.RanobeReader.Common.RanobeConstants
import ru.profapp.RanobeReader.Helpers.StringHelper
import ru.profapp.RanobeReader.JsonApi.IApiServices.IRanobeHubApiService
import ru.profapp.RanobeReader.JsonApi.RanobeHub.RanobeHubBook
import ru.profapp.RanobeReader.JsonApi.RanobeHub.RanobeHubReadyGson
import ru.profapp.RanobeReader.JsonApi.RanobeHub.tChapter
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object RanobeHubRepository {

    fun getBookInfo(ranobe: Ranobe): Observable<Ranobe> {
        return IRanobeHubApiService.create().GetChapters(ranobe.id)
                .map {


                    ranobe.chapterList.clear()
                    var index = 0
                    for( volume in it.data ) {
                        for ( rChapter in volume.chapters) {
                            val chapter = Chapter()

                            chapter.title = if (chapter.title.isBlank()) rChapter.name else chapter.title

                            chapter.url = if (chapter.url.isBlank()) "${ranobe.url}/${volume.num}/${rChapter.num}" else chapter.url

                            chapter.ranobeUrl = ranobe.url
                            chapter.ranobeName = ranobe.title
                            chapter.index = index++

                            ranobe.chapterList.add(chapter)
                        }
                    }
                    return@map ranobe
                }


    }

    fun getReadyBooks(page: Int): Observable<ArrayList<Ranobe>> {
        return IRanobeHubApiService.create().GetReadyBooks(page)
                .map {
                    return@map getRanobeReadyList(it)
                }
    }


    private fun getRanobeReadyList(it: RanobeHubReadyGson): ArrayList<Ranobe> {
        val or: ArrayList<Ranobe> = ArrayList()

        if (it.content.isNotBlank()) {

           val items = Jsoup.parse(it.content).select("div.grid_item")
            for(item in items){
                val ranobe = Ranobe(RanobeConstants.RanobeSite.RanobeHub)
                ranobe.url = item.selectFirst("a.image").attr("href")
                ranobe.id =  ranobe.url.replace("${RanobeConstants.RanobeSite.RanobeHub.url}/ranobe/","").toInt()
                ranobe.image =  RanobeConstants.RanobeSite.RanobeHub.url + item.selectFirst("img").attr("data-src")
                ranobe.description = item.selectFirst("div.description").ownText()
                ranobe.title = item.selectFirst("div.grid_item_header").selectFirst("a").text()
                ranobe.engTitle = item.selectFirst("div.grid_item_header").selectFirst("h5").text()
                or.add(ranobe)
            }
        }

        return or

    }

    fun searchBooks(search: String): Observable<ArrayList<Ranobe>> {
        return IRanobeHubApiService.create().SearchBooks(search)
                .map {
                    return@map getRanobeList(it.categories.ranobe.items)
                }
    }

    fun getChapters(ranobe_id: Int): Observable<List<tChapter>> {
        return IRanobeHubApiService.create().GetChapters(ranobe_id)
                .map {
                    return@map it.data.map { c -> c.chapters }.flatten()
                }
    }

    private fun getRanobeList(it: List<RanobeHubBook>): ArrayList<Ranobe> {
        val or: ArrayList<Ranobe> = ArrayList()


        for (value in it) {
            val ranobe = Ranobe(RanobeConstants.RanobeSite.RanobeHub)
            ranobe.updateRanobeHubRanobe(value)
            or.add(ranobe)

        }
        return or
    }

    private infix fun Ranobe.updateRanobeHubRanobe(book: RanobeHubBook) {

        id = if (id == -1) book.id ?: id else id

        title = if (title.isBlank())  book.nameRus?:title else title
        engTitle = engTitle ?: book.nameEng

        url = RanobeConstants.RanobeSite.RanobeHub.url + "/ranobe/" + id

        description = description ?: StringHelper.removeTags(book.description!!)

        if (!book.changedAt.isNullOrEmpty()) {
            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
            df.timeZone = TimeZone.getTimeZone("UTC")

            try {
                readyDate = df.parse(book.changedAt!!)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        image = image ?: RanobeConstants.RanobeSite.RanobeHub.url + "/img/ranobe/posters/" + id + "/0-min.jpg"

        rating = book.rating?.toString()
        chapterCount = chapterCount ?: book.chapters
    }


}

