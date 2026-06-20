package com.gdavidpb.tuindice.data.source.network

import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class OutdatedAppEventDataSource : OutdatedAppEventRepository {
	private val events = MutableSharedFlow<OutdatedAppState>(extraBufferCapacity = 1)

	override fun observeOutdatedApp(): Flow<OutdatedAppState> = events

	override suspend fun notifyOutdatedApp(state: OutdatedAppState) {
		events.emit(state)
	}
}
