package ru.profapp.RanobeReader.JsonApi.RulateDTO

import com.google.gson.annotations.SerializedName

data class BookmarkGson(

        @SerializedName("status") val status: String?,

        @SerializedName("msg") val msg: String?

)
