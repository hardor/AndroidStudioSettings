package ru.profapp.ranobe.network.dto.rulateDTO

import com.google.gson.annotations.SerializedName

data class FavoriteGson(
        @SerializedName("status") val status: String,
        // @SerializedName("") val msg: String,
        @SerializedName("response") val favResponse: List<FavResponse> = listOf()
)

data class FavResponse(
        @SerializedName("s_title") val sTitle: String,

        @SerializedName("t_title") val tTitle: String,

        @SerializedName("n_chapters") val nChapters: Int,

        @SerializedName("lang") val lang: String,

        @SerializedName("book_id") val bookID: Int
)
