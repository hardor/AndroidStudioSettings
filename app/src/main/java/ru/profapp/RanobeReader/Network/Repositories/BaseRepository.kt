package ru.profapp.RanobeReader.Network.Repositories

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

open class BaseRepository {
    private val TIMEOUT_IN_SECONDS: Long = 2
    val baseClient = OkHttpClient().newBuilder()
            .connectTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)!!
    var Cookie: MutableList<String> = mutableListOf()
}