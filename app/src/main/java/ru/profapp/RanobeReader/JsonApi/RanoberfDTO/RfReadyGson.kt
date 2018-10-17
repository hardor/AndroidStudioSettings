package ru.profapp.RanobeReader.JsonApi.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfGetReadyGson(
        @SerializedName("status") val status: Int?,
        @SerializedName("result") val result: RfResult?,
        @SerializedName("message") val message: String?
)

data class RfResult(
        @SerializedName("sequence") val sequence: List<Sequence> = listOf(),
        //   @SerializedName("") val hasMore: Boolean?,
        @SerializedName("books") val books: List<RfBook> = listOf()
)

data class Sequence(
        @SerializedName("book_id") val book_id: Int?,
        @SerializedName("parts") val parts: String?
)