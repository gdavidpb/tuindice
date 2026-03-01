package com.gdavidpb.tuindice.di

import org.koin.core.module.Module

fun iosModules(
	platformConfig: IosPlatformConfig = IosPlatformConfig(),
	extraModules: List<Module> = emptyList()
): List<Module> {
	return sharedModules(
		platformModules = listOf(iosPlatformModule(platformConfig)),
		extraModules = extraModules
	)
}
