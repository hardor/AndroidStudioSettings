package ru.profapp.RanobeReader.Common;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

/**
 * Created by Ruslan on 15.03.2018.
 */

public class ThemeUtils {
    private static int sTheme=AppCompatDelegate.MODE_NIGHT_NO;

    public static void change(AppCompatActivity activity) {
        activity.recreate();
    }

    public static void change(Activity activity) {
        activity.recreate();
    }

    public static void onActivityCreateSetTheme() {
        AppCompatDelegate.setDefaultNightMode(sTheme);
    }

    public static void setTheme(boolean theme) {
        sTheme = (theme) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
    }

}
