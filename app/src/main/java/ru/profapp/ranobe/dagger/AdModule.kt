package ru.profapp.ranobe.dagger

import com.google.android.gms.ads.AdRequest
import dagger.Module
import dagger.Provides
import ru.profapp.ranobe.BuildConfig
import javax.inject.Singleton

@Module
class AdModule {

    @Provides
    @Singleton
    fun provideAdRequest(): AdRequest {
        val adRequest = AdRequest.Builder()
        AdRequest.Builder()
        if (BuildConfig.DEBUG) {
            adRequest.addTestDevice("sdfsdf")
        }
        return adRequest.build()
    }

}