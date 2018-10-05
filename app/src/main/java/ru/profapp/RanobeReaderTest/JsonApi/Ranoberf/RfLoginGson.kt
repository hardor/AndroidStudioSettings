package ru.profapp.RanobeReaderTest.JsonApi.Ranoberf

data class RfLoginGson(
        val status: Int,
        val result: Result,
        val message: String
)

data class Result(
        val token: String
)
