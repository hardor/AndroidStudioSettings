package ru.profapp.RanobeReader.JsonApi

/**
 * Created by Ruslan on 09.02.2018.
 */

class JsonRulateApi private constructor() : JsonBaseClass() {
    private val ApiString = "https://tl.rulate.ru/api/%s?key=fpoiKLUues81werht039"

    fun GetFavoriteBooks(token: String): String {
        if (token.isEmpty()) {
            return ""
        }

        var request = String.format(ApiString, "bookmarks")
        request += "&token=$token"

        return getUrlText(request)
    }


    fun AddBookmark(book_id: Int?, token: String): String {
        var request = String.format(ApiString, "addBookmark")
        if (!token.isEmpty()) {
            request += "&token=$token"
        } else {
            return ""
        }
        request += "&book_id=$book_id"

        return getUrlText(request)
    }


    fun RemoveBookmark(book_id: Int?, token: String): String {
        var request = String.format(ApiString, "removeBookmark")
        if (!token.isEmpty()) {
            request += "&token=$token"
        } else {
            return ""
        }
        request += "&book_id=$book_id"

        return getUrlText(request)
    }


    companion object {

        @Volatile
        private var instance: JsonRulateApi? = null

        fun getInstance(): JsonRulateApi? {
            if (instance == null) {
                synchronized(JsonRulateApi::class.java) {
                    if (instance == null) {
                        instance = JsonRulateApi()
                    }
                }
            }
            return instance
        }
    }

}
