package ru.profapp.RanobeReader.Common;

import android.app.Activity;
import android.content.Intent;

import ru.profapp.RanobeReader.R;

/**
 * Created by Ruslan on 15.03.2018.
 */

public class ThemeUtils {
    private final static int THEME_LIGHT = 0;
    private final static int THEME_DARK = 1;
    private static int sTheme;

    public static void change(Activity activity, Intent intent) {

        activity.finish();

        activity.startActivity(intent);

        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    public static void onActivityCreateSetTheme(Activity activity, Boolean AppBar) {

        switch (sTheme) {

            default:

            case THEME_LIGHT:

                if (AppBar) {
                    activity.setTheme(R.style.AppTheme_Light_NoActionBar);
                } else {
                    activity.setTheme(R.style.AppTheme_Light);
                }
                break;

            case THEME_DARK:

                if (AppBar) {
                    activity.setTheme(R.style.AppTheme_Dark_NoActionBar);
                } else {
                    activity.setTheme(R.style.AppTheme_Dark);
                }
                break;

        }

    }

    public static void setTheme(boolean theme) {
        sTheme = (theme) ? 1 : 0;
    }
}
