package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.about.di.aboutModule
import com.gdavidpb.tuindice.enrollmentproof.di.enrollmentProofModule
import com.gdavidpb.tuindice.evaluations.di.evaluationsModule
import com.gdavidpb.tuindice.login.di.loginModule
import com.gdavidpb.tuindice.persistence.di.persistenceModule
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.summary.di.summaryModule
import org.koin.core.module.Module

fun androidReleaseModules(): List<Module> {
	return sharedModules(
		platformModules = listOf(
			appModule,
			persistenceModule,
			loginModule,
			summaryModule,
			recordModule,
			aboutModule,
			enrollmentProofModule,
			evaluationsModule
		)
	)
}
