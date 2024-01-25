package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.about.di.aboutModule
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.di.appMockModule
import com.gdavidpb.tuindice.enrollmentproof.di.enrollmentProofModule
import com.gdavidpb.tuindice.evaluations.di.evaluationsModule
import com.gdavidpb.tuindice.login.di.loginModule
import com.gdavidpb.tuindice.persistence.di.persistenceModule
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.summary.di.summaryModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DebugKoinDataSource : DependenciesRepository {
	override fun restart() {
		unloadKoinModules(
			listOf(
				appMockModule,
				persistenceModule,
				loginModule,
				summaryModule,
				recordModule,
				aboutModule,
				enrollmentProofModule,
				evaluationsModule
			)
		)
		loadKoinModules(
			listOf(
				appMockModule,
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
}