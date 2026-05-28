package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.about.di.aboutModule
import com.gdavidpb.tuindice.enrollmentproof.di.enrollmentProofModule
import com.gdavidpb.tuindice.evaluations.di.evaluationsModule
import com.gdavidpb.tuindice.auth.di.authModule
import com.gdavidpb.tuindice.persistence.di.persistenceModule
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.subjects.di.subjectsModule
import com.gdavidpb.tuindice.summary.di.summaryModule
import com.gdavidpb.tuindice.wizard.di.wizardModule
import com.gdavidpb.tuindice.pensum.di.pensumModule
import org.koin.core.module.Module

fun featureModules(): List<Module> {
	return listOf(
		mainModule,
		authModule,
		aboutModule,
		summaryModule,
		recordModule,
		evaluationsModule,
		subjectsModule,
		enrollmentProofModule,
		wizardModule,
		pensumModule
	)
}

fun commonModules(): List<Module> {
	return buildList {
		add(commonModule)
		add(persistenceModule)
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
