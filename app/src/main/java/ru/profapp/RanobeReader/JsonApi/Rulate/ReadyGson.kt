package ru.profapp.RanobeReader.JsonApi.Rulate

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ReadyGson(

        var status: String? = null,

        var msg: String? = null,
        @SerializedName("response")
        @Expose
        var books: List<RulateBook> = ArrayList()
)

data class RulateBook(

        var id: Int? = null,
        @SerializedName("book_id")
        @Expose
        var bookId: Int? = null,
        @SerializedName("s_title")
        @Expose
        var sTitle: String? = null,
        @SerializedName("t_title")
        @Expose
        var tTitle: String? = null,

        var title: String? = null,

        var img: String? = null,
        @SerializedName("ready_date")
        @Expose
        var readyDate: String? = null,

        var lang: String? = null,

        @SerializedName("n_chapters")
        @Expose
        var nChapters: Int? = null,

        @SerializedName("last_activity")
        @Expose
        var lastActivity: Long? = null,

        var status: String? = null,

        var rating: String? = null,

        var author: String? = null,

        var writer: String? = null,

        var publisher: String? = null,

        var year: String? = null,
        @SerializedName("chapters_total")
        @Expose
        var chaptersTotal: Int? = null,

        var adult: Int? = null,

        var team: String? = null,

        var chapters: List<RulateChapter> = ArrayList(),

        var comments: List<RulateComment> = ArrayList(),

        var bookmark: Int? = null

)

data class RulateChapter(

        var id: Int? = null,

        var title: String? = null,

        var status: String? = null,
        @SerializedName("can_read")
        @Expose
        var canRead: Boolean? = null,

        var new: Boolean? = null

)

data class RulateComment(

        var body: String? = null,

        var time: Long? = null,

        var author: String? = null,

        var avatar: String? = null

)

data class RulateText(

        var title: String? = null,

        var text: String? = null,

        var comments: List<RulateComment> = ArrayList()

)


