package com.gdavidpb.tuindice.di

import org.koin.core.module.Module

fun androidReleaseModules(): List<Module> {
	return sharedModules(
		platformModules = listOf(
			androidPlatformModule
		)
	)
}
