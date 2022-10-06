package com.gdavidpb.tuindice.datasources

import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.modules.mockModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DebugKoinDataSource : DependenciesRepository {
    override fun restart() {
        unloadKoinModules(mockModule)
        loadKoinModules(mockModule)
    }
}