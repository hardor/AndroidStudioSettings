package ru.profapp.ranobe.Network.DTO.RanobeHubDTO

import com.google.gson.annotations.SerializedName

data class RanobeHubSearchGson(
        @SerializedName("categories") val categories: Categories
)

data class Categories(
        @SerializedName("ranobe") val ranobe: RanobeHub
)

data class RanobeHub(
        @SerializedName("items") val items: List<RanobeHubBook> = listOf(),
        @SerializedName("name") val name: String? = null
)
