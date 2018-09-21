package ru.profapp.RanobeReader.JsonApi.Ranoberf

data class RfSearchJson(

        val status: Int? = null,
        val result: List<SearchResult> = ArrayList(),
        val message: String? = null
)

data class SearchResult(
        val id: Int? = null,
        val link: String? = null,
        val value: String? = null,
        val label: String? = null,
        val image: String? = null,
        val part: SearchPart? = null
)

data class SearchPart(
        val link: String? = null,
        val title: String? = null
)
