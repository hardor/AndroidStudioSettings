package ru.profapp.RanobeReader.Network.DTO.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfGetReadyGson(
        @SerializedName("status") val status: Int? = null,
        @SerializedName("result") val result: RfResult? = null,
        @SerializedName("message") val message: String? = null
)

data class RfResult(
        @SerializedName("sequence") val sequence: List<Sequence> = listOf(),
        //   @SerializedName("") val hasMore: Boolean?=null,
        @SerializedName("books") val books: List<RfBook> = listOf()
)

data class Sequence(
        @SerializedName("book_id") val book_id: Int? = null,
        @SerializedName("parts") val parts: String? = null
)