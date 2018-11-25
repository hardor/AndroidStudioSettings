package ru.profapp.ranobe.Network.DTO.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfFavoriteGson(
        @SerializedName("status") val status: Int,
        @SerializedName("result") val result: List<FavResult> = listOf(),
        @SerializedName("message") val message: String
)

data class FavResult(
        @SerializedName("id") val bookmarkId: Int,
        @SerializedName("bookTitle") val bookTitle: String,
        @SerializedName("bookAlias") val bookAlias: String,
        @SerializedName("bookImage") val bookImage: String,
        @SerializedName("allPartsCount") val allPartsCount: Int

        //        @SerializedName("") val nextPart: Int,
        //        @SerializedName("") val readPartsCount: Int
        //        @SerializedName("") val partTitle: String,
        //        @SerializedName("") val partAlias: String,
)
