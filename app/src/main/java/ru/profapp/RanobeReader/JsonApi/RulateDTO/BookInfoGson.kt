package ru.profapp.RanobeReader.JsonApi.RulateDTO

import com.google.gson.annotations.SerializedName

data class BookInfoGson(

        @SerializedName("status") val status: String?,

        //@SerializedName("") val msg: String?,

        @SerializedName("response") val response: RulateBook?

)