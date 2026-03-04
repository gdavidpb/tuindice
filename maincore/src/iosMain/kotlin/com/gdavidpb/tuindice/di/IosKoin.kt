package com.gdavidpb.tuindice.di

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
	iOSContext: IOSContext,
	variantModules: List<Module> = emptyList(),
	extraModules: List<Module> = emptyList()
): Koin {
	return withIosKoinLock {
		IosKoinRuntime.koin?.let { existing -> return@withIosKoinLock existing }

		val koin = startAppKoin(
			AppKoinBootstrapRequest(
				platformBootstrap = IosKoinBootstrap(
					iOSContext = iOSContext,
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
	hostConfig: IosAppHostConfig,
	extraModules: List<Module> = emptyList()
): Koin {
	return startIosKoin(
		iOSContext = hostConfig.toIOSContext(),
		variantModules = iosVariantModules(hostConfig.buildVariant),
		extraModules = extraModules
	)
}

fun iosVariantModules(buildVariant: IosBuildVariant): List<Module> {
	return when (buildVariant) {
		IosBuildVariant.DEBUG -> listOf(iosDebugVariantModule)
		IosBuildVariant.PRODUCTION -> emptyList()
	}
}

private object IosKoinRuntime {
	var koin: Koin? = null
}
