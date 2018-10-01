package ru.profapp.RanobeReader.Common

/**
 * Created by Ruslan on 12.02.2018.
 */

object Constants {
    const val fragmentBundle = "fragmentType"
    const val chaptersNum = 4

    enum class FragmentType {
        Favorite,
        Rulate,
        Ranoberf,
        RanobeHub,
        Search,
        Saved,
        History
    }

    enum class JsonObjectFrom {
        RulateGetReady,
        RulateGetBookInfo,
        RulateGetChapterText,
        RanobeRfGetReady,
        RanobeRfGetBookInfo,
        RanobeRfGetChapterText,
        RanobeRfSearch,
        RulateSearch,
        RanobeHubSearch,
        RulateFavorite
    }

    enum class RanobeSite(val url: String, val title:String) {
        None("",""),
        Title("Title","Title"),
        Rulate("tl.rulate.ru","Rulate"),
        RanobeRf("https://xn--80ac9aeh6f.xn--p1ai","Ранобэ.рф"),
        RanobeHub("https://ranobehub.org","RanobeHub")
    }
}

