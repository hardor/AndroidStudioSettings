package ru.profapp.RanobeReader.JsonApi.Ranoberf


data class RfChapterTextGson (
        val status: Int? = null,
        val result: RfText? = null,
        val message: String? = null
)

data class RfText (
        val status: Int? = null,
        val checkers: List<Checker> = ArrayList(),
        val part: TextPart? = null,
        val book: Book? = null,
        val nextPart: ChapterPart? = null,
        val previewPart: ChapterPart? = null
)

data class Book (
        val id: Int? = null,
        val image: ChapterImage? = null,
        val priceDonatePart: Int? = null,
        val title: String? = null,
        val url: String? = null
)

data class ChapterImage (
        val mobile: Mobile? = null
)

data class Mobile (
        val image: String? = null
)

data class Checker (
        val id: Int? = null,
        val bookID: Int? = null,
        val partID: Int? = null,
        val userID: Int? = null,
        val statusID: Int? = null,
        val selected: String? = null,
        val correct: String? = null,
        val original: String? = null,
        val commentModerator: String? = null,
        val createdAt: Int? = null,
        val updatedAt: Int? = null
)

data class ChapterPart (
        val title: String? = null,
        val url: String? = null
)

data class TextPart (
        val id: Int? = null,
        val url: String? = null,
        val title: String? = null,
        val indexPart: Int? = null,
        val content: String? = null,
        val payment: Boolean? = null,
        val image: String? = null,
        val publishedAt: Int? = null,
        val updatedAt: Int? = null,
        val donateNeed: Boolean? = null
)

