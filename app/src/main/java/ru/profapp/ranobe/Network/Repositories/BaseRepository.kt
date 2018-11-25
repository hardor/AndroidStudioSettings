package ru.profapp.ranobe.Network.Repositories

import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import ru.profapp.ranobe.BuildConfig
import java.util.concurrent.TimeUnit

open class BaseRepository {
    val baseClient = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)!!
    var Cookie: MutableList<String> = mutableListOf()

    init {
        if(BuildConfig.DEBUG){
            baseClient.addNetworkInterceptor(StethoInterceptor())
        }
    }
}