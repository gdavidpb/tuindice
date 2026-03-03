package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.di.startAndroidAppKoin

class TuIndiceApp : Application() {
	override fun onCreate() {
		super.onCreate()

		startAndroidAppKoin(application = this)
	}
}
