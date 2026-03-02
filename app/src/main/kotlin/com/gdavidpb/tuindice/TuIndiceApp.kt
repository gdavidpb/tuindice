package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.di.startAndroidAppKoin
import java.util.*

class TuIndiceApp : Application() {
	override fun onCreate() {
		super.onCreate()

		Locale.setDefault(DEFAULT_LOCALE)

		startAndroidAppKoin(application = this)
	}
}
