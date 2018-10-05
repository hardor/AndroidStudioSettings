package ru.profapp.RanobeReaderTest.JsonApi.RanobeHub

data class RanobeHubSearchGson(
        val categories: Categories
)

data class Categories(
        val ranobe: RanobeHub
)

data class RanobeHub(
        val items: List<RanobeHubBook> = listOf(),
        val name: String? = null
)
