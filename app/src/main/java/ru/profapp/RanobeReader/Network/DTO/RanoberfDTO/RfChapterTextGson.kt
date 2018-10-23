package ru.profapp.RanobeReader.Network.DTO.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfChapterTextGson(
        @SerializedName("status") val status: Int? = null,
        @SerializedName("result") val result: RfText? = null,
        @SerializedName("message") val message: String? = null
)

data class RfText(
        @SerializedName("status") val status: Int? = null,
        //    @SerializedName("") val checkers: List<Checker> = listOf(),
        @SerializedName("part") val part: TextPart? = null,
        @SerializedName("book") val book: Book? = null
        //   @SerializedName("") val nextPart: ChapterPart?=null,
        //  @SerializedName("") val previewPart: ChapterPart?
)

data class Book(
        @SerializedName("id") val id: Int? = null,
        //  @SerializedName("") val image: ChapterImage?=null,
        // @SerializedName("") val priceDonatePart: Int?=null,
        @SerializedName("title") val title: String? = null,
        @SerializedName("url") val url: String? = null
)

data class TextPart(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("url") val url: String? = null,
        @SerializedName("title") val title: String? = null,
        @SerializedName("content") val content: String? = null,
        @SerializedName("payment") val payment: Boolean? = null,
        @SerializedName("image") val image: String? = null

        // @SerializedName("") val indexPart: Int?=null,

        // @SerializedName("") val publishedAt: Int?=null,
        //  @SerializedName("") val updatedAt: Int?=null,
        //  @SerializedName("") val donateNeed: Boolean?
)

// data class ChapterImage(
//        @SerializedName("") val mobile: Mobile?
//)

// data class Mobile(
//        @SerializedName("") val image: String?
//)

// data class Checker(
//        @SerializedName("") val id: Int?=null,
//        @SerializedName("") val selected: String?=null,
//        @SerializedName("") val bookID: Int?=null,
//        @SerializedName("") val partID: Int?=null,
//        @SerializedName("") val userID: Int?=null,
//        @SerializedName("") val statusID: Int?=null,
//
//        @SerializedName("") val correct: String?=null,
//        @SerializedName("") val original: String?=null,
//        @SerializedName("") val commentModerator: String?=null,
//        @SerializedName("") val createdAt: Int?=null,
//        @SerializedName("") val updatedAt: Int?
//)

// data class ChapterPart(
//        @SerializedName("") val title: String?=null,
//        @SerializedName("") val url: String?
//)
