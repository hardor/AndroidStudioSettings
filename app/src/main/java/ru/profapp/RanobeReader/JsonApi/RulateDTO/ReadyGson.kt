package ru.profapp.RanobeReader.JsonApi.RulateDTO

import com.google.gson.annotations.SerializedName

data class ReadyGson(
        @SerializedName("status") val status: String,
        //  @SerializedName("") val msg: String,

        @SerializedName("response") val books: List<RulateBook> = listOf()
)

data class RulateBook(

        @SerializedName("id") val id: Int?,
        @SerializedName("book_id") val bookId: Int?,
        @SerializedName("s_title") val sTitle: String?,
        @SerializedName("t_title") val tTitle: String?,

        @SerializedName("title") val title: String?,

        @SerializedName("img") val img: String?,
        @SerializedName("ready_date") val readyDate: String?,

        @SerializedName("lang") val lang: String?,

        //   @SerializedName("n_chapters")
        //   @SerializedName("") val nChapters: Int?,

        @SerializedName("last_activity") val lastActivity: Long?,

        @SerializedName("status") val status: String?,

        @SerializedName("rating") val rating: String?,

        //   @SerializedName("") val author: String?,

        //   @SerializedName("") val writer: String?,

        //   @SerializedName("") val publisher: String?,

        //  @SerializedName("") val year: String?,
        @SerializedName("chapters_total") val chaptersTotal: Int?,

        //  @SerializedName("") val adult: Int?,

        //   @SerializedName("") val team: String?,

        @SerializedName("chapters") val chapters: List<RulateChapter> = listOf(),

        @SerializedName("comments") val comments: List<RulateComment> = listOf()

        //  @SerializedName("") val bookmark: Int?

)

data class RulateChapter(

        @SerializedName("id") val id: Int?,

        @SerializedName("title") val title: String?,

        @SerializedName("status") val status: String?,
        @SerializedName("can_read") val canRead: Boolean?,

        @SerializedName("new") val new: Boolean?

)

data class RulateText(

        @SerializedName("title") val title: String?,

        @SerializedName("text") val text: String?,

        @SerializedName("comments") val comments: List<RulateComment> = listOf()

)

data class RulateComment(

        @SerializedName("body") val body: String?,

        @SerializedName("time") val time: Long?,

        @SerializedName("author") val author: String?,

        @SerializedName("avatar") val avatar: String?

)


