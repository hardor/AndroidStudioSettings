package ru.profapp.RanobeReader.JsonApi.IApiServices

import okhttp3.Interceptor
import okhttp3.Response
import ru.profapp.RanobeReader.JsonApi.RanobeHubRepository
import java.io.IOException

class ReceivedCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        if (!originalResponse.headers("set-cookie").isEmpty()) {

            for (header in originalResponse.headers("Set-Cookie")) {
                RanobeHubRepository.Cookie["Cookie"]=header
                break
            }

        }
        return originalResponse
    }
}