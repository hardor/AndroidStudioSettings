package ru.profapp.RanobeReader.Common

/**
 * Created by Ruslan on 12.02.2018.
 */

object Constants {
    // Shared Preference Constants
    const val Rulate_Login_Pref = "preference_rulate_login"
    const val Ranoberf_Login_Pref = "preference_ranoberf_login"
    @Deprecated("Use Last_readed_Pref", ReplaceWith("last_chapter_id_Pref"), DeprecationLevel.WARNING)
    const val is_readed_Pref = "preference_is_readed"
    const val last_chapter_id_Pref = "preference_last_chapter_id"

    const val KEY_Login = "login"
    const val KEY_Token = "token"
    /*--------------------------------------------------------*/

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

    enum class RanobeSite(val url: String, val title: String) {
        Error("Error", "Error"),
        None("", ""),
        Title("Title", "Title"),
        Rulate("tl.rulate.ru", "Rulate"),
        RanobeRf("https://xn--80ac9aeh6f.xn--p1ai", "Ранобэ.рф"),
        RanobeHub("https://ranobehub.org", "RanobeHub");

        companion object {
            fun fromUrl(findValue: String): RanobeSite? = RanobeSite.values().firstOrNull { it.url == findValue }
            fun fromTitle(findValue: String): RanobeSite? = RanobeSite.values().firstOrNull { it.title == findValue }
        }
    }
}

