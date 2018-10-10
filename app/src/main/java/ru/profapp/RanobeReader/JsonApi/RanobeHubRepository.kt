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

    var Cookie: HashMap<String, String> = hashMapOf()

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

        return when (page) {
            1 -> IRanobeHubApiService.instance.GetReady()
            else -> IRanobeHubApiService.instance.GetReadyBooks(page)
        }.map {
            return@map getRanobeReadyList(it)
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

        return IRanobeHubApiService.instance.GetChapterText(parts[4].toInt(), parts[5].toInt(), parts[6].toInt()).map {

            val a = it
            return@map true

            //                    if (it.status == 200) {
            //                        val response = it.result
            //                        if (response?.status == 200) {
            //                            mCurrentChapter.title = response.part!!.title.toString()
            //                            mCurrentChapter.text = response.part.content
            //                            mCurrentChapter.url = response.part.url!!
            //                        }
            //
            //                        if (it.result!!.part!!.payment!! && mCurrentChapter.text.isNullOrBlank()) {
            //                            mCurrentChapter.text = "Даннная страница находится на платной подписке"
            //                            return@map false
            //                        }
            //                        return@map true
            //                    } else {
            //                        mCurrentChapter.text = it.message
            //                        return@map false
            //                    }

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

