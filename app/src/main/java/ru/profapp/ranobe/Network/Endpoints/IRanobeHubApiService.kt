package ru.profapp.ranobe.Network.Endpoints

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import ru.profapp.ranobe.Network.DTO.RanobeHubDTO.ChaptersGson
import ru.profapp.ranobe.Network.DTO.RanobeHubDTO.RanobeHubReadyGson
import ru.profapp.ranobe.Network.DTO.RanobeHubDTO.RanobeHubSearchGson

interface IRanobeHubApiService {


    @Headers("X-Requested-With: XMLHttpRequest")
    @GET("/ranobe")
    fun GetReadyBooks(@Query("page") page: Int,
                      @Header("X-CSRF-TOKEN") token: String
//                      @Field("country") country: Int? = null,
//                      @Field("sort") sort: String? = null,
//                      @Field("tags") tags: List<String>? = null,
//                      @Field("years") years: String? = null
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
    fun GetChapterText(@Path("ranobe_id") ranobe_id: String,
                       @Path("volume_num") volume_num: String,
                       @Path("chapter_num") chapter_num: String
    ): Single<Response<ResponseBody>>

}