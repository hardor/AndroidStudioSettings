package ru.profapp.ranobe.Helpers

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.widget.TextView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import ru.profapp.ranobe.Common.Constants
import java.util.*

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun convertDpToPixel(dp: Int, context: Context): Int {
    val resources = context.resources
    val metrics = Resources.getSystem().displayMetrics
    return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

/**
 * This method converts device specific pixels to density independent pixels.
 *
 * @param px A value in px (pixels) unit. Which we need to convert into db
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent dp equivalent to px value
 */
fun convertPixelsToDp(px: Int, context: Context): Int {
    return px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun convertDpToPixel(dp: Int): Int {
    val metrics = Resources.getSystem().displayMetrics
    return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

/**
 * This method converts device specific pixels to density independent pixels.
 *
 * @param px A value in px (pixels) unit. Which we need to convert into db
 * @return A float value to represent dp equivalent to px value
 */
fun convertPixelsToDp(px: Int): Int {
    return px / (Resources.getSystem().displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

//region Helper method for PreLollipop TextView & Buttons Vector Images

fun setVectorForPreLollipop(resourceId: Int, activity: Context): Drawable? {
    val icon: Drawable? = if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        VectorDrawableCompat.create(activity.resources, resourceId, activity.theme)
    } else {
        activity.resources.getDrawable(resourceId, activity.theme)
    }

    return icon
}

fun setVectorForPreLollipop(textView: TextView, resourceId: Int, activity: Context, position: Constants.ApplicationConstants) {
    val icon: Drawable? = if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        VectorDrawableCompat.create(activity.resources, resourceId,
                activity.theme)
    } else {
        activity.resources.getDrawable(resourceId, activity.theme)
    }
    when (position) {
        Constants.ApplicationConstants.DRAWABLE_LEFT -> textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)

        Constants.ApplicationConstants.DRAWABLE_RIGHT -> textView.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)

        Constants.ApplicationConstants.DRAWABLE_TOP -> textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)

        Constants.ApplicationConstants.DRAWABLE_BOTTOM -> textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon)
    }
}

fun setVectorForPreLollipop(textView: TextView, icon: Drawable, activity: Context, position: Constants.ApplicationConstants) {

    when (position) {
        Constants.ApplicationConstants.DRAWABLE_LEFT -> textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)

        Constants.ApplicationConstants.DRAWABLE_RIGHT -> textView.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)

        Constants.ApplicationConstants.DRAWABLE_TOP -> textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)

        Constants.ApplicationConstants.DRAWABLE_BOTTOM -> textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon)
    }
}

fun dateFromString(text: String?): Date? {
    if (text.isNullOrBlank()) return null

    val dig = text.replace("[^0-9]".toRegex(), "").toInt()
    if (text.contains("минут"))
        return Date().addMinutes(-1 * dig)
    else if (text.contains("час"))
        return Date().addHours(-1 * dig)
    else if (text.contains("день") || text.contains("дн"))
        return Date().addDays(-1 * dig)
    else if (text.contains("недел"))
        return Date().addDays(-7 * dig)
    else if (text.contains("месяц"))
        return Date().addMonths(-1 * dig)

    return null
}
