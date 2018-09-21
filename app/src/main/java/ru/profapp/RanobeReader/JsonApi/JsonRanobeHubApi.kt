package ru.profapp.RanobeReader.JsonApi

import ru.profapp.RanobeReader.Common.RanobeConstants.RanobeSite.RanobeHub
import java.util.*

/**
 * Created by Ruslan on 09.02.2018.
 */

class JsonRanobeHubApi private constructor() : JsonBaseClass() {
    private val ApiString = ""


    fun Login(login: String, pass: String): String? {
        return null
    }



    fun GetFavoriteBooks(token: String): String? {
        return null
    }


    fun GetChapterText(book_id: Int, chapter_id: Int, token: String): String? {
        return null
    }


    fun AddBookmark(book_id: Int, token: String): String? {
        return null
    }


    fun RemoveBookmark(book_id: Int, token: String): String? {
        return null
    }


    fun GetBookInfo(book_id: Int, token: String): String? {
        return null
    }

    companion object {

        @Volatile
        private var instance: JsonRanobeHubApi? = null

        fun getInstance(): JsonRanobeHubApi? {
            if (instance == null) {
                synchronized(JsonRanobeHubApi::class.java) {
                    if (instance == null) {
                        instance = JsonRanobeHubApi()
                    }
                }
            }
            return instance
        }
    }
}
