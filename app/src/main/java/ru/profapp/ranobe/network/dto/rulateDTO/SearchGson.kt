package ru.profapp.ranobe.network.dto.rulateDTO

import com.google.gson.annotations.SerializedName

//status	"success"
//msg	""
//response
//    0
//        id	293
//        s_title	"Limitless Sword God"
//        t_title	"Безграничный меч Бога"
//        n_chapters	528
//        lang	"с английского на русский"
//        last_activity	1544788285
//        status	"В работе"
//        rating	"4.4 (на основе 354 голосов)"
//        author	"Kent"
//        ready	"100%"
//        ac_read	"a"
//        ac_gen	"a"
//        ac_tr	"g"

data class SearchGson(
        @SerializedName("status")
        val status: String,
        @SerializedName("response")
        val books: List<RulateSearchBook> = listOf()
)

data class RulateSearchBook(

        @SerializedName("id")
        val id: Int? = null,

        @SerializedName("s_title")
        val sTitle: String? = null,

        @SerializedName("t_title")
        val tTitle: String? = null,

        @SerializedName("n_chapters")
        val nChapters: Int? = null,

        @SerializedName("lang")
        val lang: String? = null,

        @SerializedName("last_activity")
        val lastActivity: Long? = null,

        @SerializedName("status")
        val status: String? = null,

        @SerializedName("rating")
        val rating: String? = null,

        @SerializedName("ready")
        val ready: String? = null
)