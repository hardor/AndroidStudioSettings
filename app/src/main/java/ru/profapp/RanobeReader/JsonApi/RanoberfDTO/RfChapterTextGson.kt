package ru.profapp.RanobeReader.JsonApi.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfChapterTextGson(
        @SerializedName("status") val status: Int?,
        @SerializedName("result") val result: RfText?,
        @SerializedName("message") val message: String?
)

data class RfText(
        @SerializedName("status") val status: Int?,
        //    @SerializedName("") val checkers: List<Checker> = listOf(),
        @SerializedName("part") val part: TextPart?,
        @SerializedName("book") val book: Book?
        //   @SerializedName("") val nextPart: ChapterPart?,
        //  @SerializedName("") val previewPart: ChapterPart?
)

data class Book(
        @SerializedName("id") val id: Int?,
        //  @SerializedName("") val image: ChapterImage?,
        // @SerializedName("") val priceDonatePart: Int?,
        @SerializedName("title") val title: String?,
        @SerializedName("url") val url: String?
)

data class TextPart(
        @SerializedName("id") val id: Int?,
        @SerializedName("url") val url: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("content") val content: String?,
        @SerializedName("payment") val payment: Boolean?,
        @SerializedName("image") val image: String?

        // @SerializedName("") val indexPart: Int?,

        // @SerializedName("") val publishedAt: Int?,
        //  @SerializedName("") val updatedAt: Int?,
        //  @SerializedName("") val donateNeed: Boolean?
)

// data class ChapterImage(
//        @SerializedName("") val mobile: Mobile?
//)

// data class Mobile(
//        @SerializedName("") val image: String?
//)

// data class Checker(
//        @SerializedName("") val id: Int?,
//        @SerializedName("") val selected: String?,
//        @SerializedName("") val bookID: Int?,
//        @SerializedName("") val partID: Int?,
//        @SerializedName("") val userID: Int?,
//        @SerializedName("") val statusID: Int?,
//
//        @SerializedName("") val correct: String?,
//        @SerializedName("") val original: String?,
//        @SerializedName("") val commentModerator: String?,
//        @SerializedName("") val createdAt: Int?,
//        @SerializedName("") val updatedAt: Int?
//)

// data class ChapterPart(
//        @SerializedName("") val title: String?,
//        @SerializedName("") val url: String?
//)
