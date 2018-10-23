package ru.profapp.RanobeReader.Network.DTO.RulateDTO

import com.google.gson.annotations.SerializedName

data class BookmarkGson(

        @SerializedName("status") val status: String? = null,

        @SerializedName("msg") val msg: String? = null

)
