package ru.profapp.RanobeReader.JsonApi.Rulate

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FavoriteGson(val status: String,
                        val msg: String,
                        val response: List<Response> = listOf()
)

data class Response(
        @SerializedName("s_title")
        @Expose
        val sTitle: String,
        @SerializedName("t_title")
        @Expose
        val tTitle: String,
        @SerializedName("n_chapters")
        @Expose
        val nChapters: Int,
        val lang: String,
        @SerializedName("book_id")
        @Expose
        val bookID: Int
)
