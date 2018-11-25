package ru.profapp.ranobe.Network.DTO.RanobeHubDTO

import com.google.gson.annotations.SerializedName

data class RanobeHubBook(

        @SerializedName("id") val id: Int? = null,
        @SerializedName("name_rus") val nameRus: String? = null,
        @SerializedName("name_eng") val nameEng: String? = null,
        @SerializedName("description") val description: String? = null,
        @SerializedName("chapters") val chapters: Int? = null,
        @SerializedName("rating") val rating: Int? = null,
        @SerializedName("changed_at") val changedAt: String? = null,
        @SerializedName("url") val url: String? = null,
        @SerializedName("image") val image: String? = null

        //        @SerializedName("name_original") val nameOriginal: String?=null,
        //        @SerializedName("name_others") val nameOthers: Any?=null,
        //        @SerializedName("id_context") val idContext: String?=null,
        //        @SerializedName("id_original") val idOriginal: String?=null,
        //        @SerializedName("year") val year: Int?=null,
        //        @SerializedName("id_status") val idStatus: Int?=null,
        //        @SerializedName("volumes") val volumes: Int?=null,
        //        @SerializedName("count_download") val countDownload: Int?=null,
        //        @SerializedName("views") val views: Int?=null,
        //        @SerializedName("chapters_origin") val chaptersOrigin: String?=null,
        //        @SerializedName("is_prerelease") val isPrerelease: Int?=null,
        //        @SerializedName("is_blocked") val isBlocked: Int?=null,
        //        @SerializedName("created_at") val createdAt: String?=null,
        //        @SerializedName("updated_at") val updatedAt: String?=null,
        //        @SerializedName("deleted_at") val deletedAt: String?=null,

)
