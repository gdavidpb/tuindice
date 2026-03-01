package com.gdavidpb.tuindice.di

import org.koin.core.module.Module

fun iosModules(
	platformConfig: IosPlatformConfig = IosPlatformConfig(),
	extraModules: List<Module> = emptyList()
): List<Module> {
	return appModules(
		platformBootstrap = IosKoinBootstrap(
			platformConfig = platformConfig
		),
		extraModules = extraModules
	)
}
