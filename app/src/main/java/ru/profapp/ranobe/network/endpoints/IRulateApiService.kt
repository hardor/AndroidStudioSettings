package ru.profapp.ranobe.network.endpoints

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import ru.profapp.ranobe.network.dto.rulateDTO.*

interface IRulateApiService {

    @GET("/api/getReady")
    fun GetReadyBooks(@Query("page") page: Int): Single<ReadyGson>

    @GET("/api/searchBooks")
    fun SearchBooks(@Query("search") search: String): Single<SearchGson>

    @GET("/api/bookmarks")
    fun GetFavoriteBooks(@Query("token") token: String): Single<FavoriteGson>

    @GET("/api/chapter")
    fun GetChapterText(@Query("token") token: String, @Query("chapter_id") chapter_id: Int?, @Query(
        "book_id") book_id: Int?): Single<ChapterTextGson>

    @GET("/api/addBookmark")
    fun AddBookmark(@Query("token") token: String, @Query("book_id") book_id: Int?): Single<BookmarkGson>

    @GET("/api/removeBookmark")
    fun RemoveBookmark(@Query("token") token: String, @Query("book_id") book_id: Int?): Single<BookmarkGson>

    @GET("/api/book")
    fun GetBookInfo(@Query("token") token: String = "", @Query("book_id") book_id: Int?): Single<BookInfoGson>

    @GET("/api/auth")
    fun Login(@Query("login") login: String, @Query("pass") pass: String): Single<LoginGson>

}