package ru.profapp.RanobeReader.JsonApi.IApiServices

import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import ru.profapp.RanobeReader.JsonApi.Rulate.*


interface IRulateApiService  {

    @GET("/api/getReady?key=fpoiKLUues81werht039")
    fun GetReadyBooks(@Query("page") page: Int): Observable<ReadyGson>

    @GET("/api/searchBooks?key=fpoiKLUues81werht039")
    fun SearchBooks(@Query("search") search: String): Observable<ReadyGson>

    @GET("/api/bookmarks?key=fpoiKLUues81werht039")
    fun GetFavoriteBooks(@Query("token") token: String): Observable<ReadyGson>

    @GET("/api/chapter?key=fpoiKLUues81werht039")
    fun GetChapterText(@Query("token") token: String, @Query("chapter_id") chapter_id: Int, @Query("book_id") book_id: Int): Observable<ChapterTextGson>

    @GET("/api/addBookmark?key=fpoiKLUues81werht039")
    fun AddBookmark(@Query("token") token: String, @Query("book_id") book_id: Int): Observable<BookmarkGson>

    @GET("/api/removeBookmark?key=fpoiKLUues81werht039")
    fun RemoveBookmark(@Query("token") token: String,  @Query("book_id") book_id: Int): Observable<BookmarkGson>

    @GET("/api/book?key=fpoiKLUues81werht039")
    fun GetBookInfo(@Query("token") token: String="",  @Query("book_id") book_id: Int): Observable<BookInfoGson>

    @GET("/api/auth?key=fpoiKLUues81werht039")
    fun Login(@Query("login") token: String,  @Query("pass") pass: String): Observable<LoginGson>

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