package com.gdavidpb.tuindice.data.source.dependencies

import com.gdavidpb.tuindice.base.di.baseModule
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.di.appModule
import com.gdavidpb.tuindice.evaluations.di.evaluationsModule
import com.gdavidpb.tuindice.login.di.loginModule
import com.gdavidpb.tuindice.persistence.di.persistenceModule
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.summary.di.summaryModule
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@KoinReflectAPI
class ReleaseKoinDataSource : DependenciesRepository {
	override fun restart() {
		unloadKoinModules(
			listOf(
				appModule,
				baseModule,
				persistenceModule,
				loginModule,
				summaryModule,
				recordModule,
				evaluationsModule
			)
		)
		loadKoinModules(
			listOf(
				appModule,
				baseModule,
				persistenceModule,
				loginModule,
				summaryModule,
				recordModule,
				evaluationsModule
			)
		)
	}
}