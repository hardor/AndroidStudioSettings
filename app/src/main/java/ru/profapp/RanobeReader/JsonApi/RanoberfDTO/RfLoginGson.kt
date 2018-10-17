package ru.profapp.RanobeReader.JsonApi.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfLoginGson(
        @SerializedName("status") val status: Int,
        @SerializedName("result") val result: Result,
        @SerializedName("message") val message: String
)

data class Result(
        @SerializedName("token") val token: String
)
