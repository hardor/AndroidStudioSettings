package ru.profapp.RanobeReader.JsonApi.RanoberfDTO

import com.google.gson.annotations.SerializedName

data class RfSearchJson(

        @SerializedName("status") val status: Int?,
        @SerializedName("result") val result: List<SearchResult> = listOf(),
        @SerializedName("message") val message: String?
)

data class SearchResult(
        @SerializedName("id") val id: Int?,
        @SerializedName("link") val link: String?,
        @SerializedName("value") val value: String?,
        @SerializedName("label") val label: String?,
        @SerializedName("image") val image: String?
        // @SerializedName("") val part: SearchPart?
)

// data class SearchPart(
//        @SerializedName("") val link: String?,
//        @SerializedName("") val title: String?
//)
