package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionInvalidationRepository {
	fun observeSessionInvalidation(): Flow<Unit>
	fun markIntentionalSignOut(sessionId: String)
	fun clearIntentionalSignOut(sessionId: String)
	fun notifySessionInvalidated(sessionId: String? = null)
}
