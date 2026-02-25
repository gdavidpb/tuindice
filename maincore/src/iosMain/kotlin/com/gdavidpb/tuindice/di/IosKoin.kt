package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.di.createIosDatabase
import com.gdavidpb.tuindice.persistence.di.defaultIosDatabasePath
import com.gdavidpb.tuindice.persistence.di.persistenceCoreModule
import org.koin.core.Koin
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.unloadKoinModules
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

fun iosModules(
	database: TuIndiceDatabase,
	platformConfig: IosPlatformConfig = IosPlatformConfig(),
	extraModules: List<Module> = emptyList()
): List<Module> {
	return iosSharedModules(
		platformConfig = platformConfig,
		extraModules = listOf(persistenceCoreModule(database)) + extraModules
	)
}

fun startIosKoin(
	databasePath: String,
	platformConfig: IosPlatformConfig = IosPlatformConfig(),
	extraModules: List<Module> = emptyList()
): Koin {
	return withIosKoinLock {
		IosKoinRuntime.koin?.let { existing -> return@withIosKoinLock existing }

		val database = createIosDatabase(path = databasePath)
		val modules = iosModules(
			database = database,
			platformConfig = platformConfig,
			extraModules = extraModules
		)

		IosKoinRuntime.modules = modules

		val koin = startKoin {
			modules(modules)
		}.koin

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
	configValues: IosConfigValues = IosConfigValues(),
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
		bridge = bridge
	)

	return startIosKoin(
		databasePath = databasePath,
		platformConfig = platformConfig,
		extraModules = iosVariantModules(buildVariant) + extraModules
	)
}

fun restartIosKoinModules(): Boolean {
	return withIosKoinLock {
		val modules = IosKoinRuntime.modules
		if (modules.isEmpty()) return@withIosKoinLock false

		unloadKoinModules(modules)
		loadKoinModules(modules)

		return@withIosKoinLock true
	}
}

fun getIosKoinOrNull(): Koin? = withIosKoinLock { IosKoinRuntime.koin }

fun requireIosKoin(): Koin {
	return getIosKoinOrNull()
		?: error("iOS Koin is not started. Call TuIndiceIosEntryPoint first.")
}

private object IosKoinRuntime {
	var modules: List<Module> = emptyList()
	var koin: Koin? = null
}
