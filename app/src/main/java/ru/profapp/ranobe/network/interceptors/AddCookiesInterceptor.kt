package ru.profapp.ranobe.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.profapp.ranobe.network.repositories.BaseRepository
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