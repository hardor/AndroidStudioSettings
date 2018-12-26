package ru.profapp.ranobe.dagger

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import ru.profapp.ranobe.BuildConfig
import javax.inject.Singleton


@Module
class FabricModule {

    @Provides
    @Singleton
    fun provideCrashlyticsCore(): CrashlyticsCore = CrashlyticsCore.Builder()
            .disabled(BuildConfig.DEBUG)
            .build()

    @Provides
    @Singleton
    fun provideCrashlytics(crashlyticsCore: CrashlyticsCore): Crashlytics = Crashlytics.Builder()
            .core(crashlyticsCore)
            .build()

    @Provides
    @Singleton
    fun provideFabric(context: Context, crashlytics: Crashlytics): Fabric
            = Fabric.with(context, crashlytics)
}