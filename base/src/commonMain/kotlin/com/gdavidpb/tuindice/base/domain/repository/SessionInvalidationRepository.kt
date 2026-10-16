package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionInvalidationRepository {
	fun observeSessionInvalidation(): Flow<Unit>
	fun markIntentionalSignOut(sessionId: String)
	fun clearIntentionalSignOut(sessionId: String)

	// True while the mark of the sign-out of this session is set: from the sign-out marking it until its
	// local end, the failure of its revoke, or an invalidation that the mark silenced (which consumes it).
	// The recovery by credentials reads it to leave the end of that session to the sign-out.
	fun isIntentionalSignOut(sessionId: String): Boolean

	// True when the notice was raised, false when an intentional sign-out suppressed it.
	fun notifySessionInvalidated(sessionId: String? = null): Boolean
}
