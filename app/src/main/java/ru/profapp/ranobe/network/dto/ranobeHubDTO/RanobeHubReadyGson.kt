package ru.profapp.ranobe.network.dto.ranobeHubDTO

import com.google.gson.annotations.SerializedName

data class RanobeHubReadyGson(@SerializedName("resource") val resource: List<Resource>? = null)

data class Resource(@SerializedName("ranobe") val ranobe: RanobeH? = null, @SerializedName("content") val content: String? = null, @SerializedName(
    "created_at") val createdAt: Long? = null

)

data class RanobeH(@SerializedName("id") val id: Int? = null, @SerializedName("names") val names: Names? = null, @SerializedName(
    "url") val url: String? = null, @SerializedName("poster") val poster: String? = null)

data class Names(@SerializedName("eng") val eng: String? = null, @SerializedName("rus") val rus: String? = null)
