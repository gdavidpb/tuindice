package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.di.modules.appModule
import com.gdavidpb.tuindice.migrations.MigrationManager
import com.gdavidpb.tuindice.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.utils.DEFAULT_TIME_ZONE
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.*

open class TuIndiceApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Locale.setDefault(DEFAULT_LOCALE)
        TimeZone.setDefault(DEFAULT_TIME_ZONE)

        if (BuildConfig.DEBUG) FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)

        FirebaseRemoteConfig.getInstance().setDefaultsAsync(R.xml.default_remote_config)

        MigrationManager.execute(applicationContext)

        startKoin {
            androidLogger()

            androidContext(this@TuIndiceApp)

            androidFileProperties()

            modules(appModule)
        }
    }
}