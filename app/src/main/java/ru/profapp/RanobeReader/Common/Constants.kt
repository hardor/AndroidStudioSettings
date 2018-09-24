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

    enum class RanobeSite(val url: String) {
        None(""),
        Title("Title"),
        Rulate("tl.rulate.ru"),
        RanobeRf("https://xn--80ac9aeh6f.xn--p1ai"),
        RanobeHub("https://ranobehub.org")
    }
}

