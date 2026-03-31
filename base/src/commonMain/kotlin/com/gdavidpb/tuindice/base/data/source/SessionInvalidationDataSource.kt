package com.gdavidpb.tuindice.base.data.source


import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class SessionInvalidationDataSource : SessionInvalidationRepository {
	private val sessionInvalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

	override fun observeSessionInvalidation(): Flow<Unit> = sessionInvalidations

	override fun notifySessionInvalidated() {
		sessionInvalidations.tryEmit(Unit)
	}
}
