package ru.profapp.RanobeReader.Helpers

import android.os.AsyncTask
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*

/**
 * Created by Ruslan on 08.02.2018.
 */

class HtmlParser : AsyncTask<String, Void, Document> {

    private var Cookies: Map<String, String> = HashMap()
    private var Data: Map<String, String> = HashMap()
    private var Header: Map<String, String> = HashMap()

    constructor()

    constructor(cookies: Map<String, String>) {
        Cookies = cookies
    }

    constructor(cookies: Map<String, String>, data: Map<String, String>) {
        Cookies = cookies
        Data = data
    }

    constructor(cookies: Map<String, String>, data: Map<String, String>, header: Map<String, String>) {
        Cookies = cookies
        Data = data
        Header = header
    }

    override fun doInBackground(vararg params: String): Document? {

        try {
            var method: Connection.Method = Connection.Method.GET

            if (params.size > 1) {
                method = Connection.Method.valueOf(params[1])
            }
            val connection = Jsoup.connect(params[0])
                    .cookies(Cookies)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML," + " like Gecko) Chrome/65.0.3325.162 Safari/537.36")
                    .header("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp," + "image/apng,*/*;q=0.8")
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Accept-Encoding", "gzip, deflate")
                    .data(Data)
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .timeout(20 * 1000)
                    .method(method)
            for ((key, value) in Header) {
                connection.header(key, value)
            }

            return if (method.compareTo(Connection.Method.POST) == 0) {
                connection.post()
            } else {
                connection.get()
            }


        } catch (e: IOException) {
            // MyLog.SendError(StringResources.LogType.WARN, HtmlParser.class.toString(), "", e);
            return null
        }

    }

    override fun onPostExecute(result: Document) {

    }

}
