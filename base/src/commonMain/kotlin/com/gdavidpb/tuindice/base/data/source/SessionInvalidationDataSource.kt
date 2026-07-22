package com.gdavidpb.tuindice.base.data.source


import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class SessionInvalidationDataSource : SessionInvalidationRepository {
	// A conflated channel keeps the latest invalidation while the UI is not
	// collecting (backgrounded) and delivers it exactly once on resume.
	private val sessionInvalidations = Channel<Unit>(capacity = Channel.CONFLATED)
	private val intentionalSignOutSessionId = MutableStateFlow<String?>(null)

	override fun observeSessionInvalidation(): Flow<Unit> = sessionInvalidations.receiveAsFlow()

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
		if (consumeSuppression(sessionId)) return

		sessionInvalidations.trySend(Unit)
	}

	private fun consumeSuppression(sessionId: String?): Boolean {
		val intentionalSessionId = intentionalSignOutSessionId.value ?: return false
		val suppressed = sessionId.isNullOrBlank() || sessionId == intentionalSessionId

		if (suppressed) {
			// A marker covers exactly the invalidation raised by its own sign-out;
			// keeping it would silence unrelated invalidations forever.
			intentionalSignOutSessionId.value = null
		}

		return suppressed
	}
}
