package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.di.androidAppMockModule
import com.gdavidpb.tuindice.di.androidReleaseModules
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DebugKoinDataSource : DependenciesRepository {
	override fun restart() {
		val modules = androidReleaseModules() + androidAppMockModule

		unloadKoinModules(
			modules
		)
		loadKoinModules(
			modules
		)
	}
}
