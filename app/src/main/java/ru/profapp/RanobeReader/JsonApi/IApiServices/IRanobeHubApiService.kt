package ru.profapp.RanobeReader.JsonApi.IApiServices

import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.profapp.RanobeReader.JsonApi.RanobeHub.ChapterTextGson
import ru.profapp.RanobeReader.JsonApi.RanobeHub.ChaptersGson
import ru.profapp.RanobeReader.JsonApi.RanobeHub.RanobeHubReadyGson
import ru.profapp.RanobeReader.JsonApi.RanobeHub.RanobeHubSearchGson
import javax.xml.datatype.DatatypeConstants.SECONDS
import okhttp3.ConnectionPool
import java.util.concurrent.TimeUnit

interface IRanobeHubApiService {

    @FormUrlEncoded
    @Headers("X-Requested-With: XMLHttpRequest")
    @POST("/ranobe")
    fun GetReadyBooks(@Query("page") page: Int,
                      @Field("country") country: Int? = null,
                      @Field("sort") sort: String? = null,
                      @Field("tags") tags: List<String>? = null,
                      @Field("years") years: String? = null): Single<RanobeHubReadyGson>

    @GET("/ranobe")
    fun GetReady(): Single<RanobeHubReadyGson>

    @Headers("X-Requested-With: XMLHttpRequest")
    @GET("/api/ranobe/getByName/{name}")
    fun SearchBooks(@Path("name") name: String): Single<RanobeHubSearchGson>

    @GET("/api/ranobe/{ranobe_id}/contents")
    fun GetChapters(@Path("ranobe_id") ranobe_id: Int?): Single<ChaptersGson>

    @Headers("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    @GET("/ranobe/{ranobe_id}/{volume_num}/{chapter_num}")
    fun GetChapterText(@Path("ranobe_id") ranobe_id: Int, @Path("volume_num") volume_num: Int, @Path("chapter_num") chapter_num: Int): Single<String>

    companion object Factory {

        var instance: IRanobeHubApiService = create()
        var instanceHtml: IRanobeHubApiService = createHtml()

        fun create(): IRanobeHubApiService {
            val httpClient = OkHttpClient().newBuilder()
            //  httpClient.addInterceptor(AddCookiesInterceptor())
            //   httpClient.addInterceptor(ReceivedCookiesInterceptor())

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    //  .client(httpClient.build())
                    .baseUrl("https://ranobehub.org")
                    .build()

            return retrofit.create(IRanobeHubApiService::class.java)
        }

        fun createHtml(): IRanobeHubApiService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    //  .addConverterFactory(GsonConverterFactory.create())
                    //  .client(httpClient.build())
                    .baseUrl("https://ranobehub.org")
                    .build()

            return retrofit.create(IRanobeHubApiService::class.java)
        }
    }
}