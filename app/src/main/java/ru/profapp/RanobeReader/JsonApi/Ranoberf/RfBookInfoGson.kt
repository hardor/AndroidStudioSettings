package ru.profapp.RanobeReader.JsonApi.Ranoberf


data class RfBookInfoGson(
        val status: Int? = null,
        val result: ResultBookInfo? = null,
        val message: String? = null
)

data class ResultBookInfo(
        val book: RfBook? = null,
        val parts: List<RfChapter> = listOf(),
        val donateParts: List<RfChapter> = listOf(),
        val genres: List<Genre> = listOf(),
        val timerPart: TimerPart? = null,
        val readPart: String? = null
)

data class RfBook(
        val id: Int? = null,
        val url: String? = null,
        val fullTitle: String? = null,
        val author: String? = null,
        val title: String? = null,
        val description: String? = null,
        val info: String? = null,
        val image: Image? = null,
        val priceDonatePart: Int? = null,
        val donate: Int? = null,
        val donateNeed: Int? = null,
        val donateCurrent: Int? = null,
        val likes: Int = 0,
        val dislikes: Int = 0,
        val publishedAt: Long? = null,
        //  val user: User? = null,
        val country: Country? = null,
        val view: Int? = null,
        val lastUpdatedBook: Long? = null,
        //    val activeUsers: Int? = null,
        val parts: List<RfChapter> = listOf()
)

data class Country(
        val title: String? = null,
        val alias: String? = null,
        val image: String? = null
)

data class Image(
        val desktop: Desktop? = null,
        val mobile: Desktop? = null
)

data class Desktop(
        val image: String? = null,
        val svg: String? = null
)

data class User(
        val like: Int? = null,
        val dislike: Int? = null
)

data class RfChapter(
        val id: Int? = null,
        val title: String? = null,
        val url: String? = null,
        val payment: Boolean = false,
        val partDonate: Boolean = false,
        val publishedAt: Long? = null,
        val sponsor: Boolean = false,
        val userDonate: Boolean? = null,
        val partNumber: String? = null,
        val view: Int? = null
)

data class Genre(
        val title: String? = null
)

data class TimerPart(
        val publishedAt: Int? = null
)
