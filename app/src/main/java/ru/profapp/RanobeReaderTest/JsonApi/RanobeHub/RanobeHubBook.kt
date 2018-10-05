package ru.profapp.RanobeReaderTest.JsonApi.RanobeHub

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RanobeHubBook(

        val id: Int? = null,
        @SerializedName("name_rus")
        @Expose
        val nameRus: String? = null,
        @SerializedName("name_eng")
        @Expose
        val nameEng: String? = null,
        @SerializedName("name_original")
        @Expose
        val nameOriginal: String? = null,
        @SerializedName("name_others")
        @Expose
        val nameOthers: Any? = null,
        @SerializedName("id_context")
        @Expose
        val idContext: String? = null,
        @SerializedName("id_original")
        @Expose
        val idOriginal: String? = null,
        val description: String? = null,
        val year: Int? = null,
        @SerializedName("id_status")
        @Expose
        val idStatus: Int? = null,
        val volumes: Int? = null,
        val chapters: Int? = null,
        val rating: Int? = null,
        @SerializedName("count_download")
        @Expose
        val countDownload: Int? = null,
        val views: Int? = null,
        @SerializedName("chapters_origin")
        @Expose
        val chaptersOrigin: String? = null,
        @SerializedName("is_prerelease")
        @Expose
        val isPrerelease: Int? = null,
        @SerializedName("is_blocked")
        @Expose
        val isBlocked: Int? = null,
        @SerializedName("created_at")
        @Expose
        val createdAt: String? = null,
        @SerializedName("updated_at")
        @Expose
        val updatedAt: String? = null,
        @SerializedName("changed_at")
        @Expose
        val changedAt: String? = null,
        @SerializedName("deleted_at")
        @Expose
        val deletedAt: String? = null,
        val url: String? = null,
        val image: String? = null


)
