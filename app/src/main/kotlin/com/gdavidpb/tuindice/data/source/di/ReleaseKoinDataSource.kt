package com.gdavidpb.tuindice.data.source.di

import com.gdavidpb.tuindice.about.di.aboutModule
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.di.appModule
import com.gdavidpb.tuindice.enrollmentproof.di.enrollmentProofModule
import com.gdavidpb.tuindice.evaluations.di.evaluationsModule
import com.gdavidpb.tuindice.login.di.loginModule
import com.gdavidpb.tuindice.persistence.di.persistenceModule
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.summary.di.summaryModule
import com.gdavidpb.tuindice.transactions.di.transactionsModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class ReleaseKoinDataSource : DependenciesRepository {
	override fun restart() {
		unloadKoinModules(
			listOf(
				appModule,
				persistenceModule,
				loginModule,
				summaryModule,
				recordModule,
				aboutModule,
				enrollmentProofModule,
				evaluationsModule,
				transactionsModule
			)
		)
		loadKoinModules(
			listOf(
				appModule,
				persistenceModule,
				loginModule,
				summaryModule,
				recordModule,
				aboutModule,
				enrollmentProofModule,
				evaluationsModule,
				transactionsModule
			)
		)
	}
}