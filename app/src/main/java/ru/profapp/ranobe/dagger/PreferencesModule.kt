package ru.profapp.ranobe.dagger

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import ru.profapp.ranobe.pref.GeneralPreferences
import ru.profapp.ranobe.pref.GeneralPreferencesImpl
import ru.profapp.ranobe.pref.GeneralPreferencesManager
import javax.inject.Singleton

@Module
class PreferencesModule(private val application: Application) {
    @Provides
    @Singleton
    fun provideApplicationContext(): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideDefaultPreferences(): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)

    @Provides
    @Singleton
    fun provideSharedPreferences(name: String): SharedPreferences =
        application.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideGeneralPreferencesProvider(): GeneralPreferences {
        return GeneralPreferencesImpl(provideApplicationContext())
    }

    @Provides
    @Singleton
    fun provideGeneralPreferencesManager(): GeneralPreferencesManager {
        return GeneralPreferencesManager(provideGeneralPreferencesProvider())
    }
}