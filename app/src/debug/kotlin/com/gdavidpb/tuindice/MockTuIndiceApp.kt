package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.di.androidDebugVariantModule
import com.gdavidpb.tuindice.di.startAndroidAppKoin

class MockTuIndiceApp : Application() {
	override fun onCreate() {
		super.onCreate()

		startAndroidAppKoin(
			application = this,
			platformVariantModules = listOf(androidDebugVariantModule),
			isOverrideEnabled = true
		)
	}
}
