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
    override fun setLastChapter(ranobeUrl: String, chapterId: Int) {
        var lastChapterId: Int by PreferenceDelegates<Int>(context, Constants.last_chapter_id_Pref, ranobeUrl, -1)
        lastChapterId = chapterId
    }

    override fun getLastChapter(ranobeUrl: String): Int {
        val lastChapterId: Int by PreferenceDelegates<Int>(context, Constants.last_chapter_id_Pref, ranobeUrl, -1)
        return lastChapterId
    }

    override var sortOrder: String by PreferenceDelegates<String>(context, "", R.string.pref_general_sort_order, Constants.SortOrder.default.name)

    override var isAutoAddBookmark: Boolean by PreferenceDelegates<Boolean>(context, "", R.string.pref_general_auto_bookmark, true)

    override var useVolumeButtonsToScroll: Boolean by PreferenceDelegates<Boolean>(context, "", R.string.pref_general_volume_scroll, false)

    override var isDarkTheme: Boolean by PreferenceDelegates<Boolean>(context, "", R.string.pref_general_app_theme, false)

    override var isFirstStart: Boolean by PreferenceDelegates<Boolean>(context, null, BuildConfig.INTRO_KEY, true)

    override var fontSize: Int by PreferenceDelegates<Int>(context, "", R.string.pref_general_text_size, 13)



}

interface GeneralPreferences {
    var fontSize: Int
    var useVolumeButtonsToScroll: Boolean
    var isAutoAddBookmark: Boolean
    var isDarkTheme: Boolean
    var isFirstStart: Boolean
    var sortOrder: String


    fun setLastChapter(ranobeUrl: String, chapterId: Int)
    fun getLastChapter(ranobeUrl: String): Int

    //rulate

}

class GeneralPreferencesManager(private val mPreferencesProvider: GeneralPreferences) : GeneralPreferences by mPreferencesProvider