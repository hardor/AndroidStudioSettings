package ru.profapp.RanobeReader.JsonApi.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfBookInfoGson(
        @SerializedName("status") val status: Int?,
        @SerializedName("result") val result: ResultBookInfo?,
        @SerializedName("message") val message: String?
)

data class ResultBookInfo(
        @SerializedName("book") val book: RfBook?,
        @SerializedName("parts") val parts: List<RfChapter> = listOf(),
        @SerializedName("donateParts") val donateParts: List<RfChapter> = listOf(),
        @SerializedName("genres") val genres: List<Genre> = listOf()

        // @SerializedName("") val timerPart: TimerPart?,
        //  @SerializedName("") val readPart: String?
)

data class RfBook(
        @SerializedName("id") val id: Int?,
        @SerializedName("url") val url: String?,
        @SerializedName("fullTitle") val fullTitle: String?,

        @SerializedName("title") val title: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("info") val info: String?,
        @SerializedName("image") val image: Image?,

        @SerializedName("likes") val likes: Int = 0,
        @SerializedName("dislikes") val dislikes: Int = 0,

        @SerializedName("view") val view: Int?,
        @SerializedName("lastUpdatedBook") val lastUpdatedBook: Long?,

        @SerializedName("parts") val parts: List<RfChapter> = listOf()

        //    @SerializedName("") val author: String?,
        // @SerializedName("") val priceDonatePart: Int?,
        //  @SerializedName("") val donate: Int?,
        //  @SerializedName("") val donateNeed: Int?,
        //  @SerializedName("") val donateCurrent: Int?,
        //  @SerializedName("") val publishedAt: Long?,
        //  @SerializedName("") val user: User?,
        //   @SerializedName("") val country: Country?,
        //    @SerializedName("") val activeUsers: Int?,

)

data class Image(
        @SerializedName("desktop") val desktop: Desktop?
        //   @SerializedName("") val mobile: Desktop?
)

data class Desktop(
        @SerializedName("image") val image: String?
        //  @SerializedName("") val svg: String?
)

data class RfChapter(
        @SerializedName("id") val id: Int?,
        @SerializedName("title") val title: String?,
        @SerializedName("url") val url: String?,
        @SerializedName("payment") val payment: Boolean = false,
        @SerializedName("partDonate") val partDonate: Boolean = false,
        @SerializedName("publishedAt") val publishedAt: Long?,
        @SerializedName("sponsor") val sponsor: Boolean = false,
        // @SerializedName("") val userDonate: Boolean?,
        @SerializedName("partNumber") val partNumber: String?,
        @SerializedName("view") val view: Int?
)

data class Genre(
        @SerializedName("title") val title: String?
)

// data class Country(
//        @SerializedName("") val title: String?,
//        @SerializedName("") val alias: String?,
//        @SerializedName("") val image: String?
//)
//
// data class User(
//        @SerializedName("") val like: Int?,
//        @SerializedName("") val dislike: Int?
//)

// data class TimerPart(
//        @SerializedName("") val publishedAt: Int?
//)
