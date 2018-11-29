package ru.profapp.ranobe.Network.Repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.profapp.ranobe.Common.Constants
import ru.profapp.ranobe.Helpers.Helper
import ru.profapp.ranobe.Helpers.LogHelper
import ru.profapp.ranobe.Helpers.StringHelper
import ru.profapp.ranobe.Models.Chapter
import ru.profapp.ranobe.Models.Ranobe
import ru.profapp.ranobe.Models.RanobeImage
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.Network.DTO.RanobeHubDTO.RanobeHubBook
import ru.profapp.ranobe.Network.DTO.RanobeHubDTO.RanobeHubReadyGson
import ru.profapp.ranobe.Network.Endpoints.IRanobeHubApiService
import ru.profapp.ranobe.Network.Interceptors.AddCookiesInterceptor
import ru.profapp.ranobe.Network.Interceptors.ReceivedCookiesInterceptor
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object RanobeHubRepository : BaseRepository() {

    private var token: String = ""

    fun getBookInfo(ranobe: Ranobe): Single<Boolean> {
        return instance.GetChapters(ranobe.id).map {
            ranobe.chapterList.clear()
            var index = 0
            val volumesNum=it.data.size
            for (volume in it.data) {
                for (rChapter in volume.chapters) {
                    val chapter = Chapter()

                    chapter.title = if (chapter.title.isBlank()) {
                        if(volumesNum>1) {"Том ${volume.num}. ${rChapter.name}"} else rChapter.name
                    } else chapter.title

                    chapter.url = if (chapter.url.isBlank()) "${ranobe.url}/${volume.num}/${rChapter.num}" else chapter.url

                    chapter.ranobeUrl = ranobe.url
                    chapter.ranobeName = ranobe.title
                    chapter.index = index++

                    ranobe.chapterList.add(chapter)
                }
            }

            ranobe.chapterList.reverse()
            return@map true
        }.onErrorReturn { false }

    }

    fun getReadyBooks(page: Int): Single<List<Ranobe>> {

        if (page == 1 && token.isBlank()) {

            return instanceHtml.GetReady().flatMap { response ->
                if (response.isSuccessful) {
                    val metaOgTitle = Jsoup.parse(response.body()?.string()).head().select("meta[name=csrf-token]")
                    if (metaOgTitle != null) {
                        token = metaOgTitle.attr("content")
                    }
                }
                return@flatMap instance.GetReadyBooks(page, token).map {
                    return@map getRanobeReadyList(it)
                }
            }

        } else {
            return instance.GetReadyBooks(page, token).map {
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

                if (!ranobe.image.isNullOrBlank()) {
                    Completable.fromAction {
                        MyApp.database.ranobeImageDao().insert(RanobeImage(ranobe.url, ranobe.image!!))
                    }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                        LogHelper.logError(LogHelper.LogType.ERROR, "", "", error, false)
                    })

                }
                val desc = item.selectFirst("div.description")
                ranobe.description = desc.ownText()
                ranobe.title = item.selectFirst("div.header").selectFirst("a").text()
                ranobe.engTitle = item.selectFirst("div.header").selectFirst("h5").text()
                ranobe.readyDate = Helper.dateFromString(desc.selectFirst("span").text())
                or.add(ranobe)
            }
        }

        return or

    }

    fun searchBooks(search: String): Single<List<Ranobe>> {
        return instance.SearchBooks(search).map {
            return@map getRanobeList(it.categories.ranobe.items)
        }
    }

    fun getChapterText(mCurrentChapter: Chapter): Single<Boolean> {

        val parts = mCurrentChapter.url.split("/")

        return instanceHtml.GetChapterText(parts[4], parts[5], parts[6]).map { response ->
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
                val textObj = body.selectFirst(".__ranobe_read_container")
                textObj.selectFirst("h1").remove()
                textObj.select(".ads-desktop").remove()
                textObj.select(".ads-mobile").remove()
                textObj.select(".adsbygoogle").remove()
                textObj.select("img").forEach { it ->
                    val img = it.attr("data-src")

                    var newAttr = Constants.RanobeSite.RanobeHub.url
                    if (img.contains("/img/ranobe"))
                        newAttr += img
                    else
                        newAttr = "$newAttr/img/ranobe/content/${mCurrentChapter.ranobeUrl.split("/").takeLast(1)[0]}/$img.jpg"

                    it.attr("src", newAttr)
                }
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

        description = description ?: StringHelper.removeTags(book.description ?: "")

        if (!book.changedAt.isNullOrEmpty()) {
            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
            df.timeZone = TimeZone.getTimeZone("UTC")

            try {
                readyDate = df.parse(book.changedAt)
            } catch (e: ParseException) {
                LogHelper.logError(LogHelper.LogType.WARN, "Parse date", "", e, false)
            }
        }

        image = image ?: Constants.RanobeSite.RanobeHub.url + "/img/ranobe/posters/" + id + "/0-min.jpg"

        if (!image.isNullOrBlank()) {
            Completable.fromAction {
                MyApp.database.ranobeImageDao().insert(RanobeImage(url, image!!))
            }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                LogHelper.logError(LogHelper.LogType.ERROR, "", "", error, false)
            })

        }

        rating = book.rating?.toString()
        chapterCount = chapterCount ?: book.chapters
    }

    private var instance: IRanobeHubApiService = create()
    private var instanceHtml: IRanobeHubApiService = createHtml()

    fun create(): IRanobeHubApiService {

        val httpClient = baseClient.addInterceptor(AddCookiesInterceptor(this))
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://ranobehub.org")
                .client(httpClient.build())
                .build()

        return retrofit.create(IRanobeHubApiService::class.java)
    }

    private fun createHtml(): IRanobeHubApiService {
        val httpClient = baseClient.addInterceptor(AddCookiesInterceptor(this))
                .addInterceptor(ReceivedCookiesInterceptor(this))

        val retrofit = Retrofit.Builder()
                .client(httpClient.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .baseUrl("https://ranobehub.org")
                .build()

        return retrofit.create(IRanobeHubApiService::class.java)
    }

}

