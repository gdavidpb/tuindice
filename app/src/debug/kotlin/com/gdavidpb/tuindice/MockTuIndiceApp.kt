package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.di.androidAppMockModule
import com.gdavidpb.tuindice.di.startAndroidAppKoin
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.*

class MockTuIndiceApp : Application() {
	override fun onCreate() {
		super.onCreate()

		Locale.setDefault(DEFAULT_LOCALE)

		FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false

		startAndroidAppKoin(
			application = this,
			platformVariantModules = listOf(androidAppMockModule),
			isOverrideEnabled = true
		)
	}
}
