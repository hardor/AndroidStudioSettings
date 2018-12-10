package ru.profapp.ranobe.network.dto.rulateDTO

import com.google.gson.annotations.SerializedName

data class ChapterTextGson(

        @SerializedName("status") val status: String? = null,
        @SerializedName("msg") val msg: String? = null,
        @SerializedName("response") val response: RulateText? = null

)