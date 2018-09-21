package ru.profapp.RanobeReader.JsonApi.RanobeHub

data class RanobeHubSearchGson (
        val categories: Categories
)

data class Categories (
        val ranobe: RanobeHub
)

data class RanobeHub (
        val items: List<RanobeHubBook> = ArrayList(),
        val name: String? = null
)