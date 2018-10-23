package ru.profapp.RanobeReader.Network.DTO.RulateDTO

import com.google.gson.annotations.SerializedName

data class ReadyGson(
        @SerializedName("status") val status: String,
        //   val msg: String,
        @SerializedName("response") val books: List<RulateBook> = listOf()
)

data class RulateBook(

        @SerializedName("id") val id: Int? = null,
        @SerializedName("book_id") val bookId: Int? = null,
        @SerializedName("s_title") val sTitle: String? = null,
        @SerializedName("t_title") val tTitle: String? = null,

        @SerializedName("title") val title: String? = null,

        @SerializedName("img") val img: String? = null,
        @SerializedName("ready_date") val readyDate: String? = null,

        @SerializedName("lang") val lang: String? = null,

        //   @SerializedName("n_chapters")
        //   val nChapters: Int? = null,

        @SerializedName("last_activity") val lastActivity: Long? = null,

        @SerializedName("status") val status: String? = null,

        @SerializedName("rating") val rating: String? = null,

        //   val author: String? = null,

        //   val writer: String? = null,

        //   val publisher: String? = null,

        //   val year: String? = null,
        @SerializedName("chapters_total") val chaptersTotal: Int? = null,

        //  val adult: Int? = null,

        //  val team: String? = null,

        @SerializedName("chapters") val chapters: List<RulateChapter> = listOf(),

        @SerializedName("comments") val comments: List<RulateComment> = listOf()

        //  val bookmark: Int? = null

)

data class RulateChapter(

        @SerializedName("id") val id: Int? = null,

        @SerializedName("title") val title: String? = null,

        @SerializedName("status") val status: String? = null,
        @SerializedName("can_read") val canRead: Boolean? = null,

        @SerializedName("new") val new: Boolean? = null

)

data class RulateComment(

        @SerializedName("body") val body: String? = null,

        @SerializedName("time") val time: Long? = null,

        @SerializedName("author") val author: String? = null,

        @SerializedName("avatar") val avatar: String? = null

)

data class RulateText(

        @SerializedName("title") val title: String? = null,

        @SerializedName("text") val text: String? = null,

        @SerializedName("comments") val comments: List<RulateComment> = listOf()

)


