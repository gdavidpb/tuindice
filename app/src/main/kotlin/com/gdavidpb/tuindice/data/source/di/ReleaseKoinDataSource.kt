package com.gdavidpb.tuindice.data.source.di

import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.di.androidReleaseModules
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class ReleaseKoinDataSource : DependenciesRepository {
	override fun restart() {
		val modules = androidReleaseModules()

		unloadKoinModules(modules)
		loadKoinModules(modules)
	}
}
