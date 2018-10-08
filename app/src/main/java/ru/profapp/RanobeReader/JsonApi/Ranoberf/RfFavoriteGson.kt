package ru.profapp.RanobeReader.JsonApi.Ranoberf

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RfFavoriteGson(
        val status: Int,
        val result: List<FavResult> = listOf(),
        val message: String
)

data class FavResult(
        @SerializedName("id")
        @Expose
        val bookmarkId: Int,
        val bookTitle: String,
        val bookAlias: String,
        val partTitle: String,
        val partAlias: String,
        val bookImage: String,
        val nextPart: Int,
        val allPartsCount: Int,
        val readPartsCount: Int
)
