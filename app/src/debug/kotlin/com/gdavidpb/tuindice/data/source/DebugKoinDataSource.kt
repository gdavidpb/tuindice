package com.gdavidpb.tuindice.data.source

import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.di.modules.mockModule
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@KoinReflectAPI
class DebugKoinDataSource : DependenciesRepository {
	override fun restart() {
		unloadKoinModules(mockModule)
		loadKoinModules(mockModule)
	}
}