package ru.profapp.ranobe.dagger

import android.app.Application
import dagger.Component
import ru.profapp.ranobe.activities.*
import javax.inject.Singleton

@Singleton
@Component(modules = [FabricModule::class, AdModule::class, PreferencesModule::class])
interface ApplicationComponent {

    fun inject(app: Application)
    fun inject(activity: MainActivity)
    fun inject(activity: BackupActivity)
    fun inject(activity: ChapterTextActivity)
    fun inject(activity: DownloadActivity)
    fun inject(activity: RanobeInfoActivity)
    fun inject(activity: SettingsActivity)
}