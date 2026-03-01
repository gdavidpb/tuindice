package com.gdavidpb.tuindice.di

import org.koin.core.module.Module

fun androidReleaseModules(): List<Module> {
	return appModules(
		platformBootstrap = object : PlatformKoinBootstrap {
			override fun platformModules(): List<Module> {
				return listOf(androidPlatformModule)
			}
		}
	)
}
