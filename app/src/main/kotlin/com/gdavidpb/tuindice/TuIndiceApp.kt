package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityLifecycleCallbacks
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityProvider
import com.gdavidpb.tuindice.di.androidReleaseModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.Locale
import java.util.TimeZone

class TuIndiceApp : Application() {
	override fun onCreate() {
		super.onCreate()

		Locale.setDefault(DEFAULT_LOCALE)
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

		val koinApplication = startKoin {
			androidLogger(Level.NONE)
			androidContext(this@TuIndiceApp)
			androidFileProperties()

			modules(androidReleaseModules())
		}

		registerActivityLifecycleCallbacks(
			CurrentActivityLifecycleCallbacks(
				currentActivityProvider = koinApplication.koin.get<CurrentActivityProvider>()
			)
		)
	}
}
