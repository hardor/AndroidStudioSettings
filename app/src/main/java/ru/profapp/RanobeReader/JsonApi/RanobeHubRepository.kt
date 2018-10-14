package ru.profapp.RanobeReader.JsonApi

import io.reactivex.Single
import org.jsoup.Jsoup
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Helpers.StringHelper
import ru.profapp.RanobeReader.JsonApi.IApiServices.IRanobeHubApiService
import ru.profapp.RanobeReader.JsonApi.RanobeHub.RanobeHubBook
import ru.profapp.RanobeReader.JsonApi.RanobeHub.RanobeHubReadyGson
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object RanobeHubRepository {

    private var token: String = ""

    var Cookie: MutableList<String> = mutableListOf()

    fun getBookInfo(ranobe: Ranobe): Single<Boolean> {
        return IRanobeHubApiService.instance.GetChapters(ranobe.id).map {
            ranobe.chapterList.clear()
            var index = 0
            for (volume in it.data) {
                for (rChapter in volume.chapters.reversed()) {
                    val chapter = Chapter()

                    chapter.title = if (chapter.title.isBlank()) rChapter.name else chapter.title

                    chapter.url = if (chapter.url.isBlank()) "${ranobe.url}/${volume.num}/${rChapter.num}" else chapter.url

                    chapter.ranobeUrl = ranobe.url
                    chapter.ranobeName = ranobe.title
                    chapter.index = index++

                    ranobe.chapterList.add(chapter)
                }
            }

            return@map true
        }.onErrorReturn { false }

    }

    fun getReadyBooks(page: Int): Single<List<Ranobe>> {

        if (page == 1 && token.isBlank()) {


            return IRanobeHubApiService.instanceHtml.GetReady().flatMap { response ->
                if (response.isSuccessful) {
                    val metaOgTitle = Jsoup.parse(response.body()?.string()).head().select("meta[name=csrf-token]")
                    if (metaOgTitle != null) {
                        token = metaOgTitle.attr("content")
                    }
                }
                return@flatMap IRanobeHubApiService.instance.GetReadyBooks(page, token).map {
                    return@map getRanobeReadyList(it)
                }
            }

        } else {
            return IRanobeHubApiService.instance.GetReadyBooks(page, token).map {
                return@map getRanobeReadyList(it)
            }
        }


    }

    private fun getRanobeReadyList(it: RanobeHubReadyGson): List<Ranobe> {
        val or: MutableList<Ranobe> = mutableListOf()

        if (it.content.isNotBlank()) {

            val items = Jsoup.parse(it.content).select("div.grid_item")
            for (item in items) {
                val ranobe = Ranobe(Constants.RanobeSite.RanobeHub)
                ranobe.url = item.selectFirst("a.image").attr("href")
                ranobe.id = ranobe.url.replace("${Constants.RanobeSite.RanobeHub.url}/ranobe/", "").toInt()
                ranobe.image = Constants.RanobeSite.RanobeHub.url + item.selectFirst("img").attr("data-src")
                ranobe.description = item.selectFirst("div.description").ownText()
                ranobe.title = item.selectFirst("div.grid_item_header").selectFirst("a").text()
                ranobe.engTitle = item.selectFirst("div.grid_item_header").selectFirst("h5").text()
                or.add(ranobe)
            }
        }

        return or

    }

    fun searchBooks(search: String): Single<List<Ranobe>> {
        return IRanobeHubApiService.instance.SearchBooks(search).map {
            return@map getRanobeList(it.categories.ranobe.items)
        }
    }

    fun getChapterText(mCurrentChapter: Chapter): Single<Boolean> {

        val parts = mCurrentChapter.url.split("/")

        return IRanobeHubApiService.instanceHtml.GetChapterText(parts[4], parts[5], parts[6]).map { response ->
            if (response.isSuccessful) {
                val jsObject = Jsoup.parse(response.body()?.string())
                if (token.isBlank()) {
                    val metaOgTitle = jsObject.head().selectFirst("meta[name=csrf-token]")
                    if (metaOgTitle != null) {
                        token = metaOgTitle.attr("content")
                    }
                }

                val body = jsObject.body()
                mCurrentChapter.title = body.selectFirst(".__ranobe_read_container h1").text()
                val textObj =body.selectFirst(".__ranobe_read_container")
                textObj.selectFirst("h1").remove()
                textObj.select(".ads-desktop").remove()
                textObj.select(".ads-mobile").remove()
                textObj.select(".adsbygoogle").remove()
                mCurrentChapter.text = textObj.html()
                return@map true
            } else
                return@map false
        }

    }


    private fun getRanobeList(it: List<RanobeHubBook>): List<Ranobe> {
        val or: MutableList<Ranobe> = mutableListOf()

        for (value in it) {
            val ranobe = Ranobe(Constants.RanobeSite.RanobeHub)
            ranobe.updateRanobeHubRanobe(value)
            or.add(ranobe)

        }
        return or
    }

    private infix fun Ranobe.updateRanobeHubRanobe(book: RanobeHubBook) {

        id = if (id == null) book.id ?: id else id

        title = if (title.isBlank()) book.nameRus ?: title else title
        engTitle = engTitle ?: book.nameEng

        url = Constants.RanobeSite.RanobeHub.url + "/ranobe/" + id

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

        image = image ?: Constants.RanobeSite.RanobeHub.url + "/img/ranobe/posters/" + id + "/0-min.jpg"

        rating = book.rating?.toString()
        chapterCount = chapterCount ?: book.chapters
    }

}

