package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.di.androidReleaseModules
import com.gdavidpb.tuindice.di.appMockModule
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.Locale
import java.util.TimeZone

class MockTuIndiceApp : Application() {
	override fun onCreate() {
		super.onCreate()

		Locale.setDefault(DEFAULT_LOCALE)
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

		FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false

		startKoin {
			androidLogger(Level.NONE)
			androidContext(this@MockTuIndiceApp)
			androidFileProperties()

			allowOverride(true)

			modules(androidReleaseModules() + appMockModule)
		}
	}
}
