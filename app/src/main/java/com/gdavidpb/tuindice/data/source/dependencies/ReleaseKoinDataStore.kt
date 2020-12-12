package com.gdavidpb.tuindice.data.source.dependencies

import com.gdavidpb.tuindice.di.modules.appModule
import com.gdavidpb.tuindice.domain.repository.DependenciesRepository
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class ReleaseKoinDataStore : DependenciesRepository {
    override fun restart() {
        unloadKoinModules(appModule)
        loadKoinModules(appModule)
    }
}