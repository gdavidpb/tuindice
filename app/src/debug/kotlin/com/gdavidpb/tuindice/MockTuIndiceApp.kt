package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.di.androidAppMockModule
import com.gdavidpb.tuindice.di.startAndroidAppKoin
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MockTuIndiceApp : Application() {
	override fun onCreate() {
		super.onCreate()

		FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false

		startAndroidAppKoin(
			application = this,
			platformVariantModules = listOf(androidAppMockModule),
			isOverrideEnabled = true
		)
	}
}
