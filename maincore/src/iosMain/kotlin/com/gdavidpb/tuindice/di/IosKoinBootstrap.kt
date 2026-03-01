package com.gdavidpb.tuindice.di

import org.koin.core.module.Module

class IosKoinBootstrap(
	private val platformConfig: IosPlatformConfig = IosPlatformConfig(),
	private val platformVariantModules: List<Module> = emptyList()
) : PlatformKoinBootstrap {
	override fun platformModules(): List<Module> {
		return listOf(iosPlatformModule(platformConfig))
	}

	override fun variantModules(): List<Module> {
		return platformVariantModules
	}
}
