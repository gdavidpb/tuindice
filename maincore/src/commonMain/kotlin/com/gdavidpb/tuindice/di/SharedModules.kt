package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.about.di.aboutCoreModule
import com.gdavidpb.tuindice.enrollmentproof.di.enrollmentProofCoreModule
import com.gdavidpb.tuindice.evaluations.di.evaluationsCoreModule
import com.gdavidpb.tuindice.login.di.loginCoreModule
import com.gdavidpb.tuindice.record.di.recordCoreModule
import com.gdavidpb.tuindice.summary.di.summaryCoreModule
import org.koin.core.module.Module

fun sharedCoreModules(): List<Module> {
	return listOf(
		mainCoreModule,
		loginCoreModule,
		aboutCoreModule,
		summaryCoreModule,
		recordCoreModule,
		evaluationsCoreModule,
		enrollmentProofCoreModule
	)
}

fun sharedModules(
	platformModules: List<Module>,
	extraModules: List<Module> = emptyList()
): List<Module> {
	return buildList {
		addAll(sharedCoreModules())
		addAll(platformModules)
		addAll(extraModules)
	}
}
