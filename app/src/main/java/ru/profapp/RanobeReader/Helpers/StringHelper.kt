package ru.profapp.RanobeReader.Helpers

import android.text.Html

/**
 * Created by Ruslan on 08.02.2018.
 */
class StringHelper {
    companion object {

        fun cleanJson(string: String): String {
            return string.substring(0, string.lastIndexOf("}") + 1)
        }


        fun removeTags(string: String): String {
            return Html.fromHtml(string).toString().replace("\n\n", "\n")
        }


        fun CleanString(ranobeUrl: String): String {
            return ranobeUrl.replace("[^a-zA-Z0-9]".toRegex(), "")
        }

    }
}
