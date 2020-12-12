package com.gdavidpb.tuindice.datastores

import com.gdavidpb.tuindice.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.modules.mockModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DebugKoinDataStore : DependenciesRepository {
    override fun restart() {
        unloadKoinModules(mockModule)
        loadKoinModules(mockModule)
    }
}