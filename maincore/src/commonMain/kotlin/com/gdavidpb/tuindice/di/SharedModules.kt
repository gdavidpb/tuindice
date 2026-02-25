package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.about.di.aboutCommonModule
import com.gdavidpb.tuindice.enrollmentproof.di.enrollmentProofCommonModule
import com.gdavidpb.tuindice.evaluations.di.evaluationsCommonModule
import com.gdavidpb.tuindice.login.di.loginCommonModule
import com.gdavidpb.tuindice.record.di.recordCommonModule
import com.gdavidpb.tuindice.summary.di.summaryCommonModule
import org.koin.core.module.Module

fun sharedCommonModules(): List<Module> {
	return listOf(
		mainCommonModule,
		loginCommonModule,
		aboutCommonModule,
		summaryCommonModule,
		recordCommonModule,
		evaluationsCommonModule,
		enrollmentProofCommonModule
	)
}

fun sharedModules(
	platformModules: List<Module>,
	extraModules: List<Module> = emptyList()
): List<Module> {
	return buildList {
		addAll(sharedCommonModules())
		addAll(platformModules)
		addAll(extraModules)
	}
}
