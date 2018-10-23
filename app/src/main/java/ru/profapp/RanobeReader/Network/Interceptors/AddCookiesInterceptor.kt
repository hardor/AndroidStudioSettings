package ru.profapp.RanobeReader.Network.Interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.profapp.RanobeReader.Network.Repositories.BaseRepository
import java.io.IOException

/**
 * This interceptor put all the Cookies in Preferences in the Request.
 * Your implementation on how to get the Preferences may vary.
 */
class AddCookiesInterceptor(private val repository: BaseRepository) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val cookies = repository.Cookie
        for (cookie in cookies) {
            builder.addHeader("Cookie", cookie)
        }
        return chain.proceed(builder.build())
    }
}