package ru.profapp.RanobeReaderTest.JsonApi.IApiServices

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import ru.profapp.RanobeReaderTest.JsonApi.Rulate.*

interface IRulateApiService {

    @GET("/api/getReady?key=fpoiKLUues81werht039")
    fun GetReadyBooks(@Query("page") page: Int): Single<ReadyGson>

    @GET("/api/searchBooks?key=fpoiKLUues81werht039")
    fun SearchBooks(@Query("search") search: String): Single<ReadyGson>

    @GET("/api/bookmarks?key=fpoiKLUues81werht039")
    fun GetFavoriteBooks(@Query("token") token: String): Single<FavoriteGson>

    @GET("/api/chapter?key=fpoiKLUues81werht039")
    fun GetChapterText(@Query("token") token: String, @Query("chapter_id") chapter_id: Int?, @Query("book_id") book_id: Int?): Single<ChapterTextGson>

    @GET("/api/addBookmark?key=fpoiKLUues81werht039")
    fun AddBookmark(@Query("token") token: String, @Query("book_id") book_id: Int): Single<BookmarkGson>

    @GET("/api/removeBookmark?key=fpoiKLUues81werht039")
    fun RemoveBookmark(@Query("token") token: String, @Query("book_id") book_id: Int): Single<BookmarkGson>

    @GET("/api/book?key=fpoiKLUues81werht039")
    fun GetBookInfo(@Query("token") token: String = "", @Query("book_id") book_id: Int?): Single<BookInfoGson>

    @GET("/api/auth?key=fpoiKLUues81werht039")
    fun Login(@Query("login") login: String, @Query("pass") pass: String): Single<LoginGson>

    companion object Factory {

        fun create(): IRulateApiService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://tl.rulate.ru")
                    .build()

            return retrofit.create(IRulateApiService::class.java)
        }
    }
}