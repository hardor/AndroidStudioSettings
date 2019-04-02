package ru.profapp.ranobe.network.dto.ranobeHubDTO

import com.google.gson.annotations.SerializedName

data class RanobeHubCommentsGson(
        @SerializedName("success")
        val success: Boolean,
        @SerializedName("comments")
        val comments: List<RanobeHubComment> = listOf()
        //val count: Long
)

data class RanobeHubComment(
        //   val id: Long,
        @SerializedName("comment")
        val comment: String,
        @SerializedName("created_at")
        val createdAt: Long,
        //   val commenterID: Long,
        //    val rating: Long,
        @SerializedName("commenter")
        val commenter: Commenter,
        @SerializedName("children")
        val children: List<RanobeHubComment> = listOf()
)

data class Commenter(
        // val id: Long,
        @SerializedName("avatar")
        val avatar: Avatar,
        @SerializedName("name")
        val name: String
        //val email: String
)

data class Avatar(
        //  val big: String,
        @SerializedName("thumb")
        val thumb: String
)
