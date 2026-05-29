package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.controller.UsageDataCollectionController
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

interface PlatformKoinBootstrap {
	fun configure(koinApplication: KoinApplication) = Unit

	fun platformModules(): List<Module>

	fun variantModules(): List<Module> = emptyList()

	fun afterStart(koin: Koin) {
		koin.getAll<UsageDataCollectionController>().forEach { controller ->
			controller.start()
		}
	}
}

data class AppKoinBootstrapRequest(
	val platformBootstrap: PlatformKoinBootstrap,
	val extraModules: List<Module> = emptyList()
)

fun appModules(
	platformBootstrap: PlatformKoinBootstrap,
	extraModules: List<Module> = emptyList()
): List<Module> {
	return sharedModules(
		platformModules = platformBootstrap.platformModules(),
		extraModules = platformBootstrap.variantModules() + extraModules
	)
}

fun startAppKoin(
	request: AppKoinBootstrapRequest
): Koin {
	val modules = appModules(
		platformBootstrap = request.platformBootstrap,
		extraModules = request.extraModules
	)

	val koin = startKoin {
		request.platformBootstrap.configure(this)
		modules(modules)
	}.koin

	request.platformBootstrap.afterStart(koin)

	return koin
}
