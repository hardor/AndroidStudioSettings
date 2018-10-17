package ru.profapp.RanobeReader.JsonApi.RanobeHubDTO

import com.google.gson.annotations.SerializedName

data class RanobeHubBook(

        @SerializedName("id") val id: Int?,
        @SerializedName("name_rus") val nameRus: String?,
        @SerializedName("name_eng") val nameEng: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("chapters") val chapters: Int?,
        @SerializedName("rating") val rating: Int?,
        @SerializedName("changed_at") val changedAt: String?,
        @SerializedName("url") val url: String?,
        @SerializedName("image") val image: String?

        //        @SerializedName("name_original") val nameOriginal: String?,
        //        @SerializedName("name_others") val nameOthers: Any?,
        //        @SerializedName("id_context") val idContext: String?,
        //        @SerializedName("id_original") val idOriginal: String?,
        //        @SerializedName("year") val year: Int?,
        //        @SerializedName("id_status") val idStatus: Int?,
        //        @SerializedName("volumes") val volumes: Int?,
        //        @SerializedName("count_download") val countDownload: Int?,
        //        @SerializedName("views") val views: Int?,
        //        @SerializedName("chapters_origin") val chaptersOrigin: String?,
        //        @SerializedName("is_prerelease") val isPrerelease: Int?,
        //        @SerializedName("is_blocked") val isBlocked: Int?,
        //        @SerializedName("created_at") val createdAt: String?,
        //        @SerializedName("updated_at") val updatedAt: String?,
        //        @SerializedName("deleted_at") val deletedAt: String?,

)
