package ru.profapp.RanobeReader.Helpers

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * Created by Ruslan on 15.03.2018.
 */

object ThemeHelper {

    var sTheme = AppCompatDelegate.MODE_NIGHT_NO

    fun change(activity: AppCompatActivity) {
        activity.recreate()
    }

    fun onActivityCreateSetTheme() {
        AppCompatDelegate.setDefaultNightMode(sTheme)
    }

    fun setTheme(theme: Boolean) {
        sTheme = if (theme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    }

    fun toConfiguration(): Int {
        return when (sTheme) {
            AppCompatDelegate.MODE_NIGHT_YES -> Configuration.UI_MODE_NIGHT_YES
            AppCompatDelegate.MODE_NIGHT_NO -> Configuration.UI_MODE_NIGHT_NO
            else -> Configuration.UI_MODE_NIGHT_UNDEFINED
        }
    }
}
