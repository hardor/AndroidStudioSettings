package ru.profapp.RanobeReader.JsonApi.Ranoberf

data class RfGetReadyGson(
        val status: Int? = null,
        val result: RfResult? = null,
        val message: String? = null
)

data class RfResult(
        val sequence: List<Sequence> = listOf(),
        val hasMore: Boolean? = null,
        val books: List<RfBook> = listOf()
)


data class Sequence(
        val book_id: Int? = null,
        val parts: String? = null
)