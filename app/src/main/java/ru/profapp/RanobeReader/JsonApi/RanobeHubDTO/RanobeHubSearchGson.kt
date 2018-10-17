package ru.profapp.RanobeReader.JsonApi.RanobeHubDTO

import com.google.gson.annotations.SerializedName

data class RanobeHubSearchGson(
        @SerializedName("categories") val categories: Categories
)

data class Categories(
        @SerializedName("ranobe") val ranobe: RanobeHub
)

data class RanobeHub(
        @SerializedName("items") val items: List<RanobeHubBook> = listOf(),
        @SerializedName("name") val name: String?
)
