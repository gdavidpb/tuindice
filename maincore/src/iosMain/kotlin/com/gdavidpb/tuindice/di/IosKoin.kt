package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.persistence.di.defaultIosDatabasePath
import org.koin.core.Koin
import org.koin.core.module.Module
import platform.Foundation.NSLock

enum class IosBuildVariant {
	DEBUG,
	PRODUCTION
}

private val iosKoinLock = NSLock()

private inline fun <T> withIosKoinLock(block: () -> T): T {
	iosKoinLock.lock()
	return try {
		block()
	} finally {
		iosKoinLock.unlock()
	}
}

fun startIosKoin(
	databasePath: String,
	platformConfig: IosPlatformConfig = IosPlatformConfig(),
	variantModules: List<Module> = emptyList(),
	extraModules: List<Module> = emptyList()
): Koin {
	return withIosKoinLock {
		IosKoinRuntime.koin?.let { existing -> return@withIosKoinLock existing }

		val koin = startAppKoin(
			AppKoinBootstrapRequest(
				platformBootstrap = IosKoinBootstrap(
					platformConfig = platformConfig.copy(databasePath = databasePath),
					platformVariantModules = variantModules
				),
				extraModules = extraModules
			)
		)

		IosKoinRuntime.koin = koin

		return@withIosKoinLock koin
	}
}

fun startIosKoin(
	bridge: IosPlatformBridge,
	apiBaseUrl: String,
	privacyPolicyUrl: String,
	termsAndConditionsUrl: String,
	debug: Boolean = false,
	buildVariant: IosBuildVariant = if (debug) IosBuildVariant.DEBUG else IosBuildVariant.PRODUCTION,
	databasePath: String = defaultIosDatabasePath(),
	configValues: IosConfigValues = iosDefaultConfigValues(buildVariant),
	extraModules: List<Module> = emptyList()
): Koin {
	val platformConfig = IosPlatformConfig(
		appEnvironment = AppEnvironment(
			apiBaseUrl = apiBaseUrl,
			privacyPolicyUrl = privacyPolicyUrl,
			termsAndConditionsUrl = termsAndConditionsUrl,
			debug = debug
		),
		configValues = configValues,
		bridge = bridge,
		databasePath = databasePath
	)

	return startIosKoin(
		databasePath = databasePath,
		platformConfig = platformConfig,
		variantModules = iosVariantModules(buildVariant),
		extraModules = extraModules
	)
}

fun getIosKoinOrNull(): Koin? = withIosKoinLock { IosKoinRuntime.koin }

fun requireIosKoin(): Koin {
	return getIosKoinOrNull()
		?: error("iOS Koin is not started. Call TuIndiceIosAppLauncher first.")
}

private object IosKoinRuntime {
	var koin: Koin? = null
}
