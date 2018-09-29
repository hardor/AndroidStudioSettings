package ru.profapp.RanobeReader.JsonApi.Ranoberf

data class RfGetReadyGson(
        val status: Int? = null,
        val result: RfResult? = null,
        val message: String? = null
)

data class RfResult(
        val sequence: List<Sequence> = ArrayList(),
        val hasMore: Boolean? = null,
        val books: List<RfBook> = ArrayList()
)


data class Sequence(
        val book_id: Int? = null,
        val parts: String? = null
)