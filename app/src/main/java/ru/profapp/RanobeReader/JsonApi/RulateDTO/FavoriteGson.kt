package ru.profapp.RanobeReader.JsonApi.RulateDTO

import com.google.gson.annotations.SerializedName

data class FavoriteGson(
        @SerializedName("status") val status: String,
        // @SerializedName("") val msg: String,
        @SerializedName("response") val response: List<Response> = listOf()
)

data class Response(
        @SerializedName("s_title") val sTitle: String,

        @SerializedName("t_title") val tTitle: String,

        @SerializedName("n_chapters") val nChapters: Int,

        @SerializedName("lang") val lang: String,

        @SerializedName("book_id") val bookID: Int
)
