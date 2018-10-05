package ru.profapp.RanobeReader.JsonApi.IApiServices

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.profapp.RanobeReader.JsonApi.Ranoberf.*

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

    @FormUrlEncoded
    @POST("/v1/auth/login/")
    fun Login(@Field("email") email: String, @Field("password") password: String): Single<RfLoginGson>

    @FormUrlEncoded
    @POST("/v1/bookmark/add/")
    fun AddBookmark(@Header("Authorization") token: String, @Field("book_id") book_id: Int, @Field("part_id") part_id: Int): Single<RfBookmarkGson>

    @FormUrlEncoded
    @DELETE("/v1/bookmark/delete/")
    fun RemoveBookmark(@Header("Authorization") token: String, @Field("bookmark_id") bookmark_id: Int): Single<RfBookmarkGson>

    companion object Factory {

        fun create(): IRanobeRfApiService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://xn--80ac9aeh6f.xn--p1ai")
                    .build()
            return retrofit.create(IRanobeRfApiService::class.java)
        }
    }
}