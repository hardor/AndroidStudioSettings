package ru.profapp.ranobe.common

import android.content.Context
import ru.profapp.ranobe.R

/**
 * Created by Ruslan on 12.02.2018.
 */

object Constants {
    // Shared Preference Constants
    const val Rulate_Login_Pref = "preference_rulate_login"
    const val Ranoberf_Login_Pref = "preference_ranoberf_login"
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
        }
    }

    enum class SortOrder(val resId: Int) {
        ByTitle(R.string.byTitle),
        ByDate(R.string.byDate),
        ByUpdates(R.string.byUpdates),
        ByRanobeSite(R.string.byRanobeSite);

        companion object {
            val default: SortOrder = ByTitle
            fun toArray(context: Context): Array<Int> {
                return SortOrder.values().map {
                    return@map it.resId
                }.toTypedArray()
            }

            fun fromResId(findValue: Int): SortOrder = SortOrder.values().firstOrNull { it.resId == findValue }
                    ?: SortOrder.default
        }

    }

    enum class ApplicationConstants {
        DRAWABLE_LEFT,

        DRAWABLE_RIGHT,

        DRAWABLE_TOP,

        DRAWABLE_BOTTOM;

        companion object {
            fun valueOf(string: String?): ApplicationConstants {

                if (string.isNullOrEmpty())
                    return DRAWABLE_LEFT

                return try {
                    ApplicationConstants.valueOf(string)
                } catch (ex: IllegalArgumentException) {
                    DRAWABLE_LEFT
                }
            }

        }
    }

}

