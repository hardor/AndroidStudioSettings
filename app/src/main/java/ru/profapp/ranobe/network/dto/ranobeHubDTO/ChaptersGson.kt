package ru.profapp.ranobe.network.dto.ranobeHubDTO

import com.google.gson.annotations.SerializedName

data class ChaptersGson(
        @SerializedName("data")
        val data: List<Volumes> = listOf()
)

data class Volumes(
        @SerializedName("id")
        val id: Int,
        @SerializedName("num")
        val num: Double,
        @SerializedName("name")
        val name: String,
        @SerializedName("chapters")
        val chapters: List<tChapter> = listOf()

        //        @SerializedName("idStatus") val idStatus: Int,
        //        @SerializedName("idRanobe") val idRanobe: Int,
        //        @SerializedName("statusOriginal") val statusOriginal: Any?=null,
        //        @SerializedName("idOriginal") val idOriginal: Any?=null,
        //        @SerializedName("idRecheckStatus") val idRecheckStatus: Any?=null,
        //        @SerializedName("imageStorage") val imageStorage: String,
        //        @SerializedName("createdAt") val createdAt: String,
        //        @SerializedName("updatedAt") val updatedAt: String,
        //        @SerializedName("statusName") val statusName: Status

)

data class tChapter(
        @SerializedName("id")
        val id: Int,
        @SerializedName("num")
        val num: Double,
        @SerializedName("name")
        val name: String,
        @SerializedName("url")
        val url: String? = null
        //        @SerializedName("idRanobe") val idRanobe: Int,
        //        @SerializedName("numVolume") val numVolume: Int,
        //        @SerializedName("idOriginal") val idOriginal: Any?=null,
        //        @SerializedName("createdAt") val createdAt: String
)

data class Status(
        @SerializedName("name")
        val name: String
)
