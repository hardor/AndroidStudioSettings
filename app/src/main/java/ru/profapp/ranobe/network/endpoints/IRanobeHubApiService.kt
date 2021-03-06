package ru.profapp.ranobe.network.endpoints

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import ru.profapp.ranobe.network.dto.ranobeHubDTO.ChaptersGson
import ru.profapp.ranobe.network.dto.ranobeHubDTO.RanobeHubCommentsGson
import ru.profapp.ranobe.network.dto.ranobeHubDTO.RanobeHubReadyGson
import ru.profapp.ranobe.network.dto.ranobeHubDTO.RanobeHubSearchGson

interface IRanobeHubApiService {

    @Headers("X-Requested-With: XMLHttpRequest")
    @GET("/api/feed")
    fun GetReadyBooks(
            @Query("page")
            page: Int? = null,
            @Header("X-CSRF-TOKEN")
            token: String
    ): Single<RanobeHubReadyGson>

    @GET("/ranobe")
    fun GetReady(): Single<Response<ResponseBody>>

    @Headers("X-Requested-With: XMLHttpRequest")
    @GET("/api/ranobe/getByName/{name}")
    fun SearchBooks(
            @Path("name")
            name: String
    ): Single<RanobeHubSearchGson>

    @GET("/api/ranobe/{ranobe_id}/contents")
    fun GetChapters(
            @Path("ranobe_id")
            ranobe_id: Int?
    ): Single<ChaptersGson>

    @GET("api/comments")
    fun GetComments(
            @Query("commentable_id")
            ranobeId: Int,
            @Query("commentable_type")
            type: String= "\\App\\Entity\\Ranobe",
            @Query("order_by")
            orderBy: String="id",
            @Query("order_direction")
            order: String="asc"
    ): Single<RanobeHubCommentsGson>

    @Headers("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    @GET("/ranobe/{ranobe_id}/{volume_num}/{chapter_num}")
    fun GetChapterText(
            @Path("ranobe_id")
            ranobe_id: String,
            @Path("volume_num")
            volume_num: String,
            @Path("chapter_num")
            chapter_num: String
    ): Single<Response<ResponseBody>>

}