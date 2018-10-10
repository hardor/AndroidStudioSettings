package ru.profapp.RanobeReader.Helpers

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * Created by Ruslan on 15.03.2018.
 */

object ThemeHelper {

    private var sTheme = AppCompatDelegate.MODE_NIGHT_NO

    fun change(activity: AppCompatActivity) {
        activity.recreate()
    }

    fun onActivityCreateSetTheme() {
        AppCompatDelegate.setDefaultNightMode(sTheme)
    }

    fun setTheme(theme: Boolean) {
        sTheme = if (theme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    }

}
