package ru.profapp.RanobeReader.JsonApi.RanobeHubDTO

import com.google.gson.annotations.SerializedName

data class ChaptersGson(
        @SerializedName("data") val data: List<Volumes>
)

data class Volumes(
        @SerializedName("id") val id: Int,
        @SerializedName("num") val num: String,
        @SerializedName("name") val name: String,
        @SerializedName("chapters") val chapters: List<tChapter> = listOf()

        //        @SerializedName("idStatus") val idStatus: Int,
        //        @SerializedName("idRanobe") val idRanobe: Int,
        //        @SerializedName("statusOriginal") val statusOriginal: Any?,
        //        @SerializedName("idOriginal") val idOriginal: Any?,
        //        @SerializedName("idRecheckStatus") val idRecheckStatus: Any?,
        //        @SerializedName("imageStorage") val imageStorage: String,
        //        @SerializedName("createdAt") val createdAt: String,
        //        @SerializedName("updatedAt") val updatedAt: String,
        //        @SerializedName("statusName") val statusName: Status

)

data class tChapter(
        @SerializedName("id") val id: Int,
        @SerializedName("num") val num: String,
        @SerializedName("name") val name: String
        //        @SerializedName("idRanobe") val idRanobe: Int,
        //        @SerializedName("numVolume") val numVolume: Int,
        //        @SerializedName("idOriginal") val idOriginal: Any?,
        //        @SerializedName("createdAt") val createdAt: String
)

data class Status(
        @SerializedName("name") val name: String
)
