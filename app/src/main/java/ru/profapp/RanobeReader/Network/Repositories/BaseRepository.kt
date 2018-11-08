package ru.profapp.RanobeReader.Network.Repositories

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

open class BaseRepository {
    val baseClient = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)!!
    var Cookie: MutableList<String> = mutableListOf()
}