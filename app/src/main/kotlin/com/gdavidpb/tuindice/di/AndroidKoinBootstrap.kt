package com.gdavidpb.tuindice.di

import android.app.Application
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityLifecycleCallbacks
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.module.Module

fun startAndroidAppKoin(
	application: Application,
	platformVariantModules: List<Module> = emptyList(),
	isOverrideEnabled: Boolean = false
): Koin {
	return startAppKoin(
		AppKoinBootstrapRequest(
			platformBootstrap = AndroidKoinBootstrap(
				application = application,
				platformVariantModules = platformVariantModules,
				isOverrideEnabled = isOverrideEnabled
			)
		)
	)
}

class AndroidKoinBootstrap(
	private val application: Application,
	private val platformVariantModules: List<Module> = emptyList(),
	private val isOverrideEnabled: Boolean = false
) : PlatformKoinBootstrap {
	override fun configure(koinApplication: KoinApplication) {
		with(koinApplication) {
			androidLogger(Level.NONE)
			androidContext(application)
			androidFileProperties()

			if (isOverrideEnabled) {
				allowOverride(true)
			}
		}
	}

	override fun platformModules(): List<Module> {
		return listOf(androidPlatformModule)
	}

	override fun variantModules(): List<Module> {
		return platformVariantModules
	}

	override fun afterStart(koin: Koin) {
		application.registerActivityLifecycleCallbacks(
			CurrentActivityLifecycleCallbacks(
				currentActivityProvider = koin.get<CurrentActivityProvider>()
			)
		)
	}
}
