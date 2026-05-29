package com.gdavidpb.tuindice.base.data.source


import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class SessionInvalidationDataSource : SessionInvalidationRepository {
	private val sessionInvalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
	private val intentionalSignOutSessionId = MutableStateFlow<String?>(null)

	override fun observeSessionInvalidation(): Flow<Unit> = sessionInvalidations

	override fun markIntentionalSignOut(sessionId: String) {
		if (sessionId.isNotBlank()) {
			intentionalSignOutSessionId.value = sessionId
		}
	}

	override fun clearIntentionalSignOut(sessionId: String) {
		if (intentionalSignOutSessionId.value == sessionId) {
			intentionalSignOutSessionId.value = null
		}
	}

	override fun notifySessionInvalidated(sessionId: String?) {
		if (shouldSuppressInvalidation(sessionId)) return

		sessionInvalidations.tryEmit(Unit)
	}

	private fun shouldSuppressInvalidation(sessionId: String?): Boolean {
		val intentionalSessionId = intentionalSignOutSessionId.value ?: return false

		return sessionId.isNullOrBlank() || sessionId == intentionalSessionId
	}
}
