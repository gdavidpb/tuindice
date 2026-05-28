package com.gdavidpb.tuindice.di

import org.koin.core.KoinApplication
import org.koin.core.module.Module

class IosKoinBootstrap(
	private val iOSContext: IOSContext,
	private val platformVariantModules: List<Module> = emptyList()
) : PlatformKoinBootstrap {
	override fun configure(koinApplication: KoinApplication) {
		koinApplication.iOSContext(iOSContext)
	}

	override fun platformModules(): List<Module> {
		return listOf(iosPlatformModule)
	}

	override fun variantModules(): List<Module> {
		return platformVariantModules
	}
}
