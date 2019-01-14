package ru.profapp.ranobe.network.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.helpers.LogType
import ru.profapp.ranobe.helpers.logError
import ru.profapp.ranobe.helpers.removeTags
import ru.profapp.ranobe.models.Chapter
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.models.RanobeImage
import ru.profapp.ranobe.network.dto.ranobeHubDTO.RanobeHubBook
import ru.profapp.ranobe.network.dto.ranobeHubDTO.RanobeHubReadyGson
import ru.profapp.ranobe.network.endpoints.IRanobeHubApiService
import ru.profapp.ranobe.network.interceptors.AddCookiesInterceptor
import ru.profapp.ranobe.network.interceptors.ReceivedCookiesInterceptor
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object RanobeHubRepository : BaseRepository() {

    private val nf = NumberFormat.getNumberInstance(Locale.ENGLISH)
    private val decFormat: DecimalFormat = nf as DecimalFormat

    init {
        decFormat.isDecimalSeparatorAlwaysShown = false
    }

    private var token: String = ""

    fun getBookInfo(ranobe: Ranobe): Single<Boolean> {
        return instance.GetChapters(ranobe.id).map {
            ranobe.chapterList.clear()
            var index = 0
            val volumesNum = it.data.size
            for (volume in it.data) {
                for (tChapter in volume.chapters) {
                    val chapter = Chapter()

                    chapter.title = if (chapter.title.isBlank()) {
                        if (volumesNum > 1) {
                            "Том ${decFormat.format(volume.num)}. ${tChapter.name}"
                        } else tChapter.name
                    } else chapter.title

                    chapter.url = tChapter.url ?: chapter.url

                    chapter.ranobeUrl = ranobe.url
                    chapter.ranobeName = ranobe.title
                    chapter.index = index++

                    ranobe.chapterList.add(chapter)
                }
            }

            ranobe.chapterList.reverse()
            return@map true
        }.onErrorReturn {
            logError(LogType.ERROR, "getBookInfo", ranobe.url, it)
            false
        }

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
                return@flatMap instance.GetReadyBooks(token = token).map {
                    return@map getRanobeReadyList(it)
                }
            }

        } else {
            return if (page == 1) {
                instance.GetReadyBooks(null, token)
            } else {
                instance.GetReadyBooks(page, token)
            }.map {
                return@map getRanobeReadyList(it)
            }
        }

    }

    private fun getRanobeReadyList(it: RanobeHubReadyGson): List<Ranobe> {
        val or: MutableList<Ranobe> = mutableListOf()

        if (it.resource != null) {

            for (resource in it.resource) {

                val ranobeH = resource.ranobe

                var existRanobe = or.firstOrNull { b -> b.id == ranobeH?.id }

                if (existRanobe == null) {

                    val ranobe = Ranobe(Constants.RanobeSite.RanobeHub).apply {
                        id = ranobeH?.id
                        url = "${Constants.RanobeSite.RanobeHub.url}/ranobe/${id}"
                        image = Constants.RanobeSite.RanobeHub.url + ranobeH?.poster
                        title = ranobeH?.names?.rus ?: ""
                        engTitle = ranobeH?.names?.eng
                        readyDate = resource.createdAt?.let { it1 -> Date(it1 * 1000) }
                    }


                    if (!ranobe.image.isNullOrBlank()) {
                        Completable.fromAction {
                            MyApp.database.ranobeImageDao().insert(RanobeImage(ranobe.url, ranobe.image!!))
                        }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                            logError(LogType.ERROR, "", "", error, false)
                        })
                    }

                    or.add(ranobe)
                    existRanobe = ranobe
                }

                val jsObject = Jsoup.parse(resource.content)
                if (jsObject.hasText()) {

                    val obj = jsObject.selectFirst("a")
                    if (obj != null && obj.hasAttr("href")) {
                        val chapter = Chapter()
                        chapter.apply {
                            title = obj.text()
                            ranobeId = existRanobe.id
                            ranobeUrl = existRanobe.url
                            url = obj.attr("href")
                            ranobeName = existRanobe.title
                        }

                        existRanobe.chapterList.add(chapter)
                    }

                }

            }
        }

        or.forEach { r -> r.chapterList.sortByDescending { ch -> ch.url } }
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
                if (mCurrentChapter.title.isBlank()) mCurrentChapter.title = body.selectFirst(".__ranobe_read_container h1").text()

                val textObj: Element? = body.selectFirst(".__ranobe_read_container")
                textObj?.selectFirst("h1")?.remove()
                textObj?.select(".ads-desktop")?.remove()
                textObj?.select(".ads-mobile")?.remove()
                textObj?.select(".adsbygoogle")?.remove()
                textObj?.select("img")?.forEach { it ->
                    val img = it.attr("data-src")

                    var newAttr = Constants.RanobeSite.RanobeHub.url
                    if (img.contains("/img/ranobe"))
                        newAttr += img
                    else
                        newAttr = "$newAttr/img/ranobe/content/${mCurrentChapter.ranobeUrl.split("/").takeLast(1)[0]}/$img.jpg"

                    it.attr("src", newAttr)
                }
                mCurrentChapter.text = textObj?.html()
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

        description = description ?: book.description?.removeTags() ?: ""

        if (!book.changedAt.isNullOrEmpty()) {
            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
            df.timeZone = TimeZone.getTimeZone("UTC")

            try {
                readyDate = df.parse(book.changedAt)
            } catch (e: ParseException) {
                logError(LogType.WARN, "Parse date", "", e, false)
            }
        }

        image = image ?: Constants.RanobeSite.RanobeHub.url + "/img/ranobe/posters/" + id + "/0-min.jpg"

        if (!image.isNullOrBlank()) {
            Completable.fromAction {
                MyApp.database.ranobeImageDao().insert(RanobeImage(url, image!!))
            }?.subscribeOn(Schedulers.io())?.subscribe({}, { error ->
                logError(LogType.ERROR, "", "", error, false)
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

