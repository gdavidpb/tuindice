package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeSessionInvalidationRepository : SessionInvalidationRepository {
	private val invalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
	var intentionalSignOutSessionId: String? = null
		private set
	val notifiedSessionIds = mutableListOf<String?>()
	var invalidationCalls = 0
		private set

	override fun observeSessionInvalidation(): Flow<Unit> = invalidations

	override fun markIntentionalSignOut(sessionId: String) {
		if (sessionId.isNotBlank()) {
			intentionalSignOutSessionId = sessionId
		}
	}

	override fun clearIntentionalSignOut(sessionId: String) {
		if (intentionalSignOutSessionId == sessionId) {
			intentionalSignOutSessionId = null
		}
	}

	override fun isIntentionalSignOut(sessionId: String): Boolean {
		return sessionId.isNotBlank() && intentionalSignOutSessionId == sessionId
	}

	override fun notifySessionInvalidated(sessionId: String?): Boolean {
		val shouldSuppressInvalidation =
			intentionalSignOutSessionId != null &&
					(sessionId.isNullOrBlank() || sessionId == intentionalSignOutSessionId)

		if (shouldSuppressInvalidation) {
			return false
		}

		invalidationCalls++
		notifiedSessionIds += sessionId
		invalidations.tryEmit(Unit)

		return true
	}
}
