package ru.profapp.ranobe.utils

import android.app.Application
import com.facebook.stetho.Stetho


object StethoUtils {
    fun install(application: Application) {
        // Create an InitializerBuilder
        val initializerBuilder = Stetho.newInitializerBuilder(application)

        // Enable Chrome DevTools

        initializerBuilder.enableWebKitInspector(
                Stetho.defaultInspectorModulesProvider(application)
        )
        // Enable command line interface
        initializerBuilder.enableDumpapp(
                Stetho.defaultDumperPluginsProvider(application)
        )

        // Use the InitializerBuilder to generate an Initializer
        val initializer = initializerBuilder.build()

        // Initialize Stetho with the Initializer
        Stetho.initialize(initializer)

    }
}