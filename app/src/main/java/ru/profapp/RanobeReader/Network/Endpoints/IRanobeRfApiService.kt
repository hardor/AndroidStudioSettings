package ru.profapp.RanobeReader.Network.Endpoints

import io.reactivex.Single
import retrofit2.http.*
import ru.profapp.RanobeReader.Network.DTO.RanoberfDTO.*

interface IRanobeRfApiService {

    @FormUrlEncoded
    @POST("/v1/book/last/")
    fun GetReadyBooks(@Field("page") page: Int, @Field("sequence") sequence: String): Single<RfGetReadyGson>

    @GET("/v1/book/search/")
    fun SearchBooks(@Query("q") search: String): Single<RfSearchJson>

    @GET("/v1/part/get/")
    fun GetChapterText(@Query("bookAlias") bookAlias: String = "", @Query("partAlias") partAlias: String = ""): Single<RfChapterTextGson>

    @GET("/v1/book/get/")
    fun GetBookInfo(@Query("bookAlias") bookAlias: String = ""): Single<RfBookInfoGson>

    @GET("/v1/bookmark/index/")
    fun GetFavoriteBooks(@Header("Authorization") token: String): Single<RfFavoriteGson>

    @GET("/v1/user/settings/")
    fun GetUserStatus(): Single<RfUserGson>

    @FormUrlEncoded
    @POST("/v1/auth/login/")
    fun Login(@Field("email") email: String, @Field("password") password: String): Single<RfLoginGson>

    @FormUrlEncoded
    @POST("/v1/bookmark/add/")
    fun AddBookmark(@Header("Authorization") token: String, @Field("bookId") book_id: Int?, @Field("partId") part_id: Int?): Single<RfBookmarkGson>

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/v1/bookmark/delete/", hasBody = true)
    fun RemoveBookmark(@Header("Authorization") token: String, @Field("id") bookmark_id: Int?): Single<RfBookmarkGson>

}