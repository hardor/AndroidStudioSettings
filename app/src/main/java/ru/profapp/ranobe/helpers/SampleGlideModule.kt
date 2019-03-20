package ru.profapp.ranobe.helpers

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import okhttp3.OkHttpClient
import ru.profapp.ranobe.R
import java.io.InputStream
import java.util.concurrent.TimeUnit

@GlideModule
class SampleGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val memoryCacheSizeBytes = 1024 * 1024 * 20 // 20mb
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes.toLong()))
        builder.setDiskCache(InternalCacheDiskCacheFactory(context,
            (memoryCacheSizeBytes * 10).toLong()))
        builder.setDefaultRequestOptions(requestOptions)
        builder.build(context)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val client = OkHttpClient.Builder().readTimeout(2, TimeUnit.SECONDS)
            .connectTimeout(2, TimeUnit.SECONDS).build()

        val factory = OkHttpUrlLoader.Factory(client)

        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }

    companion object {
        val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis() / (24 * 60 * 60 * 1000)))
            .override(200, 200).encodeFormat(Bitmap.CompressFormat.PNG).encodeQuality(100)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).format(PREFER_ARGB_8888)
            .skipMemoryCache(false).placeholder(R.drawable.ic_adb_black_24dp)
            .error(R.drawable.ic_error_outline_black_24dp).fitCenter()
    }

}