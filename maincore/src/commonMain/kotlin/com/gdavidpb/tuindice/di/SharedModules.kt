package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.about.di.aboutModule
import com.gdavidpb.tuindice.enrollmentproof.di.enrollmentProofModule
import com.gdavidpb.tuindice.evaluations.di.evaluationsModule
import com.gdavidpb.tuindice.login.di.loginModule
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.summary.di.summaryModule
import org.koin.core.module.Module

fun featureModules(): List<Module> {
	return listOf(
		mainModule,
		loginModule,
		aboutModule,
		summaryModule,
		recordModule,
		evaluationsModule,
		enrollmentProofModule
	)
}

fun commonModules(): List<Module> {
	return buildList {
		add(commonModule)
		addAll(featureModules())
	}
}

fun sharedModules(
	platformModules: List<Module>,
	extraModules: List<Module> = emptyList()
): List<Module> {
	return buildList {
		addAll(commonModules())
		addAll(platformModules)
		addAll(extraModules)
	}
}
