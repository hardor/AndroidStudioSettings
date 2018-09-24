package ru.profapp.RanobeReader.JsonApi.IApiServices

import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.profapp.RanobeReader.JsonApi.RanobeHub.ChapterTextGson
import ru.profapp.RanobeReader.JsonApi.RanobeHub.ChaptersGson
import ru.profapp.RanobeReader.JsonApi.RanobeHub.RanobeHubReadyGson
import ru.profapp.RanobeReader.JsonApi.RanobeHub.RanobeHubSearchGson


interface IRanobeHubApiService {

    @FormUrlEncoded
    @Headers("X-Requested-With: XMLHttpRequest")
    @POST("/ranobe")
    fun GetReadyBooks(@Query("page") page: Int,
                      @Field("country") country: Int?=null,
                      @Field("sort") sort: String?=null,
                      @Field("tags") tags: ArrayList<String>?=null,
                      @Field("years") years: String?=null): Single<RanobeHubReadyGson>

    @Headers("X-Requested-With: XMLHttpRequest")
    @GET("/api/ranobe/getByName/{name}")
    fun SearchBooks(@Path("name") name: String): Single<RanobeHubSearchGson>

    @GET("/api/ranobe/{ranobe_id}/contents")
    fun GetChapters(@Path("ranobe_id") ranobe_id: Int): Single<ChaptersGson>

    @GET("/api/ranobe/chapter")
    fun GetChapterText(@Query("ranobe_id") ranobe_id: Int, @Query("volume_num") volume_num: Int, @Query("chapter_num") chapter_num: Int): Single<ChapterTextGson>

    companion object Factory {

        fun create(): IRanobeHubApiService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://ranobehub.org")
                    .build()

            return retrofit.create(IRanobeHubApiService::class.java)
        }
    }
}