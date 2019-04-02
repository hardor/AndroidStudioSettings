package ru.profapp.ranobe.pref

import android.content.Context
import android.content.SharedPreferences
import ru.profapp.ranobe.BuildConfig
import ru.profapp.ranobe.R
import ru.profapp.ranobe.common.Constants

/**
 * A helper class for retrieving the general preferences from [SharedPreferences].
 */

class GeneralPreferencesImpl(private val context: Context) : GeneralPreferences {

    override fun setLastChapterUrl(ranobeUrl: String, chapterUrl: String) {
        var lastChapterUrl: String by PreferenceDelegates<String>(context,
            Constants.last_chapter_id_Pref,
            "url_$ranobeUrl",
            "")
        lastChapterUrl = chapterUrl
    }

    override fun getLastChapterUrl(ranobeUrl: String): String {
        val lastChapterUrl: String  by PreferenceDelegates<String>(context,
            Constants.last_chapter_id_Pref,
            "url_$ranobeUrl",
            "")
        return lastChapterUrl
    }

    //Todo: remove 1.19
    override fun getLastChapterId(ranobeUrl: String): Int? {
        val lastChapterId: Int? by PreferenceDelegates<Int?>(context,
            Constants.last_chapter_id_Pref,
            ranobeUrl,
            -1)
        return lastChapterId
    }

    override var sortOrder: String by PreferenceDelegates<String>(context,
        "",
        R.string.pref_general_sort_order,
        Constants.SortOrder.default.name)

    override var rulateToken: String by PreferenceDelegates<String>(context,
        Constants.Rulate_Login_Pref,
        Constants.KEY_Token,
        "")
    override var ranoberfToken: String by PreferenceDelegates<String>(context,
        Constants.Ranoberf_Login_Pref,
        Constants.KEY_Token,
        "")

    override var rulateLogin: String by PreferenceDelegates<String>(context,
        Constants.Rulate_Login_Pref,
        Constants.KEY_Login,
        "")
    override var ranoberfLogin: String by PreferenceDelegates<String>(context,
        Constants.Ranoberf_Login_Pref,
        Constants.KEY_Login,
        "")

    override var isAutoAddBookmark: Boolean by PreferenceDelegates<Boolean>(context,
        "",
        R.string.pref_general_auto_bookmark,
        true)

    override var useVolumeButtonsToScroll: Boolean by PreferenceDelegates<Boolean>(context,
        "",
        R.string.pref_general_volume_scroll,
        false)

    override var isDarkTheme: Boolean by PreferenceDelegates<Boolean>(context,
        "",
        R.string.pref_general_app_theme,
        false)

    override var isFirstStart: Boolean by PreferenceDelegates<Boolean>(context,
        null,
        BuildConfig.INTRO_KEY,
        true)

    override var fontSize: Int by PreferenceDelegates<Int>(context,
        "",
        R.string.pref_general_text_size,
        13)

    override var useSwipeForNavigate: Boolean by PreferenceDelegates<Boolean>(context,
        "",
        R.string.pref_general_swipe_navigate,
        false)

    override var font: String by PreferenceDelegates<String>(context,
        "",
        R.string.pref_general_font,
        Constants.CustomFonts.Default.title)

    override var textColor: Int? by PreferenceDelegates<Int?>(context,
        "",
        R.string.pref_general_text_color,
        -1)

    override var backgroundColor: Int? by PreferenceDelegates<Int?>(context,
        "",
        R.string.pref_general_background_color,
        -1)

    override var lineHeightCss: Int by PreferenceDelegates<Int>(context,
        "",
        R.string.pref_general_lineheight_web,
        12)

    override var isPremium: Boolean by PreferenceDelegates<Boolean>(context,
        "",
        R.string.pref_general_is_premium,
        false)

    override var keepScreenOn: Boolean by PreferenceDelegates<Boolean>(context,
        "",
        R.string.pref_general_keep_screen_on,
        false)

}

interface GeneralPreferences {
    var fontSize: Int
    var useVolumeButtonsToScroll: Boolean
    var isAutoAddBookmark: Boolean
    var isDarkTheme: Boolean
    var isFirstStart: Boolean
    var sortOrder: String
    var useSwipeForNavigate: Boolean
    var rulateToken: String
    var ranoberfToken: String
    var rulateLogin: String
    var ranoberfLogin: String
    var font: String
    var textColor: Int?
    var backgroundColor: Int?
    var lineHeightCss: Int
    var isPremium: Boolean
    var keepScreenOn: Boolean


    fun setLastChapterUrl(ranobeUrl: String, chapterUrl: String)

    fun getLastChapterUrl(ranobeUrl: String): String

    // Todo: remove 1.19
    fun getLastChapterId(ranobeUrl: String): Int?

}

class GeneralPreferencesManager(private val mPreferencesProvider: GeneralPreferences) :
    GeneralPreferences by mPreferencesProvider