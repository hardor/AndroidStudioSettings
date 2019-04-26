package ru.profapp.ranobe.network.dto.ranobeHubDTO

import com.google.gson.annotations.SerializedName

data class RanobeHubSearchGson(
        @SerializedName("data")
        val data: List<RanobeHubSearchItem> = mutableListOf()
)

data class RanobeHubSearchItem(
        @SerializedName("id")
        val id: Int? = null,
        @SerializedName("names")
        val names: Names? = null,
        @SerializedName("description")
        val description: String? = null,
        @SerializedName("url")
        val url: String? = null,
        @SerializedName("image")
        val image: String? = null
)

