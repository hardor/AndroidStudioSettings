package ru.profapp.RanobeReader.JsonApi.RulateDTO

import com.google.gson.annotations.SerializedName

data class ChapterTextGson(

        @SerializedName("status") val status: String?,
        @SerializedName("msg") val msg: String?,
        @SerializedName("response") val response: RulateText?

)