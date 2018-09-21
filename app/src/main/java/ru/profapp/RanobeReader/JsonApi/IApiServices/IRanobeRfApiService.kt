package ru.profapp.RanobeReader.JsonApi.IApiServices

import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfBookInfoGson
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfChapterTextGson
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfGetReadyGson
import ru.profapp.RanobeReader.JsonApi.Ranoberf.RfSearchJson
import ru.profapp.RanobeReader.JsonApi.Rulate.BookInfoGson


interface IRanobeRfApiService {

    @FormUrlEncoded
    @POST("/v1/book/last/")
    fun GetReadyBooks(@Field("page") page: Int, @Field("sequence") sequence: String): Observable<RfGetReadyGson>

    @GET("/v1/book/search/")
    fun SearchBooks(@Query("q") search: String): Observable<RfSearchJson>

    @GET("/v1/part/get/")
    fun GetChapterText(@Query("bookAlias") bookAlias: String = "", @Query("partAlias") partAlias: String = ""): Observable<RfChapterTextGson>

    @GET("/v1/book/get/")
    fun GetBookInfo(@Query("bookAlias") bookAlias: String=""): Observable<RfBookInfoGson>

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