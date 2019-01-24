package ru.profapp.ranobe.network.dto.ranoberfDTO

import com.google.gson.annotations.SerializedName

data class RfUserGson(

        @SerializedName("status")
        var status: Int? = null,
        @SerializedName("result")
        var result: ResultUser? = null,
        @SerializedName("message")
        var message: String? = null

)

data class ResultUser(

        @SerializedName("userId")
        var userId: Int? = null,
        @SerializedName("username")
        var username: String? = null,
        @SerializedName("role")
        var role: String? = null,
        @SerializedName("paymentStatus")
        var paymentStatus: String? = null,
        @SerializedName("paymentContinue")
        var paymentContinue: Long? = null,
        @SerializedName("donates")
        var donates: Double? = null,
        @SerializedName("checkerBlock")
        var checkerBlock: Any? = null

)