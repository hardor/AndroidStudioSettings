package ru.profapp.RanobeReader.JsonApi

import org.jsoup.nodes.Document
import ru.profapp.RanobeReader.Common.ErrorConnectionException
import ru.profapp.RanobeReader.Helpers.HtmlParser
import ru.profapp.RanobeReader.Helpers.StringHelper
import ru.profapp.RanobeReader.Helpers.UrlParser
import java.util.*
import java.util.concurrent.ExecutionException

open class JsonBaseClass {

    var Cookies: Map<String, String> = HashMap()


    fun getUrlText(request: String): String {

        try {
            return UrlParser().execute(request).get() ?: throw ErrorConnectionException()

        } catch (ignored: InterruptedException) {

        } catch (ignored: ExecutionException) {
        } catch (e: NullPointerException) {
            throw ErrorConnectionException(e)
        }

        return ""
    }


    fun getUrlText(request: String, header: Map<String, String>): String {

        try {
            return UrlParser(header).execute(request).get() ?: throw ErrorConnectionException()

        } catch (ignored: InterruptedException) {
        } catch (ignored: ExecutionException) {
        } catch (e: NullPointerException) {
            throw ErrorConnectionException(e)
        }

        return ""
    }


    fun getDocumentText(Cookies: Map<String, String>, vararg params: String): String {

        val html: Document
        try {
            html = HtmlParser(Cookies).execute(*params).get()
            val result = html.body().html()
            return StringHelper.cleanJson(result)
        } catch (ignored: InterruptedException) {
        } catch (ignored: ExecutionException) {
        } catch (e: NullPointerException) {
            throw ErrorConnectionException(e)
        }

        return ""
    }


    fun getDocumentText(Cookies: Map<String, String>, Data: Map<String, String>,
                        Header: Map<String, String>,
                        vararg params: String): String {

        val html: Document
        try {
            html = HtmlParser(Cookies, Data, Header).execute(*params).get()

            val result = html.body().html()
            return StringHelper.cleanJson(result)
        } catch (ignored: InterruptedException) {

        } catch (ignored: ExecutionException) {
        } catch (e: NullPointerException) {
            throw ErrorConnectionException(e)
        }

        return ""
    }
}
