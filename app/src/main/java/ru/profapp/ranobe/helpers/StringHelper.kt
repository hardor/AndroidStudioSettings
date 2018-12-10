package ru.profapp.ranobe.helpers

import android.text.Html

/**
 * Created by Ruslan on 08.02.2018.
 */

fun String.cleanJson(): String {
    return this.substring(0, this.lastIndexOf("}") + 1)
}

fun String.removeTags(): String {
    return Html.fromHtml(this).toString().replace("\n\n", "\n")
}

fun String.CleanString(): String {
    return this.replace("[^a-zA-Z0-9]".toRegex(), "")
}

fun Double.pretty(): String {
    return if (this == this.toLong().toDouble())
        String.format("%d", this.toLong())
    else
        String.format("%s", this)
}