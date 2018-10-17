package ru.profapp.RanobeReader.JsonApi.RulateDTO

import com.google.gson.annotations.SerializedName

data class LoginGson(
        @SerializedName("status") val status: String,
        @SerializedName("msg") val msg: String,
        @SerializedName("response") val response: Login
)

data class Login(
        @SerializedName("id") val id: Int,
        @SerializedName("token") val token: String,
        @SerializedName("login") val login: String
        //   @SerializedName("") val avatar: String,
        //    @SerializedName("") val balance: Int
)
