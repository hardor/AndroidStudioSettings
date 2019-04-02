package ru.profapp.ranobe.network.dto.ranoberfDTO

import com.google.gson.annotations.SerializedName

data class RfBookmarkGson(
        @SerializedName("status")
        val status: Int,
        @SerializedName("result")
        val result: List<Any?> = listOf(),
        @SerializedName("message")
        val message: String
)