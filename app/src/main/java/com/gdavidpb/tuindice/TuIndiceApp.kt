package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.di.modules.appModule
import com.gdavidpb.tuindice.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.utils.DEFAULT_TIME_ZONE
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

        startKoin {
            androidLogger()

            androidContext(this@TuIndiceApp)

            androidFileProperties()

            modules(appModule)
        }
    }
}