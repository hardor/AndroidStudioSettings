package ru.profapp.ranobe.Network.DTO.RulateDTO

import com.google.gson.annotations.SerializedName

data class ChapterTextGson(

        @SerializedName("status") val status: String? = null,
        @SerializedName("msg") val msg: String? = null,
        @SerializedName("response") val response: RulateText? = null

)