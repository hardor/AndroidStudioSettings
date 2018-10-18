package ru.profapp.RanobeReader.JsonApi.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfBookInfoGson(
        @SerializedName("status") val status: Int? = null,
        @SerializedName("result") val result: ResultBookInfo? = null,
        @SerializedName("message") val message: String? = null
)

data class ResultBookInfo(
        @SerializedName("book") val book: RfBook? = null,
        @SerializedName("parts") val parts: List<RfChapter> = listOf(),
        @SerializedName("donateParts") val donateParts: List<RfChapter> = listOf(),
        @SerializedName("genres") val genres: List<Genre> = listOf()

        // @SerializedName("") val timerPart: TimerPart?=null,
        //  @SerializedName("") val readPart: String?
)

data class RfBook(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("url") val url: String? = null,
        @SerializedName("fullTitle") val fullTitle: String? = null,

        @SerializedName("title") val title: String? = null,
        @SerializedName("description") val description: String? = null,
        @SerializedName("info") val info: String? = null,
        @SerializedName("image") val image: Image? = null,

        @SerializedName("likes") val likes: Int = 0,
        @SerializedName("dislikes") val dislikes: Int = 0,

        @SerializedName("view") val view: Int? = null,
        @SerializedName("lastUpdatedBook") val lastUpdatedBook: Long? = null,

        @SerializedName("parts") val parts: List<RfChapter> = listOf()

        //    @SerializedName("") val author: String?=null,
        // @SerializedName("") val priceDonatePart: Int?=null,
        //  @SerializedName("") val donate: Int?=null,
        //  @SerializedName("") val donateNeed: Int?=null,
        //  @SerializedName("") val donateCurrent: Int?=null,
        //  @SerializedName("") val publishedAt: Long?=null,
        //  @SerializedName("") val user: User?=null,
        //   @SerializedName("") val country: Country?=null,
        //    @SerializedName("") val activeUsers: Int?=null,

)

data class Image(
        @SerializedName("desktop") val desktop: Desktop? = null
        //   @SerializedName("") val mobile: Desktop?=null
)

data class Desktop(
        @SerializedName("image") val image: String? = null
        //  @SerializedName("") val svg: String?=null
)

data class RfChapter(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("title") val title: String? = null,
        @SerializedName("url") val url: String? = null,
        @SerializedName("payment") val payment: Boolean = false,
        @SerializedName("partDonate") val partDonate: Boolean = false,
        @SerializedName("publishedAt") val publishedAt: Long? = null,
        @SerializedName("sponsor") val sponsor: Boolean = false,
        // @SerializedName("") val userDonate: Boolean?=null,
        @SerializedName("partNumber") val partNumber: String? = null,
        @SerializedName("view") val view: Int? = null
)

data class Genre(
        @SerializedName("title") val title: String? = null
)

// data class Country(
//        @SerializedName("") val title: String?=null,
//        @SerializedName("") val alias: String?=null,
//        @SerializedName("") val image: String?=null
//)
//
// data class User(
//        @SerializedName("") val like: Int?=null,
//        @SerializedName("") val dislike: Int?=null
//)

// data class TimerPart(
//        @SerializedName("") val publishedAt: Int?=null
//)
