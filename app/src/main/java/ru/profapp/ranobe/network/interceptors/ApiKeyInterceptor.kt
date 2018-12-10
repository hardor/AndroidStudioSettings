package ru.profapp.ranobe.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ApiKeyInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = request.url().newBuilder().addQueryParameter("key", "fpoiKLUues81werht039").build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}