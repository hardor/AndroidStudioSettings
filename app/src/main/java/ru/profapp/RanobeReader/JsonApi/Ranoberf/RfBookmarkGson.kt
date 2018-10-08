package ru.profapp.RanobeReader.JsonApi.Ranoberf

data class RfBookmarkGson(
        val status: Int,
        val result: List<Any?> = listOf(),
        val message: String
)