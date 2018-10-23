package ru.profapp.RanobeReader.Network.DTO.RulateDTO

import com.google.gson.annotations.SerializedName

data class BookInfoGson(

        @SerializedName("status") val status: String? = null,

        //@SerializedName("") val msg: String?=null,

        @SerializedName("response") val response: RulateBook? = null

)