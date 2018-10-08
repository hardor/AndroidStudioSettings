package ru.profapp.RanobeReaderTest.Helpers

import android.text.Html

/**
 * Created by Ruslan on 08.02.2018.
 */
class StringHelper {
    companion object {
        @JvmStatic
        fun cleanJson(string: String): String {
            return string.substring(0, string.lastIndexOf("}") + 1)
        }

        @JvmStatic
        fun removeTags(string: String): String {
            return Html.fromHtml(string).toString().replace("\n\n", "\n")
        }

        @JvmStatic
        fun CleanString(ranobeUrl: String): String {
            return ranobeUrl.replace("[^a-zA-Z0-9]".toRegex(), "")
        }

    }
}