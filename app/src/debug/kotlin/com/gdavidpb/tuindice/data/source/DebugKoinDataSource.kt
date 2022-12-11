package com.gdavidpb.tuindice.data.source

import com.gdavidpb.tuindice.base.di.baseModule
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.di.modules.appMockModule
import com.gdavidpb.tuindice.evaluations.di.modules.evaluationsModule
import com.gdavidpb.tuindice.login.di.modules.loginModule
import com.gdavidpb.tuindice.record.di.modules.recordModule
import com.gdavidpb.tuindice.summary.di.modules.summaryModule
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@KoinReflectAPI
class DebugKoinDataSource : DependenciesRepository {
	override fun restart() {
		unloadKoinModules(
			listOf(
				appMockModule,
				baseModule,
				loginModule,
				summaryModule,
				recordModule,
				evaluationsModule
			)
		)
		loadKoinModules(
			listOf(
				appMockModule,
				baseModule,
				loginModule,
				summaryModule,
				recordModule,
				evaluationsModule
			)
		)
	}
}