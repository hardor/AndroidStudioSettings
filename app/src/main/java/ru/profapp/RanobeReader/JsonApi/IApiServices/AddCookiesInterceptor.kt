package ru.profapp.RanobeReader.JsonApi.IApiServices

import okhttp3.Interceptor
import okhttp3.Response
import ru.profapp.RanobeReader.JsonApi.RanobeHubRepository
import java.io.IOException

/**
 * This interceptor put all the Cookies in Preferences in the Request.
 * Your implementation on how to get the Preferences may vary.
 */
class AddCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val cookies = RanobeHubRepository.Cookie
        for (cookie in cookies) {
            builder.addHeader("Cookie", cookie)
        }
        return chain.proceed(builder.build())
    }
}