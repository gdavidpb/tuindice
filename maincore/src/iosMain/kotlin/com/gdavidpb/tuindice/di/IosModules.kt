package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.enrollmentproof.di.enrollmentProofIosModule
import com.gdavidpb.tuindice.summary.di.summaryIosModule
import org.koin.core.module.Module

fun iosFeatureModules(
	platformConfig: IosPlatformConfig = IosPlatformConfig()
): List<Module> {
	return listOf(
		iosPlatformModule(platformConfig),
		summaryIosModule,
		enrollmentProofIosModule
	)
}

fun iosSharedModules(
	platformConfig: IosPlatformConfig = IosPlatformConfig(),
	extraModules: List<Module> = emptyList()
): List<Module> {
	return sharedModules(
		platformModules = iosFeatureModules(platformConfig),
		extraModules = extraModules
	)
}
