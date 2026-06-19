package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import kotlinx.coroutines.flow.Flow

interface OutdatedAppEventRepository {
	fun observeOutdatedApp(): Flow<OutdatedAppState>

	suspend fun notifyOutdatedApp(state: OutdatedAppState)
}
