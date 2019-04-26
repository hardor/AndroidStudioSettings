package ru.profapp.ranobe.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.profapp.ranobe.network.repositories.BaseRepository
import java.io.IOException

class ReceivedCookiesInterceptor(private val repository: BaseRepository) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        if (!originalResponse.headers("set-cookie").isEmpty()) {
            repository.Cookie.clear()
            for (header in originalResponse.headers("Set-Cookie")) {
                repository.Cookie.add(header)
            }

        }
        return originalResponse
    }
}