package ru.profapp.RanobeReader.Helpers

import org.jsoup.Jsoup

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
            return Jsoup.parse(string).text()
        }
        @JvmStatic
        fun cleanAdditionalInfo(string: String): String {
            return Jsoup.parse(string).text()
        }
        @JvmStatic
        fun CleanString(ranobeUrl: String): String {
            return ranobeUrl.replace("[^a-zA-Z0-9]".toRegex(), "")
        }

    }
}
