package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.di.modules.appModule
import com.gdavidpb.tuindice.migrations.MigrationManager
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.base.utils.DEFAULT_TIME_ZONE
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.*

@KoinReflectAPI
class TuIndiceApp : Application() {
	override fun onCreate() {
		super.onCreate()

		Locale.setDefault(DEFAULT_LOCALE)
		TimeZone.setDefault(DEFAULT_TIME_ZONE)

		MigrationManager.execute(applicationContext)

		startKoin {
			androidLogger(Level.NONE)

			androidContext(this@TuIndiceApp)

			androidFileProperties()

			modules(appModule)
		}
	}
}