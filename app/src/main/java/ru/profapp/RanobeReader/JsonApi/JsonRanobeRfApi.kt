package ru.profapp.RanobeReader.JsonApi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import ru.profapp.RanobeReader.Common.Constants.RanobeSite.RanobeRf
import ru.profapp.RanobeReader.JsonApi.Ranoberf.Sequence
import ru.profapp.RanobeReader.Models.Chapter
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Created by Ruslan on 09.02.2018.
 */

class JsonRanobeRfApi private constructor() : JsonBaseClass() {


    private var sequence = ""





    fun GetChapterText(chapter: Chapter): String {

        var ranobeName = chapter.ranobeUrl.replace(RanobeRf.url, "")

        var chapterName = chapter.url.replace(RanobeRf.url, "").replace(
                ranobeName, "")

        ranobeName = ranobeName.replace("/", "")
        chapterName = chapterName.replace("/", "")
        val request = (RanobeRf.url + "/v1/part/get/?bookAlias=" + ranobeName
                + "&partAlias=" + chapterName)
        return getUrlText(request)

    }



    fun RemoveBookmark(bookmark_id: Int, token: String): String {

        if (!token.isEmpty()) {

            try {

                val request = RanobeRf.url + "/v1/bookmark/delete/"

                val rawData = "id=" + Integer.toString(bookmark_id)

                val obj = URL(request)
                val con = obj.openConnection() as HttpURLConnection

                //add reuqest header
                con.requestMethod = "DELETE" //e.g POST
                con.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded") //e.g key = Accept, value =
                // application/json
                con.setRequestProperty("Authorization", "Bearer $token")
                //e.g key = Accept, value = application/json
                con.setRequestProperty("Accept",
                        "*/*") //e.g key = Accept, value = application/json

                con.doOutput = true

                val w = OutputStreamWriter(con.outputStream, "UTF-8")

                w.write(rawData)
                w.close()

                val responseCode = con.responseCode

                val ino = BufferedReader(InputStreamReader(con.inputStream))

                var inputLine: String?
                val response = StringBuilder()

                do {

                    inputLine = ino.readLine()

                    if (inputLine == null)

                        break

                    response.append(inputLine)

                } while (true)

                ino.close()

                return response.toString()
            } catch (ignored: Exception) {
            }

        }
        return ""

    }


    fun AddBookmark(book_id: Int?, part_id: Int?, token: String): String {
        return if (!token.isEmpty()) {
            val request = RanobeRf.url + "/v1/bookmark/add/"

            val header = HashMap<String, String>()
            header["Content-Type"] = "application/x-www-form-urlencoded"
            header["Authorization"] = "Bearer $token"
            header["Accept"] = "*/*"
            val data = HashMap<String, String>()
            data["book_id"] = Integer.toString(book_id!!)
            data["part_id"] = Integer.toString(part_id!!)
            getDocumentText(Cookies, data, header, request, Connection.Method.POST.name)
        } else {
            ""
        }
    }



    fun GetFavoriteBooks(token: String): String {
        val request = RanobeRf.url + "/v1/bookmark/index/"

        val data = HashMap<String, String>()
        val header = HashMap<String, String>()
        header["Authorization"] = "Bearer $token"
        header["Accept"] = "*/*"

        return getDocumentText(Cookies, data, header, request)
    }

    companion object {
        @Volatile
        private var instance: JsonRanobeRfApi? = null

        fun getInstance(): JsonRanobeRfApi? {
            if (instance == null) {
                synchronized(JsonRanobeRfApi::class.java) {
                    if (instance == null) {
                        instance = JsonRanobeRfApi()
                    }
                }
            }
            return instance
        }
    }
}
