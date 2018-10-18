package ru.profapp.RanobeReader.JsonApi.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfSearchJson(

        @SerializedName("status") val status: Int? = null,
        @SerializedName("result") val result: List<SearchResult> = listOf(),
        @SerializedName("message") val message: String? = null
)

data class SearchResult(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("link") val link: String? = null,
        @SerializedName("value") val value: String? = null,
        @SerializedName("label") val label: String? = null,
        @SerializedName("image") val image: String? = null
        // @SerializedName("") val part: SearchPart?
)

// data class SearchPart(
//        @SerializedName("") val link: String?=null,
//        @SerializedName("") val title: String?
//)
