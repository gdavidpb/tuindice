package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.di.AndroidKoinBootstrap
import com.gdavidpb.tuindice.di.AppKoinBootstrapRequest
import com.gdavidpb.tuindice.di.startAppKoin
import java.util.*

class TuIndiceApp : Application() {
	override fun onCreate() {
		super.onCreate()

		Locale.setDefault(DEFAULT_LOCALE)

		startAppKoin(
			AppKoinBootstrapRequest(
				platformBootstrap = AndroidKoinBootstrap(
					application = this
				)
			)
		)
	}
}
