package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.migrations.MigrationManager
import com.gdavidpb.tuindice.modules.mockModule
import com.gdavidpb.tuindice.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.utils.DEFAULT_TIME_ZONE
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.*

class TuIndiceMockApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Locale.setDefault(DEFAULT_LOCALE)
        TimeZone.setDefault(DEFAULT_TIME_ZONE)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)

        MigrationManager.execute(applicationContext)

        startKoin {
            androidLogger(Level.NONE)

            androidContext(this@TuIndiceMockApp)

            androidFileProperties()

            modules(mockModule)
        }
    }
}