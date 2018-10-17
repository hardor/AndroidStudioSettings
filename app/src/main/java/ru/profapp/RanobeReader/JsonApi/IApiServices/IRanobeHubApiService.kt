package ru.profapp.RanobeReader.JsonApi.IApiServices

import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.profapp.RanobeReader.JsonApi.RanobeHubDTO.ChaptersGson
import ru.profapp.RanobeReader.JsonApi.RanobeHubDTO.RanobeHubReadyGson
import ru.profapp.RanobeReader.JsonApi.RanobeHubDTO.RanobeHubSearchGson

interface IRanobeHubApiService {

    @FormUrlEncoded
    @Headers("X-Requested-With: XMLHttpRequest")
    @POST("/ranobe")
    fun GetReadyBooks(@Query("page") page: Int,
                      @Header("X-CSRF-TOKEN") token: String,
                      @Field("country") country: Int? = null,
                      @Field("sort") sort: String? = null,
                      @Field("tags") tags: List<String>? = null,
                      @Field("years") years: String? = null
    ): Single<RanobeHubReadyGson>

    @GET("/ranobe")
    fun GetReady(): Single<Response<ResponseBody>>

    @Headers("X-Requested-With: XMLHttpRequest")
    @GET("/api/ranobe/getByName/{name}")
    fun SearchBooks(@Path("name") name: String): Single<RanobeHubSearchGson>

    @GET("/api/ranobe/{ranobe_id}/contents")
    fun GetChapters(@Path("ranobe_id") ranobe_id: Int?): Single<ChaptersGson>

    @Headers("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    @GET("/ranobe/{ranobe_id}/{volume_num}/{chapter_num}")
    fun GetChapterText(@Path("ranobe_id") ranobe_id: String, @Path("volume_num") volume_num: String, @Path("chapter_num") chapter_num: String): Single<Response<ResponseBody>>

    companion object Factory {

        var instance: IRanobeHubApiService = create()
        var instanceHtml: IRanobeHubApiService = createHtml()

        fun create(): IRanobeHubApiService {

            val httpClient = OkHttpClient().newBuilder().addInterceptor(AddCookiesInterceptor())
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://ranobehub.org")
                    .client(httpClient.build())
                    .build()

            return retrofit.create(IRanobeHubApiService::class.java)
        }

        fun createHtml(): IRanobeHubApiService {
            val httpClient = OkHttpClient().newBuilder().addInterceptor(AddCookiesInterceptor()).addInterceptor(ReceivedCookiesInterceptor())
            val retrofit = Retrofit.Builder()
                    .client(httpClient.build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(httpClient.build())
                    .baseUrl("https://ranobehub.org")
                    .build()

            return retrofit.create(IRanobeHubApiService::class.java)
        }
    }
}