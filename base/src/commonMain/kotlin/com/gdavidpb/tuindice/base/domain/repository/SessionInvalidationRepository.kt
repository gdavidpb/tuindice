package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionInvalidationRepository {
	fun observeSessionInvalidation(): Flow<Unit>
	fun markIntentionalSignOut(sessionId: String)
	fun clearIntentionalSignOut(sessionId: String)

	// True when the notice was raised, false when an intentional sign-out suppressed it.
	fun notifySessionInvalidated(sessionId: String? = null): Boolean
}
