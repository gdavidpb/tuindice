package com.gdavidpb.tuindice.di

import org.koin.core.module.Module

data class AppKoinBootstrapRequest(
	val platformBootstrap: PlatformKoinBootstrap,
	val extraModules: List<Module> = emptyList()
)
