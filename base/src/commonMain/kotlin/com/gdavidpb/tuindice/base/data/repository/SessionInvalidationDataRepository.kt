package com.gdavidpb.tuindice.base.data.repository

import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class SessionInvalidationDataRepository : SessionInvalidationRepository {
	private val sessionInvalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

	override fun observeSessionInvalidation(): Flow<Unit> = sessionInvalidations

	override fun notifySessionInvalidated() {
		sessionInvalidations.tryEmit(Unit)
	}
}
