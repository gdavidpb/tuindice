package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.module.Module

interface PlatformKoinBootstrap {
	fun configure(koinApplication: KoinApplication) = Unit

	fun platformModules(): List<Module>

	fun variantModules(): List<Module> = emptyList()

	fun afterStart(koin: Koin) {
		koin.getAll<AppStartupTask>().forEach { task ->
			task.start()
		}
	}
}
